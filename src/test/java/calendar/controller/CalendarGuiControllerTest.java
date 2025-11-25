package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.controller.guicommands.CalendarGuiCommandContext;
import calendar.controller.guicommands.CreateCalendarCommand;
import calendar.controller.guicommands.EditCalendarCommand;
import calendar.controller.guicommands.NextMonthCommand;
import calendar.controller.guicommands.PrevMonthCommand;
import calendar.controller.guicommands.SelectCalendarCommand;
import calendar.controller.service.EventCreationRequest;
import calendar.controller.service.EventEditRequest;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.api.EditScope;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.view.CalendarGuiFeatures;
import calendar.view.CalendarGuiViewInterface;
import calendar.view.model.CalendarCreationData;
import calendar.view.model.CalendarEditData;
import calendar.view.model.GuiEventSummary;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

/**
 * Controller-level tests that exercise the GUI features without triggering Swing events.
 *
 * <p>The tests use the real {@link CalendarManager} and {@link TimeZoneInMemoryCalendarInterface}
 * so that business logic matches the CLI implementation, but swap in a lightweight stub view to
 * capture user prompts/messages.</p>
 */
public final class CalendarGuiControllerTest {
  private CalendarManager manager;
  private TimeZoneInMemoryCalendarInterface modelCalendar;
  private GuiCalendar guiCalendar;
  private RecordingView view;
  private CalendarGuiController controller;

  /** Prepares a fresh controller and recording view for each test. */
  @Before
  public void setUp() {
    manager = new CalendarManager();
    modelCalendar = manager.createCalendar("Default Calendar", "America/New_York");
    guiCalendar = new GuiCalendar(modelCalendar);
    view = new RecordingView();
    controller = new CalendarGuiController(
        CalendarSettings.defaults(),
        view,
        guiCalendar,
        manager);
  }

  private CalendarGuiCommandContext commandContext() {
    return new CalendarGuiCommandContext(manager, guiCalendar, controller, view);
  }

  @Test
  public void requestEventCreation_createsEventAndRefreshesView() {
    LocalDate targetDate = LocalDate.of(2025, 5, 10);
    controller.daySelected(targetDate);

    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Demo")
        .startDateTime(LocalDateTime.of(targetDate, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(targetDate, LocalTime.of(10, 0)))
        .build());

    controller.requestEventCreation();

    assertEquals("Event should be created in the active calendar",
        1, modelCalendar.allEvents().size());
    assertTrue("Success message propagated to the view",
        view.messages.contains("Event created successfully."));
    assertEquals("Events were refreshed for the selected date",
        targetDate, view.lastDisplayedDate);
    assertEquals("Displayed list reflects the newly created event",
        1, view.lastDisplayedEvents.size());
  }

  @Test
  public void controllerInitializationPopulatesView() {
    assertTrue("View should receive controller callbacks via setFeatures", view.featuresSet);
    assertEquals(Collections.singletonList("Default Calendar"), view.addedCalendars);
    assertEquals("Default Calendar", view.selectedCalendar);
    assertEquals("Default Calendar", view.activeCalendarName);
    assertEquals("America/New_York", view.activeCalendarTimezone);
    assertEquals("Constructor should draw initial month once", 1, view.drawnMonths.size());
    assertEquals(guiCalendar.getMonth().atDay(1), view.selectedDate);
    assertEquals("Refresh events should run on startup", view.selectedDate, view.lastDisplayedDate);
    assertTrue("Selector should be synced on startup", view.selectCalendarCalls >= 1);
    assertTrue("Selected date setter should be invoked", view.setSelectedDateCalls >= 1);
    assertTrue("Events should be displayed on startup", view.displayEventsCalls >= 1);
  }

  @Test
  public void monthNavigationInvokesCommands() {
    final int initialDraws = view.drawnMonths.size();
    new NextMonthCommand().run(commandContext());
    YearMonth afterNext = guiCalendar.getMonth();
    assertEquals(afterNext.atDay(1), view.lastDisplayedDate);

    new PrevMonthCommand().run(commandContext());
    YearMonth afterPrev = guiCalendar.getMonth();
    assertEquals(afterPrev.atDay(1), view.lastDisplayedDate);

    assertTrue(view.drawnMonths.size() >= initialDraws + 2);
  }

