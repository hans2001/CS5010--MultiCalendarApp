package calendar.controller.guicommands;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.view.CalendarGuiViewInterface;
import java.time.YearMonth;

/**
 * Selects a calendar from all calendars and sets it as the new one in use.
 */
public class SelectCalendarCommand implements CalendarGuiCommand {
  @Override
  public void run(CalendarManager manager, GuiCalendarInterface current,
                  CalendarGuiController controller,
                  CalendarGuiViewInterface view) {

    String name = view.getSelectedCalendarName();

    try {
      TimeZoneInMemoryCalendarInterface found = manager.getCalendar(name);

      view.setActiveCalendarName(name);
      view.setActiveCalendarTimezone(found.getZoneId().toString());
      view.drawMonth(YearMonth.now());

      controller.setInUseCalendar(found);
    } catch (Exception e) {
      view.showError("Error selecting: " + e.getMessage());
    }
  }
}

