package calendar.controller.guicommands;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.view.CalendarGuiViewInterface;

/**
 * Command for creating a new calendar.
 */
public class CreateCalendarCommand implements CalendarGuiCommand {

  @Override
  public void run(CalendarManager manager, GuiCalendarInterface current,
                  CalendarGuiController controller,
                  CalendarGuiViewInterface view) {

    String[] data = view.promptNewCalendar();
    if (data == null) {
      return;
    }

    String name = data[0];
    String tz = data[1];

    try {
      manager.createCalendar(name, tz);
      view.showMessage("Created calendar with name \""
          +
          name + "\" with timezone " + tz);
      view.addCalendarToSelector(name);
    } catch (Exception e) {
      view.showError("Could not create calendar: " + e.getMessage());
    }
  }
}
