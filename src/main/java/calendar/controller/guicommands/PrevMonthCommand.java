package calendar.controller.guicommands;

import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.view.CalendarGuiView;
import calendar.view.CalendarGuiViewInterface;
import java.time.YearMonth;

/**
 * Move to prev month.
 */
public class PrevMonthCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarManager manager,
                  GuiCalendarInterface inUseCalendar, CalendarGuiViewInterface view) {
    YearMonth prev = inUseCalendar.getPreviousMonth();
    view.drawMonth(prev);
  }
}

