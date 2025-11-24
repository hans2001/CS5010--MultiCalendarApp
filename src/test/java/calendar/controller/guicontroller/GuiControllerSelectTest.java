package calendar.controller.guicontroller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import java.awt.event.ActionEvent;
import java.util.Map;
import org.junit.Test;

/**
 * Create tests for the gui controller.
 */
public class GuiControllerSelectTest extends GuiControllerTest {
  /**
   * Test selecting calendars.
   */
  @Test
  public void testSelectCalendar() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendar guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    view.nextNewCalendarResponse = new String[]{"School", "America/Los_Angeles"};
    Map<String, TimeZoneInMemoryCalendarInterface> calendars =
        calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));

    controller.actionPerformed(new ActionEvent(this, 0, "create-calendar"));
    view.selectorCalendars.add("School");
    assertEquals(2, calendars.size());

    // Names are correct
    assertTrue(calendars.containsKey("Default Calendar"));
    assertTrue(calendars.containsKey("School"));

    // Correct Timezones
    assertEquals("America/New_York",
        calendars.get("Default Calendar").getZoneId().toString());
    assertEquals("America/Los_Angeles",
        calendars.get("School").getZoneId().toString());

    assertEquals("Default Calendar", guiCalendar.getName());
    assertEquals("America/New_York", guiCalendar.getZoneId());

    view.selectedCalendar = "Default Calendar";
    controller.actionPerformed(new ActionEvent(this, 0, "select-calendar"));
    assertEquals("Default Calendar", guiCalendar.getName());
    assertEquals("America/New_York", guiCalendar.getZoneId());

    view.selectedCalendar = "School";
    controller.actionPerformed(new ActionEvent(this, 0, "select-calendar"));
    assertEquals("School", controller.getName());
    assertEquals("America/Los_Angeles", controller.getZoneId());

    view.nextEditCalendarResponse = new String[]{"Default Calendar",
        "Default Calendar2", "Africa/Nairobi"};
    controller.actionPerformed(new ActionEvent(this, 0, "edit-calendar"));
    assertFalse(calendars.containsKey("Default Calendar"));
    assertTrue(calendars.containsKey("Default Calendar2"));
    assertEquals("Africa/Nairobi",
        calendars.get("Default Calendar2").getZoneId().toString());

    view.selectedCalendar = "Default Calendar2";
    controller.actionPerformed(new ActionEvent(this, 0, "select-calendar"));
    assertEquals("Default Calendar2", controller.getName());
    assertEquals("Africa/Nairobi", controller.getZoneId());
  }

  /**
   * Test selecting invalid calendars.
   */
  @Test
  public void testInvalidCreateCalendar() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendar guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);
    Map<String, TimeZoneInMemoryCalendarInterface> calendars =
        calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));

    view.selectedCalendar = "School";
    controller.actionPerformed(new ActionEvent(this, 0, "select-calendar"));
    assertEquals("Selected calendar not found: "
        + "Calendar 'School' not found", view.lastError);
  }
}