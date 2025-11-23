package calendar.controller.guicommands;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
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
   * @param current the current calendar in use.
   * @param view the view.
   */
  @Override
  public void run(CalendarManager manager, GuiCalendarInterface current,
                  CalendarGuiController controller,
                  CalendarGuiViewInterface view) {
    String[] ret = view.displayEditCalendar(
        current.getName(),
        current.getZoneId());
    String ogName = ret[0];
    String newName = ret[1];
    String newTimeZone = ret[2];

    try {
      TimeZoneInMemoryCalendarInterface found = manager.getCalendar(ogName);
      //Update the Timezone (if changed)
      if (!found.getZoneId().toString().equals(newTimeZone)) {
        try {
          manager.editCalendarTimezone(ogName, ZoneId.of(newTimeZone));
          view.setActiveCalendarTimezone(newTimeZone);
        } catch (Exception e) {
          view.showError("Error while editing: " + e.getMessage());
        }
      }

      //Update the name (if changed)
      if (!found.getName().equals(newName)) {
        try {
          manager.editCalendarName(ogName, newName);
          view.setActiveCalendarName(newName);
          view.editCalendarInSelector(ogName, newName);
        } catch (Exception e) {
          view.showError("Error while editing: " + e.getMessage());
        }
      }
    } catch (Exception e) {
      view.showError("Error while editing: " + e.getMessage());
    }
  }
}
