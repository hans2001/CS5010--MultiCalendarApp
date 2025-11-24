package calendar.controller.guicommands;

import calendar.controller.guicommands.CalendarGuiCommandContext;
import calendar.model.GuiCalendar;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.exception.NotFoundException;

/**
 * Selects a calendar from all calendars and sets it as the new one in use.
 */
public class SelectCalendarCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarGuiCommandContext context) {
    String name = context.view().getSelectedCalendarName();

    TimeZoneInMemoryCalendarInterface found;
    try {
      found = context.manager().getCalendar(name);
    } catch (NotFoundException e) {
      context.view().showError("Selected calendar not found: " + e.getMessage());
      return;
    }

    context.controller().setInUseCalendar(new GuiCalendar(found));
  }
}
