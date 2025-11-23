package calendar.controller.guicommands;

import calendar.controller.guicommands.CalendarGuiCommandContext;
import calendar.view.model.CalendarCreationData;

/**
 * Command for creating a new calendar.
 */
public class CreateCalendarCommand implements CalendarGuiCommand {

  @Override
  public void run(CalendarGuiCommandContext context) {
    CalendarCreationData data = context.view().promptNewCalendar().orElse(null);
    if (data == null) {
      return;
    }

    String name = data.name();
    String tz = data.timezone();

    try {
      context.manager().createCalendar(name, tz);
      context.view().showMessage("Created calendar with name \"" + name + "\" with timezone " + tz);
      context.controller().registerCalendarName(name);
    } catch (Exception e) {
      context.view().showError("Could not create calendar: " + e.getMessage());
    }
  }
}
