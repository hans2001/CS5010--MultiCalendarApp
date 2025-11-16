package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.model.TimeZoneInMemoryCalendar;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.SeriesId;
import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

/**
 * Tests for {@link EventCopier}.
 */
public final class EventCopierTest {

  private static EventDraft draft(String subject, LocalDateTime start, LocalDateTime end) {
    EventDraft draft = new EventDraft();
    draft.subject = subject;
    draft.start = Optional.of(start);
    draft.end = Optional.of(end);
    return draft;
  }

  private static TimeZoneInMemoryCalendar calendar(String zone, String name) {
    return new TimeZoneInMemoryCalendar(zone, name);
  }

  @Test
  public void copyEvent_preservesDurationAndSubject() {
    TimeZoneInMemoryCalendarInterface source = calendar("America/New_York", "Source");
    TimeZoneInMemoryCalendarInterface target = calendar("America/Los_Angeles", "Target");
    LocalDateTime sourceStart = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime sourceEnd = sourceStart.plusHours(1);
    source.create(draft("Lecture", sourceStart, sourceEnd));

    LocalDateTime targetStart = LocalDateTime.of(2025, 5, 7, 9, 30);
    EventCopier.copyEvent(source, "Lecture", sourceStart, target, targetStart);

    List<Event> targetEvents = target.allEvents();
    assertEquals(1, targetEvents.size());
    Event copied = targetEvents.get(0);
    assertEquals(targetStart, copied.start());
    assertEquals(targetStart.plusHours(1), copied.end());
    assertEquals("Lecture", copied.subject());
  }

  @Test
  public void copyEventsOn_appliesChronoUnitDaysOffset() {
    TimeZoneInMemoryCalendarInterface source = calendar("America/New_York", "Source");
    TimeZoneInMemoryCalendarInterface target = calendar("America/New_York", "Target");
    LocalDate date = LocalDate.of(2025, 6, 2);
    source.create(draft("Morning", date.atTime(9, 0), date.atTime(10, 0)));
    source.create(draft("Afternoon", date.atTime(14, 0), date.atTime(15, 0)));

    LocalDate targetDate = date.plusDays(3);
    EventCopier.copyEventsOn(source, date, target, targetDate);

    List<Event> copied = target.allEvents();
    assertEquals(2, copied.size());
    assertEquals(targetDate, copied.get(0).start().toLocalDate());
    assertEquals(targetDate, copied.get(1).start().toLocalDate());
  }

  @Test
  public void copyEventsBetween_offsetsBasedOnTargetStart() {
    TimeZoneInMemoryCalendarInterface source = calendar("America/Chicago", "Source");
    TimeZoneInMemoryCalendarInterface target = calendar("America/Chicago", "Target");
    LocalDate start = LocalDate.of(2025, 11, 3);
    source.create(draft("Day1", start.atTime(8, 0), start.atTime(9, 0)));
    source.create(draft("Day2", start.plusDays(2).atTime(8, 0), start.plusDays(2).atTime(9, 0)));

    EventCopier.copyEventsBetween(
        source, start, start.plusDays(2), target, LocalDate.of(2025, 12, 1));

    List<Event> copied = target.allEvents();
    assertEquals(LocalDate.of(2025, 12, 1), copied.get(0).start().toLocalDate());
    assertEquals(LocalDate.of(2025, 12, 3), copied.get(1).start().toLocalDate());
  }

  @Test
  public void copyEventsBetween_rejectsEndBeforeStart() {
    TimeZoneInMemoryCalendarInterface source = calendar("America/Chicago", "Source");
    TimeZoneInMemoryCalendarInterface target = calendar("America/Chicago", "Target");
    assertThrows(ValidationException.class, () -> EventCopier.copyEventsBetween(
        source,
        LocalDate.of(2025, 1, 5),
        LocalDate.of(2025, 1, 4),
        target,
        LocalDate.of(2025, 1, 10)));
  }