  @Test
  public void registerCalendarNameAddsOnlyOnce() {
    controller.registerCalendarName("Team");
    controller.registerCalendarName("Team");
    long occurrences = view.addedCalendars.stream()
        .filter(name -> name.equals("Team"))
        .count();
    assertEquals(1, occurrences);
  }

  @Test
  public void createCalendarCommandCreatesAndRegistersCalendar() {
    CreateCalendarCommand cmd = new CreateCalendarCommand();
    view.nextNewCalendarPrompt = Optional.of(new CalendarCreationData("Team", "America/Chicago"));
    cmd.run(commandContext());

    assertTrue(manager.hasCalendar("Team"));
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Created calendar")));
    assertTrue(view.addedCalendars.contains("Team"));
  }

  @Test
  public void requestCalendarCreationDelegatesToCommand() {
    view.nextNewCalendarPrompt = Optional.of(
        new CalendarCreationData("ViaRequest", "America/Chicago"));
    controller.requestCalendarCreation();
    assertTrue(manager.hasCalendar("ViaRequest"));
  }

  @Test
  public void createCalendarCommandGracefullyHandlesConflict() {
    CreateCalendarCommand cmd = new CreateCalendarCommand();
    view.nextNewCalendarPrompt = Optional.of(
        new CalendarCreationData("Default Calendar", "America/New_York"));
    cmd.run(commandContext());

    assertTrue("Error message shown for duplicate name", view.errors.stream()
        .anyMatch(err -> err.contains("Could not create calendar")));
  }

  @Test
  public void requestCalendarEditDelegatesToCommand() {
    view.nextEditCalendarResponse = new CalendarEditData(
        "Default Calendar", "ReqEdit", "America/Denver");
    controller.requestCalendarEdit();
    assertTrue(manager.hasCalendar("ReqEdit"));
    assertEquals("ReqEdit", view.activeCalendarName);
    assertEquals("America/Denver", view.activeCalendarTimezone);
  }

  @Test
  public void selectCalendarCommandSwitchesActiveCalendar() {
    manager.createCalendar("Team", "America/Chicago");
    view.selectedCalendar = "Team";
    new SelectCalendarCommand().run(commandContext());
    assertEquals("Team", view.activeCalendarName);
    assertEquals("America/Chicago", view.activeCalendarTimezone);
  }

  @Test
  public void calendarSelectedUsesCurrentViewSelection() {
    manager.createCalendar("Team", "America/Chicago");
    view.selectedCalendar = "Team";
    controller.calendarSelected("ignored");
    assertEquals("Team", view.activeCalendarName);
    assertEquals("America/Chicago", view.activeCalendarTimezone);
  }

  @Test
  public void editCalendarCommandUpdatesNameAndTimezone() {
    final int drawsBefore = view.drawnMonths.size();
    view.nextEditCalendarResponse =
        new CalendarEditData("Default Calendar", "Renamed", "America/Chicago");
    new EditCalendarCommand().run(commandContext());

    assertTrue(manager.hasCalendar("Renamed"));
    assertEquals("Renamed", view.activeCalendarName);
    assertEquals("America/Chicago", view.activeCalendarTimezone);
    assertTrue(view.selectorEdits.contains("Default Calendar->Renamed"));
    final long before = view.addedCalendars.stream()
        .filter(name -> name.equals("Renamed"))
        .count();
    controller.registerCalendarName("Renamed");
    final long after = view.addedCalendars.stream()
        .filter(name -> name.equals("Renamed"))
        .count();
    assertEquals("Rename should update known calendar cache", before, after);
    assertTrue("refreshActiveCalendar redraws month",
        view.drawnMonths.size() > drawsBefore);
    // Known calendars should drop old name and include new name.
    try {
      Field f = CalendarGuiController.class.getDeclaredField("knownCalendars");
      f.setAccessible(true);
      @SuppressWarnings("unchecked")
      java.util.Set<String> known = (java.util.Set<String>) f.get(controller);
      assertTrue(known.contains("Renamed"));
      assertFalse(known.contains("Default Calendar"));
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Reflection failed", e);
    }
  }

