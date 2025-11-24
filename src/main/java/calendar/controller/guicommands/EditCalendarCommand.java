package calendar.controller.guicommands;

import calendar.controller.guicommands.CalendarGuiCommandContext;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.exception.NotFoundException;
import calendar.view.model.CalendarEditData;
import java.time.ZoneId;

/**
 * Edits the calendar for a new name or timezone.
 */
public class EditCalendarCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarGuiCommandContext context) {
    CalendarEditData editData = context.view().displayEditCalendar(
        context.currentCalendar().getName(),
        context.currentCalendar().getZoneId());
    String ogName = editData.originalName();
    String newName = editData.newName();
    String newTimeZone = editData.newTimezone();

    TimeZoneInMemoryCalendarInterface found;
    try {
      found = context.manager().getCalendar(ogName);
    } catch (NotFoundException e) {
      context.view().showError("Calendar not found: " + e.getMessage());
      return;
    }


    try {
      // Update the timezone if it changed.
      if (!found.getZoneId().toString().equals(newTimeZone)) {
        context.manager().editCalendarTimezone(ogName, ZoneId.of(newTimeZone));
        context.view().setActiveCalendarTimezone(newTimeZone);
      }

      if (!found.getName().equals(newName)) {
        context.manager().editCalendarName(ogName, newName);
        context.controller().renameKnownCalendar(ogName, newName);
        context.view().setActiveCalendarName(newName);
        context.view().editCalendarInSelector(ogName, newName);
      }

      context.controller().refreshActiveCalendar();
    } catch (Exception e) {
      context.view().showError("Error while editing: " + e.getMessage());
    }
  }
}
