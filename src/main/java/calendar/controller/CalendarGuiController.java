package calendar.controller;

import calendar.controller.guicommands.CalendarGuiCommand;
import calendar.controller.guicommands.NextMonthCommand;
import calendar.controller.guicommands.PrevMonthCommand;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.config.CalendarSettings;
import calendar.view.CalendarGuiViewInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Gui.
 */
public class CalendarGuiController implements ActionListener {
  private final CalendarSettings settings;
  private final CalendarGuiViewInterface view;
  private GuiCalendar inUseCalendar;
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
    this.inUseCalendar = inUseCalendar;
    this.settings = settings;
    this.view = view;

    bindCommands();
    view.setCommandButtonListener(this);

    view.drawMonth(inUseCalendar.getMonth());
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
    CalendarGuiCommand cmd = commandMap.get(cmdName);

    if (cmd == null) {
      view.showError("Unknown command: " + cmdName);
      return;
    }

    cmd.run(calendarManager, inUseCalendar, view);
  }
}
