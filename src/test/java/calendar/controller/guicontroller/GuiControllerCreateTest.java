package calendar.controller.guicontroller;

import static org.junit.Assert.assertEquals;
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
public class GuiControllerCreateTest extends GuiControllerTest {
  /**
   * Test creating calendars.
   */
  @Test
  public void testCreateCalendar() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendar guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    view.nextNewCalendarResponse = new String[]{"School", "America/New_York"};
    Map<String, TimeZoneInMemoryCalendarInterface> calendars =
        calendarManager.getAllCalendars();
    assertTrue(calendars.containsKey("Default Calendar"));

    controller.actionPerformed(new ActionEvent(this, 0, "create-calendar"));
    calendars = calendarManager.getAllCalendars();

    // Check total number of calendars
    assertEquals(2, calendars.size());

    // Names are correct
    assertTrue(calendars.containsKey("Default Calendar"));
    assertTrue(calendars.containsKey("School"));

    // Correct Timezones
    assertEquals("America/New_York",
        calendars.get("Default Calendar").getZoneId().toString());
    assertEquals("America/New_York",
        calendars.get("School").getZoneId().toString());

    view.nextNewCalendarResponse = new String[]{"Africa", "Africa/Nairobi"};
    controller.actionPerformed(new ActionEvent(this, 0, "create-calendar"));
    calendars = calendarManager.getAllCalendars();
    assertEquals(3, calendars.size());
    assertTrue(calendars.containsKey("Africa"));
  }

  /**
   * Test creating invalid calendars.
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

    view.nextNewCalendarResponse = new String[]{"Default Calendar", "America/New_York"};


    controller.actionPerformed(new ActionEvent(this, 0, "create-calendar"));
    calendars = calendarManager.getAllCalendars();

    //Should have failed create
    assertEquals(1, calendars.size());
    assertTrue(view.lastError.startsWith("Could not create calendar: "
        + "Calendar with name 'Default Calendar' already exists"));

    view.nextNewCalendarResponse = new String[]{"Africa", "Africa/fake"};
    controller.actionPerformed(new ActionEvent(this, 0, "create-calendar"));
    calendars = calendarManager.getAllCalendars();
    assertEquals(1, calendars.size());
    assertTrue(view.lastError.startsWith("Could not create calendar: "
        + "Unsupported timezone: Africa/fake"));
  }

}