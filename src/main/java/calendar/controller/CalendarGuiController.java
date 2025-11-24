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
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.model.exception.ConflictException;
import calendar.model.exception.ValidationException;
import calendar.view.CalendarGuiFeatures;
import calendar.view.CalendarGuiViewInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import calendar.view.model.GuiEventSummary;
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
  private GuiCalendarInterface inUseGuiCalendar;
  private final CalendarManager calendarManager;
  private final Map<String, CalendarGuiCommand> commandMap = new HashMap<>();
  private final Set<String> knownCalendars = new HashSet<>();
  private final CalendarFormService formService = new CalendarFormService();
  private LocalDate selectedDate;

  /**
   * Creates the controller.
   */
  public CalendarGuiController(CalendarSettings settings, CalendarGuiViewInterface view,
                               GuiCalendarInterface inUseCalendar,
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
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    CalendarGuiCommand command = commandMap.get(cmd);

    if (command != null) {
      command.run(calendarManager, inUseGuiCalendar, this, view);
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
  public void setInUseCalendar(TimeZoneInMemoryCalendarInterface newCalendar) {
    this.inUseGuiCalendar.switchCalendar(newCalendar);
  }

  @Override
  public void goToPreviousMonth() {

  }

  @Override
  public void goToNextMonth() {

  }

  @Override
  public void requestEventCreation() {

  }

  @Override
  public void requestEventEdit(GuiEventSummary summary) {

  }

  @Override
  public void requestCalendarCreation() {

  }

  @Override
  public void requestCalendarEdit() {

  }

  @Override
  public void calendarSelected(String name) {

  }

  @Override
  public void daySelected(LocalDate date) {

  }
}