  @Test
  public void setInUseCalendarUpdatesViewState() {
    manager.createCalendar("Secondary", "America/Chicago");
    GuiCalendar newGui = new GuiCalendar(manager.getCalendar("Secondary"));
    final int beforeDisplay = view.displayEventsCalls;
    final int beforeSelectCalls = view.setSelectedDateCalls;
    final int beforeSelectCalendar = view.selectCalendarCalls;

    controller.setInUseCalendar(newGui);

    assertEquals("Secondary", view.activeCalendarName);
    assertEquals("America/Chicago", view.activeCalendarTimezone);
    assertTrue(view.addedCalendars.contains("Secondary"));
    assertEquals("Secondary", view.selectedCalendar);
    assertEquals(newGui.getMonth().atDay(1), view.selectedDate);
    assertTrue(view.drawnMonths.contains(newGui.getMonth()));
    assertEquals(view.selectedDate, view.lastDisplayedDate);
    assertTrue(view.displayEventsCalls > beforeDisplay);
    assertTrue(view.setSelectedDateCalls > beforeSelectCalls);
    assertTrue(view.selectCalendarCalls > beforeSelectCalendar);
  }

  @Test
  public void goToNextMonthUsesBoundCommand() {
    final int before = view.drawnMonths.size();
    final int beforeSelect = view.setSelectedDateCalls;
    final int beforeDisplay = view.displayEventsCalls;
    controller.goToNextMonth();
    assertTrue(view.errors.isEmpty());
    assertTrue(view.drawnMonths.size() > before);
    assertTrue("setSelectedDate should be called on navigation",
        view.setSelectedDateCalls > beforeSelect);
    assertTrue("displayEvents should refresh on navigation",
        view.displayEventsCalls > beforeDisplay);
  }

  @Test
  public void unknownCommandShowsError() throws Exception {
    Field f = CalendarGuiController.class.getDeclaredField("commandMap");
    f.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, calendar.controller.guicommands.CalendarGuiCommand> map =
        (java.util.Map<String, calendar.controller.guicommands.CalendarGuiCommand>) f.get(
            controller);
    final java.util.Map<String, calendar.controller.guicommands.CalendarGuiCommand> backup =
        new java.util.HashMap<>(map);
    map.clear();
    controller.goToPreviousMonth();
    assertTrue(view.errors.stream().anyMatch(msg -> msg.contains("Unknown command")));
    map.putAll(backup);
  }

  @Test
  public void handleCreateEventErrorsWhenNoDate() throws Exception {
    Field f = CalendarGuiController.class.getDeclaredField("selectedDate");
    f.setAccessible(true);
    f.set(controller, null);

    controller.requestEventCreation();
    assertTrue(view.errors.stream().anyMatch(e -> e.contains("Select a date first")));
  }

