package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.model.api.EventDraft;
import calendar.model.domain.Event;
import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

/**
 * Tests for {@link CalendarManager}.
 */
public final class CalendarManagerTest {

  private static EventDraft draft(String subject, LocalDateTime start, LocalDateTime end) {
    EventDraft draft = new EventDraft();
    draft.subject = subject;
    draft.start = Optional.of(start);
    draft.end = Optional.of(end);
    return draft;
  }

  @Test(expected = ConflictException.class)
  public void createCalendar_enforcesUniqueNames() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("School", "America/New_York");
    manager.createCalendar("School", "America/New_York");
  }

  @Test
  public void hasCalendarFalseWhenMissing() {
    CalendarManager manager = new CalendarManager();
    assertFalse(manager.hasCalendar("ghost"));
  }

  @Test(expected = ValidationException.class)
  public void createCalendar_blankName_rejected() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("   ", "America/New_York");
  }

  @Test
  public void editCalendarName_updatesManagerAndCalendar() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("School", "America/New_York");

    manager.editCalendarName("School", "Personal");

    assertTrue(manager.hasCalendar("Personal"));
    assertEquals("Personal", manager.getCalendar("Personal").getName());
  }

  @Test(expected = ValidationException.class)
  public void editCalendarName_blankNewName_rejected() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("School", "America/New_York");
    manager.editCalendarName("School", "   ");
  }

  @Test
  public void editCalendarTimezone_updatesZone() {
    CalendarManager manager = new CalendarManager();
    TimeZoneInMemoryCalendarInterface calendar =
        manager.createCalendar("School", "America/New_York");

    manager.editCalendarTimezone("School", "Europe/Paris");
    assertEquals(ZoneId.of("Europe/Paris"), calendar.getZoneId());

    manager.editCalendarTimezone("School", ZoneId.of("Asia/Tokyo"));
    assertEquals(ZoneId.of("Asia/Tokyo"), calendar.getZoneId());
  }

  @Test
  public void copyEvent_createsEventInTargetCalendar() {
    CalendarManager manager = new CalendarManager();
    TimeZoneInMemoryCalendarInterface source =
        manager.createCalendar("Source", "America/New_York");
    TimeZoneInMemoryCalendarInterface target =
        manager.createCalendar("Target", "America/Los_Angeles");

    LocalDateTime sourceStart = LocalDateTime.of(2025, 7, 1, 8, 0);
    LocalDateTime sourceEnd = sourceStart.plusHours(2);
    source.create(draft("Workshop", sourceStart, sourceEnd));

    LocalDateTime targetStart = LocalDateTime.of(2025, 7, 3, 9, 0);
    manager.copyEvent("Source", "Workshop", sourceStart, "Target", targetStart);

    Event copied = target.allEvents().get(0);
    assertEquals("Workshop", copied.subject());
    assertEquals(targetStart, copied.start());
  }

  @Test(expected = NotFoundException.class)
  public void getCalendar_missing_throwsNotFound() {
    CalendarManager manager = new CalendarManager();
    manager.getCalendar("missing");
  }

  @Test
  public void getCalendar_trimsNameOnLookup() {
    CalendarManager manager = new CalendarManager();
    TimeZoneInMemoryCalendarInterface calendar =
        manager.createCalendar("Work", "America/New_York");
    assertEquals(calendar, manager.getCalendar("  Work  "));
  }

  @Test(expected = IllegalArgumentException.class)
  public void editCalendarTimezone_invalidZone_rethrows() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendarTimezone("Work", "Not/AZone");
  }

  @Test(expected = NotFoundException.class)
  public void copyEvent_missingCalendar_throwsNotFound() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Source", "America/New_York");
    manager.copyEvent("Source", "Any", LocalDateTime.of(2025, 1, 1, 9, 0),
        "Missing", LocalDateTime.of(2025, 1, 2, 9, 0));
  }

  @Test(expected = ConflictException.class)
  public void editCalendarName_rejectsDuplicate() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("One", "America/New_York");
    manager.createCalendar("Two", "America/New_York");
    manager.editCalendarName("Two", "One");
  }

  @Test
  public void copyEventsOn_usesFacadeLookup() {
    CalendarManager manager = new CalendarManager();
    TimeZoneInMemoryCalendarInterface source =
        manager.createCalendar("Source", "America/New_York");
    TimeZoneInMemoryCalendarInterface target =
        manager.createCalendar("Target", "America/New_York");

    LocalDate date = LocalDate.of(2025, 8, 10);
    source.create(draft("Standup", date.atTime(9, 0), date.atTime(9, 30)));

    LocalDate targetDate = date.plusDays(1);
    manager.copyEventsOn("Source", date, "Target", targetDate);

    List<Event> copied = target.allEvents();
    assertEquals(1, copied.size());
    assertEquals(targetDate, copied.get(0).start().toLocalDate());
  }

  @Test
  public void copyEventsBetween_shiftsRangeBasedOnTargetStart() {
    CalendarManager manager = new CalendarManager();
    TimeZoneInMemoryCalendarInterface source =
        manager.createCalendar("Source", "America/New_York");
    final TimeZoneInMemoryCalendarInterface target =
        manager.createCalendar("Target", "America/New_York");

    LocalDate startDate = LocalDate.of(2025, 9, 1);
    source.create(draft("Lecture1", startDate.atTime(10, 0), startDate.atTime(11, 0)));
    source.create(draft("Lecture2", startDate.plusDays(1).atTime(10, 0),
        startDate.plusDays(1).atTime(11, 0)));

    manager.copyEventsBetween("Source", startDate, startDate.plusDays(1),
        "Target", LocalDate.of(2025, 10, 1));

    List<Event> copied = target.allEvents();
    assertEquals(2, copied.size());
    assertEquals(LocalDate.of(2025, 10, 1), copied.get(0).start().toLocalDate());
    assertEquals(LocalDate.of(2025, 10, 2), copied.get(1).start().toLocalDate());
  }

  @Test
  public void copyEventsBetween_rejectsInvalidRange() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Source", "America/New_York");
    manager.createCalendar("Target", "America/New_York");

    assertThrows(ValidationException.class,
        () -> manager.copyEventsBetween("Source",
            LocalDate.of(2025, 1, 10),
            LocalDate.of(2025, 1, 9),
            "Target",
            LocalDate.of(2025, 2, 1)));
  }

  @Test
  public void getCalendarNames_returnsImmutableSnapshot() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    Set<String> names = manager.getCalendarNames();
    assertTrue(names.contains("Work"));
    assertThrows(UnsupportedOperationException.class, () -> names.add("Personal"));
  }

  @Test
  public void getAllCalendars_returnsImmutableSnapshot() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    Map<String, TimeZoneInMemoryCalendarInterface> calendars = manager.getAllCalendars();
    assertTrue(calendars.containsKey("Work"));
    assertThrows(UnsupportedOperationException.class, () ->
        calendars.put("Other", manager.createCalendar("Other", "America/Chicago")));
  }

  @Test
  public void sizeAndIsEmptyTrackState() {
    CalendarManager manager = new CalendarManager();
    assertEquals(0, manager.size());
    assertTrue(manager.isEmpty());

    manager.createCalendar("Work", "America/New_York");
    assertEquals(1, manager.size());
    assertTrue(manager.hasCalendar("Work"));
    assertFalse(manager.isEmpty());
  }
}
