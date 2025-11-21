package calendar.controller.guicommands;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.view.CalendarGuiViewInterface;
import java.time.YearMonth;

/**
 * Move to prev month.
 */
public class PrevMonthCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarManager manager,
                  GuiCalendarInterface current,
                  CalendarGuiController controller,
                  CalendarGuiViewInterface view) {
    YearMonth previous = current.getPreviousMonth();
    controller.onMonthChanged(previous);
  }
}
