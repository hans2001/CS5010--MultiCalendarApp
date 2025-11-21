package calendar.controller.guicommands;

import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.exception.NotFoundException;
import calendar.view.CalendarGuiViewInterface;
import java.time.ZoneId;

/**
 * Edits the calendar for a new name or timezone.
 */
public class EditCalendarCommand implements CalendarGuiCommand {
  /**
   * Grabs the user changes and updates the calendar.
   *
   * @param manager calendar manager.
   * @param inUseCalendar the current calendar in use.
   * @param view the view.
   */
  @Override
  public void run(CalendarManager manager, GuiCalendarInterface inUseCalendar,
                  CalendarGuiViewInterface view) {
    String[] ret = view.displayEditCalendar(
        inUseCalendar.getName(),
        inUseCalendar.getZoneId().toString());
    String ogName = ret[0];
    String newName = ret[1];
    String newTimeZone = ret[2];

    boolean sucessfulNameChange = false;

    TimeZoneInMemoryCalendarInterface found = manager.getCalendar(ogName);
    if (!found.getName().equals(newName)) {
      try {
        manager.editCalendarName(ogName, newName);
        sucessfulNameChange = true;
      } catch (NotFoundException e) {
        view.showError("Edit Error " + e.getMessage());
      }
    }

    ZoneId updatedZone = ZoneId.of(newTimeZone);
    if (!found.getZoneId().equals(ZoneId.of(newTimeZone))) {
      if (sucessfulNameChange) {
        manager.editCalendarTimezone(newName, updatedZone);
      } else {
        manager.editCalendarTimezone(ogName, updatedZone);
      }
    }

    //Update calendar name + timezone (only if already selected)

    //Update dropdown list (if the name changed only)
    
  }
}
