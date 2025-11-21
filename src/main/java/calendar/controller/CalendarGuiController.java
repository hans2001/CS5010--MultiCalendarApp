package calendar.controller;

import calendar.controller.guicommands.CalendarGuiCommand;
import calendar.controller.guicommands.NextMonthCommand;
import calendar.controller.guicommands.PrevMonthCommand;
import calendar.controller.service.CalendarFormService;
import calendar.controller.service.EventCreationRequest;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the Gui.
 */
public class CalendarGuiController implements ActionListener {
  private final CalendarSettings settings;
  private final CalendarGuiViewInterface view;
  private GuiCalendar inUseCalendar;
  private final CalendarManager calendarManager;
  Map<String, CalendarGuiCommand> commandMap = new HashMap<>();
  private final CalendarFormService formService = new CalendarFormService();
  private LocalDate selectedDate;

  /**
   * Create a controller for the gui.
   *
   * @param settings configuration for the session's behavior and display.
   * @param view the gui view.
   * @param inUseCalendar the calendar in use.
   * @param calendarManager manager.
   */
  public CalendarGuiController(CalendarSettings settings, CalendarGuiViewInterface view,
                               GuiCalendar inUseCalendar,
                               CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
    this.inUseCalendar = inUseCalendar;
    this.settings = settings;
    this.view = view;

    bindCommands();
    view.setCommandButtonListener(this);

    selectedDate = inUseCalendar.getMonth().atDay(1);
    view.setSelectedDate(selectedDate);
    view.drawMonth(inUseCalendar.getMonth());
    refreshEvents();
  }

  /**
   * Binds commands to the command name.
   */
  private void bindCommands() {
    commandMap.put("prev-month", new PrevMonthCommand());
    commandMap.put("next-month", new NextMonthCommand());
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

    cmd.run(calendarManager, inUseCalendar, view);

    if ("prev-month".equals(cmdName) || "next-month".equals(cmdName)) {
      selectedDate = inUseCalendar.getMonth().atDay(1);
      view.setSelectedDate(selectedDate);
      refreshEvents();
    }
  }

  private void handleSelectDay(LocalDate date) {
    this.selectedDate = date;
    view.setSelectedDate(date);
    refreshEvents();
  }

  private void refreshEvents() {
    if (selectedDate == null) {
      return;
    }
    List<Event> events = inUseCalendar.eventsOn(selectedDate);
    List<String> lines = new ArrayList<>();
    for (Event event : events) {
      lines.add(String.format(Locale.ROOT, "- %s from %s to %s",
          event.subject(),
          event.start().toLocalTime(),
          event.end().toLocalTime()));
    }
    view.displayEvents(selectedDate, lines);
  }

  private void handleCreateEvent() {
    if (selectedDate == null) {
      view.showError("Select a date first.");
      return;
    }

    Optional<String> command = view.promptForCreateEvent(selectedDate);
    if (command.isEmpty()) {
      return;
    }

    try {
      EventCreationRequest request = formService.parseCreateEventCommand(command.get());
      formService.applyCreateEvent(request, inUseCalendar);
      refreshEvents();
    } catch (ValidationException e1) {
      view.showError("Fields are invalid " + e1.getMessage());
    } catch (ConflictException e2) {
      view.showError("Event conflict: " + e2.getMessage());
    } catch (IllegalArgumentException e3) {
      view.showError(e3.getMessage());
    }
  }
}
