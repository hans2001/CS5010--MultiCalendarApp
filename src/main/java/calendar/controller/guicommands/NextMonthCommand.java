package calendar.controller.guicommands;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.view.CalendarGuiViewInterface;
import java.time.YearMonth;

/**
 * Move to next month.
 */
public class NextMonthCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarManager manager, GuiCalendarInterface current,
                  CalendarGuiController controller,
                  CalendarGuiViewInterface view) {
    YearMonth next = current.getNextMonth();
    controller.onMonthChanged(next);
  }
}
