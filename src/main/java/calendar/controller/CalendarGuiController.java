package calendar.controller;

import calendar.controller.guicommands.CalendarGuiCommand;
import calendar.controller.guicommands.CreateCalendarCommand;
import calendar.controller.guicommands.EditCalendarCommand;
import calendar.controller.guicommands.NextMonthCommand;
import calendar.controller.guicommands.PrevMonthCommand;
import calendar.controller.guicommands.SelectCalendarCommand;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.config.CalendarSettings;
import calendar.view.CalendarGuiViewInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controller for the Gui.
 */
public class CalendarGuiController implements ActionListener {
  private final CalendarSettings settings;
  private final CalendarGuiViewInterface view;
  private GuiCalendar inUseGuiCalendar;
  private final CalendarManager calendarManager;
  Map<String, CalendarGuiCommand> commandMap = new HashMap<>();

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
    this.inUseGuiCalendar = inUseCalendar;
    this.settings = settings;
    this.view = view;

    bindCommands();
    view.setCommandButtonListener(this);

    view.drawMonth(inUseGuiCalendar.getMonth());
    view.setActiveCalendarName(inUseGuiCalendar.getName());
    view.setActiveCalendarTimezone(inUseGuiCalendar.getZoneId());
    view.addCalendarToSelector(inUseGuiCalendar.getName());
    view.selectCalendarOnCalendarSelector(inUseGuiCalendar.getName());
  }

  /**
   * Binds commands to the command name.
   */
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
    CalendarGuiCommand cmd = commandMap.get(cmdName);

    if (cmd == null) {
      view.showError("Unknown command: " + cmdName);
      return;
    }

    cmd.run(calendarManager, inUseGuiCalendar, this, view);
  }

  /**
   * Sets the inUseCalendar to a new calendar.
   *
   * @param newCalendar new calendar.
   */
  public void setInUseCalendar(GuiCalendar newCalendar) {
    this.inUseGuiCalendar = newCalendar;
  }
}
