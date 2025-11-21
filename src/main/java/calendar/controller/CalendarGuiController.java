package calendar.controller;

import calendar.controller.guicommands.CalendarGuiCommand;
import calendar.controller.guicommands.CreateCalendarCommand;
import calendar.controller.guicommands.EditCalendarCommand;
import calendar.controller.guicommands.NextMonthCommand;
import calendar.controller.guicommands.PrevMonthCommand;
import calendar.controller.guicommands.SelectCalendarCommand;
import calendar.controller.service.CalendarFormService;
import calendar.controller.service.EventCreationRequest;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.model.exception.ConflictException;
import calendar.model.exception.ValidationException;
import calendar.view.CalendarGuiViewInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Controller for the GUI view.
 */
public class CalendarGuiController implements ActionListener {
  private final CalendarGuiViewInterface view;
  private final CalendarManager calendarManager;
  private GuiCalendar inUseGuiCalendar;
  private final Map<String, CalendarGuiCommand> commandMap = new HashMap<>();
  private final Set<String> knownCalendars = new HashSet<>();
  private final CalendarFormService formService = new CalendarFormService();
  private LocalDate selectedDate;

  /**
   * Creates the controller.
   */
  public CalendarGuiController(CalendarSettings settings,
                               CalendarGuiViewInterface view,
                               GuiCalendar inUseCalendar,
                               CalendarManager calendarManager) {
    this.view = view;
    this.calendarManager = calendarManager;
    this.inUseGuiCalendar = inUseCalendar;

    bindCommands();
    view.setCommandButtonListener(this);

    knownCalendars.add(inUseCalendar.getName());
    view.addCalendarToSelector(inUseCalendar.getName());
    view.selectCalendarOnCalendarSelector(inUseCalendar.getName());
    view.setActiveCalendarName(inUseCalendar.getName());
    view.setActiveCalendarTimezone(inUseCalendar.getZoneId());

    selectedDate = inUseCalendar.getMonth().atDay(1);
    view.setSelectedDate(selectedDate);
    view.drawMonth(inUseCalendar.getMonth());
    refreshEvents();
  }

  private void bindCommands() {
    commandMap.put("prev-month", new PrevMonthCommand());
    commandMap.put("next-month", new NextMonthCommand());
    commandMap.put("create-calendar", new CreateCalendarCommand());
    commandMap.put("select-calendar", new SelectCalendarCommand());
    commandMap.put("edit-calendar", new EditCalendarCommand());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String cmdName = e.getActionCommand();
    if (cmdName.startsWith("select-day-")) {
      handleSelectDay(LocalDate.parse(cmdName.substring("select-day-".length())));
      return;
    }
    if ("create-event".equals(cmdName)) {
      handleCreateEvent();
      return;
    }

    CalendarGuiCommand cmd = commandMap.get(cmdName);
    if (cmd == null) {
      view.showError("Unknown command: " + cmdName);
      return;
    }

    cmd.run(calendarManager, inUseGuiCalendar, this, view);
  }

  private void handleSelectDay(LocalDate date) {
    this.selectedDate = date;
    view.setSelectedDate(date);
    refreshEvents();
  }

  private void handleCreateEvent() {
    if (selectedDate == null) {
      view.showError("Select a date first.");
      return;
    }

    Optional<String> request = view.promptForCreateEvent(selectedDate);
    if (request.isEmpty()) {
      return;
    }

    try {
      EventCreationRequest parsed = formService.parseCreateEventCommand(request.get());
      formService.applyCreateEvent(parsed, getActiveCalendar());
      refreshEvents();
    } catch (ValidationException e) {
      view.showError("Fields are invalid " + e.getMessage());
    } catch (ConflictException e) {
      view.showError("Event conflict: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }

  private void refreshEvents() {
    if (selectedDate == null) {
      return;
    }

    List<Event> events = getActiveCalendar().eventsOn(selectedDate);
    List<String> lines = new ArrayList<>();
    for (Event event : events) {
      lines.add(String.format(Locale.ROOT, "- %s from %s to %s",
          event.subject(),
          event.start().toLocalTime(),
          event.end().toLocalTime()));
    }
    view.displayEvents(selectedDate, lines);
  }

  private TimeZoneInMemoryCalendarInterface getActiveCalendar() {
    return calendarManager.getCalendar(inUseGuiCalendar.getName());
  }

  /**
   * Registers a newly created calendar name to avoid duplicates.
   */
  public void registerCalendarName(String name) {
    if (knownCalendars.add(name)) {
      view.addCalendarToSelector(name);
    }
  }

  /**
   * Invoked by commands when the active calendar changes.
   */
  public void setInUseCalendar(GuiCalendar newCalendar) {
    this.inUseGuiCalendar = newCalendar;
    selectedDate = newCalendar.getMonth().atDay(1);
    view.setActiveCalendarName(newCalendar.getName());
    view.setActiveCalendarTimezone(newCalendar.getZoneId());
    if (knownCalendars.add(newCalendar.getName())) {
      view.addCalendarToSelector(newCalendar.getName());
    }
    view.selectCalendarOnCalendarSelector(newCalendar.getName());
    view.setSelectedDate(selectedDate);
    view.drawMonth(newCalendar.getMonth());
    refreshEvents();
  }

  /**
   * Called after month navigation commands.
   *
   * @param newMonth month to display.
   */
  public void onMonthChanged(YearMonth newMonth) {
    this.selectedDate = newMonth.atDay(1);
    view.setSelectedDate(selectedDate);
    view.drawMonth(newMonth);
    refreshEvents();
  }

  /**
   * Refreshes the GUI after a calendar edit.
   */
  public void refreshActiveCalendar() {
    TimeZoneInMemoryCalendarInterface active = getActiveCalendar();
    setInUseCalendar(new GuiCalendar(active));
  }
}
