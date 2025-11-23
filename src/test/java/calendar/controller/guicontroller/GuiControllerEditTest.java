package calendar.controller.guicontroller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import java.awt.event.ActionEvent;
import java.util.Map;
import org.junit.Test;

/**
 * Create tests for the gui controller.
 */
public class GuiControllerEditTest extends GuiControllerTest {
  /**
   * Test editing calendars.
   */
  @Test
  public void testEditCalendar() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendarInterface guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    Map<String, TimeZoneInMemoryCalendarInterface> calendars =
        calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));

    view.nextEditCalendarResponse = new String[]{"Default Calendar",
        "New Name", "America/New_York"};
    controller.actionPerformed(new ActionEvent(this, 0, "edit-calendar"));
    calendars = calendarManager.getAllCalendars();

    assertEquals(1, calendars.size());
    assertFalse(calendars.containsKey("Default Calendar"));
    assertTrue(calendars.containsKey("New Name"));

    assertEquals("America/New_York",
        calendars.get("New Name").getZoneId().toString());

    view.nextEditCalendarResponse = new String[]{"New Name", "New Name", "Africa/Nairobi"};
    controller.actionPerformed(new ActionEvent(this, 0, "edit-calendar"));
    calendars = calendarManager.getAllCalendars();

    assertEquals(1, calendars.size());
    assertFalse(calendars.containsKey("Default Calendar"));
    assertTrue(calendars.containsKey("New Name"));
    assertNotEquals("America/New_York",
        calendars.get("New Name").getZoneId().toString());
    assertEquals("Africa/Nairobi",
        calendars.get("New Name").getZoneId().toString());
  }

  /**
   * Test invalid editing calendars.
   */
  @Test
  public void testInvalidEditCalendar() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendarInterface guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    Map<String, TimeZoneInMemoryCalendarInterface> calendars =
        calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));

    view.nextNewCalendarResponse = new String[]{"Default Calendar2", "America/New_York"};
    controller.actionPerformed(new ActionEvent(this, 0, "create-calendar"));

    view.nextEditCalendarResponse = new String[]{"Default Calendar",
        "Default Calendar2", "America/New_York"};
    controller.actionPerformed(new ActionEvent(this, 0, "edit-calendar"));
    assertEquals("Error while editing: Calendar "
        + "with name 'Default Calendar2' already exists", view.lastError);
    calendars = calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));
    assertEquals("America/New_York",
        calendars.get("Default Calendar").getZoneId().toString());
    assertEquals(2, calendars.size());

    view.nextEditCalendarResponse = new String[]{"New Name", "New Name", "Africa/Nairobi"};
    controller.actionPerformed(new ActionEvent(this, 0, "edit-calendar"));
    assertEquals("Error while editing: Calendar "
        + "'New Name' not found", view.lastError);
    calendars = calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));
    assertEquals(2, calendars.size());
    assertEquals("America/New_York",
        calendars.get("Default Calendar").getZoneId().toString());

    view.nextEditCalendarResponse = new String[]{"Default Calendar", "Default Calendar", "fake"};
    controller.actionPerformed(new ActionEvent(this, 0, "edit-calendar"));
    assertEquals("Error while editing: "
        + "Unknown time-zone ID: fake", view.lastError);
    calendars = calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));
    assertEquals("America/New_York",
        calendars.get("Default Calendar").getZoneId().toString());
    assertEquals(2, calendars.size());
  }
}
