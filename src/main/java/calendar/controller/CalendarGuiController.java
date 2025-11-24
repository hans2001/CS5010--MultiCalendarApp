package calendar.controller;

import calendar.controller.guicommands.CalendarGuiCommand;
import calendar.controller.guicommands.CalendarGuiCommandContext;
import calendar.controller.guicommands.CreateCalendarCommand;
import calendar.controller.guicommands.EditCalendarCommand;
import calendar.controller.guicommands.NextMonthCommand;
import calendar.controller.guicommands.PrevMonthCommand;
import calendar.controller.guicommands.SelectCalendarCommand;
import calendar.controller.service.CalendarFormService;
import calendar.controller.service.EventCreationRequest;
import calendar.controller.service.EventEditRequest;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.model.exception.ConflictException;
import calendar.model.exception.ValidationException;
import calendar.view.CalendarGuiFeatures;
import calendar.view.CalendarGuiViewInterface;
import calendar.view.model.GuiEventSummary;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Controller for the GUI view.
 */
public class CalendarGuiController implements CalendarGuiFeatures {
  private final CalendarGuiViewInterface view;
  private final CalendarManager calendarManager;
  private final Map<String, CalendarGuiCommand> commandMap = new HashMap<>();
  private final Set<String> knownCalendars = new HashSet<>();
  private final CalendarFormService formService = new CalendarFormService();
  private LocalDate selectedDate;
  private GuiCalendar inUseGuiCalendar;

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
    view.setFeatures(this);

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
  public void goToPreviousMonth() {
    executeCommand("prev-month");
  }

  @Override
  public void goToNextMonth() {
    executeCommand("next-month");
  }

  @Override
  public void requestEventCreation() {
    handleCreateEvent();
  }

  @Override
  public void requestEventEdit(GuiEventSummary summary) {
    handleEditEvent(summary);
  }

  @Override
  public void requestCalendarCreation() {
    executeCommand("create-calendar");
  }

  @Override
  public void requestCalendarEdit() {
    executeCommand("edit-calendar");
  }

  @Override
  public void calendarSelected(String name) {
    executeCommand("select-calendar");
  }

  @Override
  public void daySelected(LocalDate date) {
    handleSelectDay(date);
  }

  private void executeCommand(String name) {
    CalendarGuiCommand cmd = commandMap.get(name);
    if (cmd == null) {
      view.showError("Unknown command: " + name);
      return;
    }
    CalendarGuiCommandContext context = new CalendarGuiCommandContext(
        calendarManager, inUseGuiCalendar, this, view);
    cmd.run(context);
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

    Optional<EventCreationRequest> request = view.promptForCreateEvent(selectedDate);
    if (request.isEmpty()) {
      return;
    }

    try {
      formService.applyCreateEvent(request.get(), getActiveCalendar());
      view.showMessage("Event created successfully.");
      refreshEvents();
    } catch (ValidationException e) {
      view.showError("Fields are invalid " + e.getMessage());
    } catch (ConflictException e) {
      view.showError("Event conflict: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }

  private void handleEditEvent(GuiEventSummary summary) {
    Optional<EventEditRequest> command = view.promptForEditEvent(summary);
    if (command.isEmpty()) {
      return;
    }

    try {
      formService.applyEditEvent(command.get(), getActiveCalendar());
      view.showMessage("Event updated successfully.");
      refreshEvents();
    } catch (ValidationException e) {
      view.showError("Invalid update values: " + e.getMessage());
    } catch (ConflictException e) {
      view.showError("Update failed due to event conflict: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }

  private void refreshEvents() {
    if (selectedDate == null) {
      return;
    }

    List<Event> events = getActiveCalendar().eventsOn(selectedDate);
    List<GuiEventSummary> summaries = new ArrayList<>();
    for (Event event : events) {
      summaries.add(new GuiEventSummary(
          event.subject(),
          event.start(),
          event.end(),
          event.description().orElse(""),
          event.location().orElse(""),
          event.status()));
    }
    view.displayEvents(selectedDate, summaries);
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
   * Updates the known calendar cache when a calendar is renamed.
   *
   * @param oldName previous name.
   * @param newName new name.
   */
  public void renameKnownCalendar(String oldName, String newName) {
    knownCalendars.remove(oldName);
    knownCalendars.add(newName);
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
   * Gets the name of the inUseCalendar.
   *
   * @return name of calendar.
   */
  public String getName() {
    return this.inUseGuiCalendar.getName();
  }

  /**
   * Gets the name of the inUseCalendar.
   *
   * @return name of calendar.
   */
  public String getZoneId() {
    return this.inUseGuiCalendar.getZoneId();
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

  /**
   * Executes the command.
   *
   * @param e event.
   */
  public void actionPerformed(ActionEvent e) {
    executeCommand(e.getActionCommand());
  }

}
