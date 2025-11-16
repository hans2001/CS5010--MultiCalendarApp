package calendar.controller.guicommands;

import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.view.CalendarGuiViewInterface;
import java.time.YearMonth;

/**
 * Move to next month.
 */
public class NextMonthCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarManager manager, GuiCalendarInterface inUseCalendar,
                  CalendarGuiViewInterface view) {
    YearMonth prev = inUseCalendar.getNextMonth();
    view.drawMonth(prev);
  }
}

