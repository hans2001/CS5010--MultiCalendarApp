package calendar.controller.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarManager;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Focused tests for {@link HandleEvents}.
 */
public class HandleEventsTest {

  @Test
  public void editCalendarNamePrintsSuccessMessage() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("school", "America/New_York");
    RecordingView view = new RecordingView();

    HandleEvents.handleEditCalendarEvent(
        "edit calendar --name school --property name work", manager, view);

    assertTrue(view.messages.contains("Successfully edited calendar name."));
    assertTrue(manager.hasCalendar("work"));
  }

  @Test
  public void handleUseCalendarInvalidFormatPrintsError() throws Exception {
    RecordingView view = new RecordingView();

    TimeZoneInMemoryCalendarInterface result =
        HandleEvents.handleUseCalendarEvent("use calendar school", new CalendarManager(), view);

    assertTrue(view.messages.contains("Invalid use calendar command format."));
    assertNull(result);
  }

  @Test
  public void copyEventRequiresSelectedCalendar() throws Exception {
    RecordingView view = new RecordingView();
    HandleEvents.handleCopyEvent(
        "copy event sample on 2025-05-05T10:00 --target work to 2025-05-05T11:00",
        new CalendarManager(),
        view,
        null);

    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Error: No calendar selected")));
  }

  @Test
  public void copyEventInvalidFormatPrintsError() throws Exception {
    CalendarManager manager = new CalendarManager();
    TimeZoneInMemoryCalendarInterface calendar =
        manager.createCalendar("school", "America/New_York");
    RecordingView view = new RecordingView();

    HandleEvents.handleCopyEvent(
        "copy event bad format",
        manager,
        view,
        calendar);

    assertTrue(view.messages.contains("Invalid copy calendar command format."));
  }

  @Test
  public void printEventsFromToPrintsCalendarHeading() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("school", "America/New_York");
    RecordingView view = new RecordingView();

    HandleEvents.handlePrintEvent(
        "print events from 2025-05-01T00:00 to 2025-05-02T00:00",
        view,
        manager);

    assertTrue(view.messages.contains("Events for calendar: school"));
    assertEquals(1, view.eventsFromToCalls);
  }

  private static final class RecordingView implements CalendarView {
    private final List<String> messages = new ArrayList<>();
    private int eventsOnCalls = 0;
    private int eventsFromToCalls = 0;

    @Override
    public void printMessage(String message) throws IOException {
      messages.add(message);
    }

    @Override
    public void printEventsOn(LocalDate date, List<Event> events) {
      eventsOnCalls++;
    }

    @Override
    public void printEventsFromTo(LocalDateTime from, LocalDateTime to, List<Event> events) {
      eventsFromToCalls++;
    }

    @Override
    public void printStatus(BusyStatus status) {
      // not needed for these tests
    }
  }
}
