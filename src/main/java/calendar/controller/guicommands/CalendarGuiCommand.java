package calendar.controller.guicommands;

import calendar.controller.guicommands.CalendarGuiCommandContext;

/**
 * Commands run by the GUI.
 */
public interface CalendarGuiCommand {
  /**
   * Runs the command.
   *
   * @param context bundled controller/manager/view/calendar state.
   */
  void run(CalendarGuiCommandContext context);
}
