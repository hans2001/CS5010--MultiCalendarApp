package calendar.controller.guicommands;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.view.CalendarGuiViewInterface;

/**
 * Commands run by the GUI.
 */
public interface CalendarGuiCommand {
  /**
   * Runs the command.
   *
   * @param manager calendar manager.
   * @param inUseCalendar the current calendar in use.
   * @param controller gui controller.
   * @param view the view.
   */
  void run(CalendarManager manager,
           GuiCalendarInterface inUseCalendar,
           CalendarGuiController controller,
           CalendarGuiViewInterface view);
}
