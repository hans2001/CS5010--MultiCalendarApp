package calendar.controller.guicommands;

import calendar.controller.guicommands.CalendarGuiCommandContext;
import java.time.YearMonth;

/**
 * Move to prev month.
 */
public class PrevMonthCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarGuiCommandContext context) {
    YearMonth previous = context.currentCalendar().getPreviousMonth();
    context.controller().onMonthChanged(previous);
  }
}
