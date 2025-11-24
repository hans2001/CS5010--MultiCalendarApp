package calendar.controller.guicontroller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarController;
import calendar.controller.CalendarGuiController;
import calendar.controller.service.EventCreationRequest;
import calendar.controller.service.EventEditRequest;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.GuiCalendarInterface;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.config.CalendarSettings;
import calendar.view.CalendarGuiFeatures;
import calendar.view.CalendarGuiViewInterface;
import calendar.view.model.CalendarCreationData;
import calendar.view.model.CalendarEditData;
import calendar.view.model.GuiEventSummary;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    CalendarGuiFeatures features;

    boolean drawCalled = false;
    boolean visibleCalled = false;

    String activeName;
    String activeTz;

    List<String> selectorCalendars = new ArrayList<>();
    String selectedCalendar;

    String lastError;
    String lastMessage;

    String[] nextNewCalendarResponse = null;   // {name, tz}
    String[] nextEditCalendarResponse = null;  // {ogName, newName, newTz}

    @Override
    public void makeVisible() {
      visibleCalled = true;
    }

    @Override
    public void drawMonth(YearMonth month) {
      drawCalled = true;
    }

    @Override
    public void setSelectedDate(LocalDate date) {
    }

    @Override
    public void displayEvents(LocalDate date, List<GuiEventSummary> events) {
    }

    @Override
    public Optional<EventCreationRequest> promptForCreateEvent(LocalDate date) {
      return Optional.empty();
    }

    @Override
    public Optional<EventEditRequest> promptForEditEvent(GuiEventSummary summary) {
      return Optional.empty();
    }

    @Override
    public void setFeatures(CalendarGuiFeatures features) {
      this.features = features;
    }

    @Override
    public void showError(String message) {
      this.lastError = message;
    }

    @Override
    public void showMessage(String message) {
      this.lastMessage = message;
    }

    // calendar creation
    @Override
    public Optional<CalendarCreationData> promptNewCalendar() {
      if (nextNewCalendarResponse == null) {
        return Optional.empty();
      }
      return Optional.of(new CalendarCreationData(
          nextNewCalendarResponse[0],
          nextNewCalendarResponse[1]
      ));
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
    public CalendarEditData displayEditCalendar(String calendarName, String calendarTz) {
      if (nextEditCalendarResponse == null) {
        return new CalendarEditData(calendarName, calendarName, calendarTz);
      }

      return new CalendarEditData(
          nextEditCalendarResponse[0],  // original name
          nextEditCalendarResponse[1],  // new name
          nextEditCalendarResponse[2]   // new timezone
      );
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
      GuiCalendar guiCalendar,
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
  protected GuiCalendar setUpGui(CalendarManager calendarManager) {
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
    GuiCalendar guiCalendar = setUpGui(calendarManager);
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
    GuiCalendar guiCalendar = setUpGui(calendarManager);
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
    GuiCalendar guiCalendar = setUpGui(calendarManager);
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
    GuiCalendar guiCalendar = setUpGui(calendarManager);
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