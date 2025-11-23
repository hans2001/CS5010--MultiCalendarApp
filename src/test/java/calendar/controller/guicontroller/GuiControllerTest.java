package calendar.controller.guicontroller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarController;
import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.config.CalendarSettings;
import calendar.view.CalendarGuiViewInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Tests the gui controller.
 */
public class GuiControllerTest {
  /**
   * Mock view to help us debug. Stores info that would be displayed if
   * using the real view.
   */
  protected static class DummyView implements CalendarGuiViewInterface {
    ActionListener listener;

    boolean drawCalled = false;
    boolean visibleCalled = false;

    String activeName;
    String activeTz;

    // Selector calendars
    List<String> selectorCalendars = new ArrayList<>();

    String selectedCalendar;
    String lastError;
    String lastMessage;

    // Returned values for dialog prompts
    String[] nextNewCalendarResponse = null;
    String[] nextEditCalendarResponse = null;

    @Override
    public void setCommandButtonListener(ActionListener listener) {
      this.listener = listener;
    }

    @Override
    public void makeVisible() {
      visibleCalled = true;
    }

    @Override
    public void drawMonth(YearMonth month) {
      drawCalled = true;
    }

    @Override
    public void setActiveCalendarName(String name) {
      this.activeName = name;
    }

    @Override
    public void setActiveCalendarTimezone(String tz) {
      this.activeTz = tz;
    }

    @Override
    public void addCalendarToSelector(String name) {
      selectorCalendars.add(name);
    }

    @Override
    public void editCalendarInSelector(String ogName, String newName) {
      int idx = selectorCalendars.indexOf(ogName);
      if (idx >= 0) {
        selectorCalendars.set(idx, newName);
      }
    }

    @Override
    public void selectCalendarOnCalendarSelector(String name) {
      this.selectedCalendar = name;
    }

    @Override
    public String getSelectedCalendarName() {
      return this.selectedCalendar;
    }

    @Override
    public String[] displayEditCalendar(String calendarName, String calendarTz) {
      return nextEditCalendarResponse;
    }

    @Override
    public void showError(String message) {
      lastError = message;
    }

    @Override
    public void showMessage(String message) {
      lastMessage = message;
    }

    @Override
    public String[] promptNewCalendar() {
      return nextNewCalendarResponse;
    }
  }


  /**
   * Initializes the controller.
   *
   * @param view view.
   * @param guiCalendar model.
   * @param calendarManager manager of calendars.
   *
   * @return controller.
   */
  protected CalendarGuiController setUpController(
      DummyView view,
      GuiCalendarInterface guiCalendar,
      CalendarManager calendarManager
  ) {
    CalendarSettings settings = CalendarSettings.defaults();

    return new CalendarGuiController(settings, view, guiCalendar, calendarManager);
  }

  /**
   * Initializes the model.
   *
   * @param calendarManager of calendars.
   *
   * @return model.
   */
  protected GuiCalendarInterface setUpGui(CalendarManager calendarManager) {
    ZoneId systemZone = ZoneId.systemDefault();
    TimeZoneInMemoryCalendarInterface inUseCalendar =
        calendarManager.createCalendar("Default Calendar", systemZone.toString());

    return new GuiCalendar(inUseCalendar);
  }

  /**
   * Tests calendar functionalities.
   */
  @Test
  public void getCalendar() {
    CalendarSettings settings = CalendarSettings.defaults();
    DummyView view = new DummyView();

    CalendarManager calendarManager = new CalendarManager();
    ZoneId systemZone = ZoneId.systemDefault();
    TimeZoneInMemoryCalendarInterface inUseCalendar = calendarManager.createCalendar(
        "Default Calendar", systemZone.toString());
    GuiCalendar guiCalendar = new GuiCalendar(inUseCalendar);

    //view.makeVisible();
    //view.displayEditCalendar(guiCalendar.getName(), guiCalendar.getZoneId());
    //view.promptNewCalendar();

    CalendarGuiController controller = new CalendarGuiController(settings,
        view, guiCalendar, calendarManager);

    assertEquals("Default Calendar", view.activeName);
    String systemTz = ZoneId.systemDefault().toString();
    assertEquals(systemTz, view.activeTz);
    controller.actionPerformed(new ActionEvent(this, 0, "create-calendar"));
  }

  @Test
  public void testInitialization() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendarInterface guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    assertEquals("Default Calendar", view.activeName);
    assertEquals(ZoneId.systemDefault().toString(), view.activeTz);
  }

  /**
   * Test prev-month command.
   */
  @Test
  public void testPrevMonth() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendarInterface guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    YearMonth before = guiCalendar.getMonth();
    assertEquals(YearMonth.now(), before);

    YearMonth expected = before.minusMonths(1);
    controller.actionPerformed(new ActionEvent(this, 0, "prev-month"));
    assertEquals(expected, guiCalendar.getMonth());
  }

  /**
   * Test next-month command.
   */
  @Test
  public void testNextMonth() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendarInterface guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    YearMonth before = guiCalendar.getMonth();
    assertEquals(YearMonth.now(), before);

    YearMonth expected = before.plusMonths(1);
    controller.actionPerformed(new ActionEvent(this, 0, "next-month"));
    assertEquals(expected, guiCalendar.getMonth());
  }

  /**
   * Tests if we can combine next/prev months
   * and go past years to see if the months are valid still.
   */
  @Test
  public void testMovingMonths() {
    DummyView view = new DummyView();
    CalendarManager calendarManager = new CalendarManager();
    GuiCalendarInterface guiCalendar = setUpGui(calendarManager);
    CalendarGuiController controller = setUpController(view, guiCalendar, calendarManager);

    YearMonth start = guiCalendar.getMonth();

    YearMonth expected = start;
    //Go back 14 months
    for (int i = 0; i < 14; i++) {
      expected = expected.minusMonths(1);
      controller.actionPerformed(new ActionEvent(this, 0, "prev-month"));

      assertEquals(expected, guiCalendar.getMonth());
    }

    //Go forward 14 months
    for (int i = 0; i < 14; i++) {
      expected = expected.plusMonths(1);

      controller.actionPerformed(new ActionEvent(this, 0, "next-month"));

      assertEquals(expected, guiCalendar.getMonth());
    }

    // Final state we roll everything back.
    assertEquals(start, guiCalendar.getMonth());
  }
}