  @Test
  public void handleCreateEventValidationError() {
    LocalDate date = LocalDate.of(2025, 8, 1);
    controller.daySelected(date);
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Bad")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(11, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0))) // end before start
        .build());
    controller.requestEventCreation();
    assertTrue(view.errors.stream().anyMatch(msg -> msg.contains("Fields are invalid")));
  }

  @Test
  public void handleCreateEventConflictError() {
    LocalDate date = LocalDate.of(2025, 10, 10);
    controller.daySelected(date);
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Conflict")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();

    // duplicate slot triggers ConflictException branch
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Conflict")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();
    assertTrue(view.errors.stream().anyMatch(msg -> msg.contains("Event conflict")));
  }

  @Test
  public void handleEditEventInvalidPropertyError() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    controller.daySelected(date);
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("X")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();
    Event existing = modelCalendar.allEvents().get(0);
    GuiEventSummary summary = new GuiEventSummary(
        existing.subject(), existing.start(), existing.end(),
        existing.description().orElse(""), existing.location().orElse(""), existing.status());

    // invalid status value should throw IllegalArgumentException in patch factory
    view.nextEditRequest = Optional.of(new EventEditRequest.Builder()
        .property(EditProperty.STATUS)
        .subject(existing.subject())
        .start(existing.start())
        .end(existing.end())
        .newValue("NOT_A_STATUS")
        .scope(EditScope.SINGLE)
        .build());

    controller.requestEventEdit(summary);
    assertTrue(view.errors.stream().anyMatch(msg -> msg.contains("NOT_A_STATUS")));
  }

  @Test
  public void handleEditEventValidationError() {
    LocalDate date = LocalDate.of(2025, 9, 1);
    controller.daySelected(date);
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("EditMe")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();
    Event existing = modelCalendar.allEvents().get(0);
    GuiEventSummary summary = new GuiEventSummary(
        existing.subject(), existing.start(), existing.end(),
        existing.description().orElse(""), existing.location().orElse(""), existing.status());

    view.nextEditRequest = Optional.of(new EventEditRequest.Builder()
        .property(EditProperty.END)
        .subject(existing.subject())
        .start(existing.start())
        .end(existing.end())
        .newValue(existing.start().minusMinutes(30).toString()) // end before start
        .scope(EditScope.SINGLE)
        .build());

    controller.requestEventEdit(summary);
    assertFalse("Validation failure should surface an error", view.errors.isEmpty());
    // ensure the event did not change
    Event still = modelCalendar.allEvents().get(0);
    assertEquals(existing.start(), still.start());
    assertEquals(existing.end(), still.end());
  }

  @Test
  public void requestEventEdit_updatesEventStartAndDuration() {
    LocalDate date = LocalDate.of(2025, 6, 5);
    controller.daySelected(date);
    // Seed an event.
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();

    Event existing = modelCalendar.allEvents().get(0);
    GuiEventSummary summary = new GuiEventSummary(
        existing.subject(),
        existing.start(),
        existing.end(),
        existing.description().orElse(""),
        existing.location().orElse(""),
        existing.status());

    view.nextEditRequest = Optional.of(new EventEditRequest.Builder()
        .property(EditProperty.START)
        .subject(existing.subject())
        .start(existing.start())
        .end(existing.end())
        .newValue(existing.start().plusMinutes(30).toString())
        .scope(EditScope.SINGLE)
        .build());

    view.lastDisplayedDate = null;
    controller.requestEventEdit(summary);

    Event updated = modelCalendar.allEvents().get(0);
    assertEquals("Start time should shift to requested value",
        LocalDateTime.of(date, LocalTime.of(9, 30)), updated.start());
    assertEquals("Duration preserved when editing start time",
        LocalDateTime.of(date, LocalTime.of(10, 30)), updated.end());
    assertTrue(view.messages.contains("Event updated successfully."));
    assertEquals("refreshEvents invoked after edit", date, view.lastDisplayedDate);
    assertTrue("displayEvents called after edit", view.displayEventsCalls > 0);
  }

  @Test
  public void timezoneChangesEventTimeTest() {
    LocalDate date = LocalDate.of(2025, 6, 5);
    controller.daySelected(date);

    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();

    view.nextEditCalendarResponse = new CalendarEditData(
        "Default Calendar", "ReqEdit", "America/Los_Angeles");
    controller.requestCalendarEdit();

    Event existing = modelCalendar.allEvents().get(0);
    assertEquals("Workshop", existing.subject());
    assertEquals("2025-06-05T06:00", existing.start().toString());
    assertEquals("2025-06-05T07:00", existing.end().toString());
  }

  @Test
  public void daySelectedRefreshesView() {
    int beforeSelect = view.setSelectedDateCalls;
    int beforeDisplay = view.displayEventsCalls;
    LocalDate date = LocalDate.of(2025, 7, 4);
    controller.daySelected(date);
    assertTrue(view.setSelectedDateCalls > beforeSelect);
    assertTrue(view.displayEventsCalls > beforeDisplay);
    assertEquals(date, view.lastDisplayedDate);
  }

  @Test
  public void secondCreateSameSlotProducesConflictError() {
    LocalDate date = LocalDate.of(2025, 10, 10);
    controller.daySelected(date);
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Conflict")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();

    // attempt duplicate
    view.nextCreationRequest = Optional.of(new EventCreationRequest.Builder()
        .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
        .subject("Conflict")
        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
        .build());
    controller.requestEventCreation();

    assertTrue(view.errors.stream().anyMatch(msg -> msg.contains("Event conflict")));
  }

  /**
   * Lightweight stub of {@link CalendarGuiViewInterface} that records interactions and provides
   * canned responses for prompts. This keeps the tests free of Swing dependencies.
   */
  private static final class RecordingView implements CalendarGuiViewInterface {
    private CalendarGuiFeatures features;
    Optional<EventCreationRequest> nextCreationRequest = Optional.empty();
    Optional<EventEditRequest> nextEditRequest = Optional.empty();
    Optional<CalendarCreationData> nextNewCalendarPrompt = Optional.empty();
    CalendarEditData nextEditCalendarResponse;
    final List<String> messages = new ArrayList<>();
    final List<String> errors = new ArrayList<>();
    LocalDate lastDisplayedDate;
    List<GuiEventSummary> lastDisplayedEvents = Collections.emptyList();
    boolean featuresSet = false;
    final List<String> addedCalendars = new ArrayList<>();
    final List<String> selectorEdits = new ArrayList<>();
    final List<java.time.YearMonth> drawnMonths = new ArrayList<>();
    final List<String> timezoneUpdates = new ArrayList<>();
    final List<String> nameUpdates = new ArrayList<>();
    int selectCalendarCalls = 0;
    int setSelectedDateCalls = 0;
    int displayEventsCalls = 0;
    String selectedCalendar = "Default Calendar";
    LocalDate selectedDate;
    String activeCalendarName;
    String activeCalendarTimezone;

    @Override
    public void makeVisible() {
      // No-op for tests.
    }

    @Override
    public void drawMonth(java.time.YearMonth month) {
      drawnMonths.add(month);
    }

    @Override
    public void setSelectedDate(LocalDate date) {
      this.selectedDate = date;
      setSelectedDateCalls++;
    }

    @Override
    public void displayEvents(LocalDate date, List<GuiEventSummary> events) {
      lastDisplayedDate = date;
      lastDisplayedEvents = events;
      displayEventsCalls++;
    }

    @Override
    public Optional<EventCreationRequest> promptForCreateEvent(LocalDate date) {
      Optional<EventCreationRequest> result = nextCreationRequest;
      nextCreationRequest = Optional.empty();
      return result;
    }

    @Override
    public Optional<EventEditRequest> promptForEditEvent(GuiEventSummary summary) {
      Optional<EventEditRequest> result = nextEditRequest;
      nextEditRequest = Optional.empty();
      return result;
    }

    @Override
    public void setFeatures(CalendarGuiFeatures features) {
      this.features = features;
      this.featuresSet = true;
    }

    @Override
    public void showError(String message) {
      errors.add(message);
    }

    @Override
    public void showMessage(String message) {
      messages.add(message);
    }

    @Override
    public Optional<CalendarCreationData> promptNewCalendar() {
      Optional<CalendarCreationData> result = nextNewCalendarPrompt;
      nextNewCalendarPrompt = Optional.empty();
      return result;
    }

    @Override
    public void setActiveCalendarName(String name) {
      activeCalendarName = name;
      nameUpdates.add(name);
    }

    @Override
    public void setActiveCalendarTimezone(String tz) {
      activeCalendarTimezone = tz;
      timezoneUpdates.add(tz);
    }

    @Override
    public void addCalendarToSelector(String name) {
      addedCalendars.add(name);
    }

    @Override
    public void editCalendarInSelector(String ogName, String newName) {
      selectorEdits.add(ogName + "->" + newName);
    }

    @Override
    public void selectCalendarOnCalendarSelector(String name) {
      selectedCalendar = name;
      selectCalendarCalls++;
    }

    @Override
    public String getSelectedCalendarName() {
      return selectedCalendar;
    }

    @Override
    public CalendarEditData displayEditCalendar(String calendarName, String calendarTz) {
      if (nextEditCalendarResponse != null) {
        CalendarEditData response = nextEditCalendarResponse;
        nextEditCalendarResponse = null;
        return response;
      }
      return new CalendarEditData(calendarName, calendarName, calendarTz);
    }
  }
}