  @Test
  public void copyEvent_wrappedConflictIncludesCause() {
    TimeZoneInMemoryCalendarInterface source = calendar("America/New_York", "Source");
    TimeZoneInMemoryCalendarInterface target = calendar("America/New_York", "Target");
    LocalDateTime sourceStart = LocalDateTime.of(2025, 5, 5, 10, 0);
    source.create(draft("Overlap", sourceStart, sourceStart.plusHours(1)));

    target.create(draft("Overlap", LocalDateTime.of(2025, 6, 1, 9, 0),
        LocalDateTime.of(2025, 6, 1, 10, 0)));

    ConflictException ex = assertThrows(ConflictException.class,
        () -> EventCopier.copyEvent(source, "Overlap", sourceStart, target,
            LocalDateTime.of(2025, 6, 1, 9, 0)));
    assertTrue(ex.getMessage().contains("Cannot copy event"));
  }

  @Test(expected = NotFoundException.class)
  public void copyEvent_missingEvent_throwsNotFound() {
    LocalDateTime otherStart = LocalDateTime.of(2025, 1, 1, 8, 0);
    Event other = new Event.Builder()
        .subject("Other")
        .start(otherStart)
        .end(otherStart.plusHours(1))
        .build();
    TimeZoneInMemoryCalendarInterface source =
        StubCalendar.withEvents(Collections.singletonList(other), ZoneId.of("America/New_York"));
    TimeZoneInMemoryCalendarInterface target =
        calendar("America/New_York", "Target");

    EventCopier.copyEvent(source, "Absent", LocalDateTime.of(2025, 1, 1, 10, 0), target,
        LocalDateTime.of(2025, 1, 2, 10, 0));
  }

  @Test(expected = ValidationException.class)
  public void copyEvent_ambiguousMatch_throwsValidationException() {
    List<Event> duplicates = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 2, 5, 9, 0);
    duplicates.add(new Event.Builder()
        .subject("Standup")
        .start(start)
        .end(start.plusHours(1))
        .build());
    duplicates.add(new Event.Builder()
        .subject("Standup")
        .start(start)
        .end(start.plusHours(2))
        .build());

    TimeZoneInMemoryCalendarInterface source =
        StubCalendar.withEvents(duplicates, ZoneId.of("America/New_York"));
    TimeZoneInMemoryCalendarInterface target =
        calendar("America/New_York", "Target");

    EventCopier.copyEvent(source, "Standup", start, target, start.plusDays(1));
  }

  private static final class StubCalendar implements TimeZoneInMemoryCalendarInterface {
    private final List<Event> events;
    private ZoneId zoneId;

    private StubCalendar(List<Event> events, ZoneId zoneId) {
      this.events = events;
      this.zoneId = zoneId;
    }

    static StubCalendar withEvents(List<Event> events, ZoneId zoneId) {
      return new StubCalendar(events, zoneId);
    }

    @Override
    public String getName() {
      return "stub";
    }

    @Override
    public ZoneId getZoneId() {
      return zoneId;
    }

    @Override
    public void setName(String name) {
      // no-op
    }

    @Override
    public void setZoneId(String timeZoneId) {
      this.zoneId = ZoneId.of(timeZoneId);
    }

    @Override
    public void setZoneId(ZoneId zoneId) {
      this.zoneId = zoneId;
    }

    @Override
    public java.time.ZonedDateTime convertTimeFromOneTimeZoneToAnother(LocalDateTime time,
                                                                       ZoneId currentZoneId,
                                                                       ZoneId newZoneId) {
      return time.atZone(currentZoneId).withZoneSameInstant(newZoneId);
    }

    @Override
    public LocalDateTime convertToLocalDateTime(LocalDateTime time, ZoneId currentZoneId,
                                                ZoneId newZoneId) {
      return convertTimeFromOneTimeZoneToAnother(time, currentZoneId, newZoneId).toLocalDateTime();
    }

    @Override
    public List<Event> eventsOn(LocalDate date) {
      return events;
    }

    @Override
    public List<Event> eventsOverlapping(LocalDateTime from, LocalDateTime to) {
      return events;
    }

    @Override
    public List<Event> allEvents() {
      return events;
    }

    @Override
    public EventId create(EventDraft draft) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SeriesId createSeries(SeriesDraft draft) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void updateBySelector(EventSelector selector, EventPatch patch, EditScope scope) {
      throw new UnsupportedOperationException();
    }

    @Override
    public BusyStatus statusAt(LocalDateTime instant) {
      return BusyStatus.AVAILABLE;
    }
  }
}
