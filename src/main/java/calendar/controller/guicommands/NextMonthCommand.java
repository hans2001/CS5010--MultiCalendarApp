package calendar.controller.guicommands;

import calendar.controller.guicommands.CalendarGuiCommandContext;
import java.time.YearMonth;

/**
 * Move to next month.
 */
public class NextMonthCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarGuiCommandContext context) {
    YearMonth next = context.currentCalendar().getNextMonth();
    context.controller().onMonthChanged(next);
  }
}
