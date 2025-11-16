package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.model.api.CalendarApi;
import calendar.model.api.EventDraft;
import calendar.model.api.SeriesDraft;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.SeriesId;
import calendar.model.exception.ConflictException;
import calendar.model.exception.ValidationException;
import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Test;

/**
 * Create-flow tests for {@link InMemoryCalendar}.
 */
public final class InMemoryCalendarCreateTest {

  private static LocalDateTime at(int y, int m, int d, int h, int min) {
    return LocalDateTime.of(y, m, d, h, min);
  }

  private static EventDraft timed(String s, LocalDateTime start, LocalDateTime end) {
    EventDraft d = new EventDraft();
    d.subject = s;
    d.start = Optional.of(start);
    d.end = Optional.of(end);
    return d;
  }

  /**
   * All-day create expands to 08:00-17:00 per settings.
   */
  @Test
  public void create_allDayExpandsToEightToFive() {
    CalendarApi cal = new InMemoryCalendar();

    EventDraft d = new EventDraft();
    d.subject = "Offsite";
    d.allDayDate = Optional.of(LocalDate.of(2025, 5, 5));

    EventId id = cal.create(d);
    Event e = cal.allEvents().get(0);
    assertEquals(id, e.id());
    assertEquals(LocalDateTime.of(2025, 5, 5, 8, 0), e.start());
    assertEquals(LocalDateTime.of(2025, 5, 5, 17, 0), e.end());
  }

  /**
   * Timed create works and duplicate (subject,start,end) is rejected.
   */
  @Test
  public void create_timedSuccess_and_duplicateRejected() {
    CalendarApi cal = new InMemoryCalendar();

    cal.create(timed("Standup", at(2025, 1, 1, 10, 0), at(2025, 1, 1, 10, 15)));

    assertThrows(
        ConflictException.class,
        () -> cal.create(timed("Standup", at(2025, 1, 1, 10, 0), at(2025, 1, 1, 10, 15)))
    );
  }

  /**
   * Missing end on a timed creation normalizes to all-day on that date.
   */
  @Test
  public void create_nonAllDayWithoutEnd_normalizesToAllDay() {
    CalendarApi cal = new InMemoryCalendar();

    EventDraft d = new EventDraft();
    d.subject = "Implicit All-Day";
    d.start = Optional.of(at(2025, 1, 1, 10, 0));

    EventId id = cal.create(d);
    Event e = cal.allEvents().stream()
        .filter(ev -> ev.id().equals(id)).findFirst()
        .orElseThrow(() -> new AssertionError("Expected event not found"));
    assertEquals(LocalDateTime.of(2025, 1, 1, 8, 0), e.start());
    assertEquals(LocalDateTime.of(2025, 1, 1, 17, 0), e.end());
  }

  /**
   * Create validations for null/missing/invalid fields.
   */
  @Test
  public void create_validations() {
    CalendarApi cal = new InMemoryCalendar();

    EventDraft d1 = new EventDraft();
    d1.subject = null;
    d1.allDayDate = Optional.of(LocalDate.of(2025, 5, 5));
    assertThrows(ValidationException.class, () -> cal.create(d1));

    EventDraft d2 = new EventDraft();
    d2.subject = "Nowhere in time";
    assertThrows(ValidationException.class, () -> cal.create(d2));

    EventDraft d3 = new EventDraft();
    d3.subject = "Reverse";
    d3.start = Optional.of(LocalDateTime.of(2025, 1, 1, 10, 0));
    d3.end = Optional.of(LocalDateTime.of(2025, 1, 1, 9, 0));
    assertThrows(ValidationException.class, () -> cal.create(d3));

    EventDraft d4 = new EventDraft();
    d4.subject = "   ";
    d4.allDayDate = Optional.of(LocalDate.of(2025, 5, 5));
    assertThrows(ValidationException.class, () -> cal.create(d4));
  }

  /**
   * createSeries validations: timed drafts fail precheck when invalid.
   */
  @Test
  public void createSeries_instance_validations() {
    final InMemoryCalendar cal = new InMemoryCalendar();

    SeriesDraft d1 = new SeriesDraft();
    d1.subject = "Bad";
    d1.allDay = false;
    d1.startDate = LocalDate.of(2025, 5, 5);
    d1.startTime = Optional.of(LocalDateTime.of(2025, 5, 5, 11, 0).toLocalTime());
    d1.endTime = Optional.of(LocalDateTime.of(2025, 5, 5, 10, 0).toLocalTime());
    d1.rule =
        new RecurrenceRule(java.util.EnumSet.of(Weekday.M), Optional.of(1),
            Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> cal.createSeries(d1));

    SeriesDraft d2 = new SeriesDraft();
    d2.subject = "CrossDay";
    d2.allDay = false;
    d2.startDate = LocalDate.of(2025, 5, 5);
    d2.startTime = Optional.of(LocalDateTime.of(2025, 5, 5, 23, 0).toLocalTime());
    d2.endTime = Optional.of(LocalDateTime.of(2025, 5, 6, 1, 0).toLocalTime());
    d2.rule =
        new RecurrenceRule(java.util.EnumSet.of(Weekday.M), Optional.of(1),
            Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> cal.createSeries(d2));
  }

  /**
   * createSeries: missing endTime in timed draft fails precheck.
   */
  @Test
  public void createSeries_missingEndTime_failsPrecheck() {
    final InMemoryCalendar cal = new InMemoryCalendar();

    SeriesDraft d = new SeriesDraft();
    d.subject = "X";
    d.allDay = false;
    d.startDate = LocalDate.of(2025, 5, 5);
    d.startTime = Optional.of(java.time.LocalTime.of(9, 0));
    d.rule = new RecurrenceRule(java.util.EnumSet.of(Weekday.M), Optional.of(1),
        Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> cal.createSeries(d));
  }

  /**
   * createSeries: duplicate instances violate uniqueness.
   */
  @Test
  public void createSeries_duplicateInstances_conflict() {
    final InMemoryCalendar cal = new InMemoryCalendar();

    SeriesDraft d = new SeriesDraft();
    d.subject = "Dup";
    d.allDay = true;
    d.startDate = LocalDate.of(2025, 5, 5);
    d.rule = new RecurrenceRule(java.util.EnumSet.of(Weekday.M), Optional.of(1),
        Optional.empty());

    cal.createSeries(d);
    assertThrows(ConflictException.class, () -> cal.createSeries(d));
  }

  /**
   * createSeries returns a non-null SeriesId on success.
   */
  @Test
  public void createSeries_returns_seriesId() {
    final InMemoryCalendar cal = new InMemoryCalendar();

    SeriesDraft d = new SeriesDraft();
    d.subject = "Sid";
    d.allDay = true;
    d.startDate = LocalDate.of(2025, 5, 5);
    d.rule = new RecurrenceRule(java.util.EnumSet.of(Weekday.M), Optional.of(1),
        Optional.empty());

    SeriesId sid = cal.createSeries(d);
    org.junit.Assert.assertNotNull(sid);
  }

  /**
   * All-day series uses 08:00-17:00 window from settings.
   */
  @Test
  public void createSeries_allDay_usesSettingsWindow() {
    final InMemoryCalendar cal = new InMemoryCalendar();

    SeriesDraft d = new SeriesDraft();
    d.subject = "AllDaySeries";
    d.allDay = true;
    d.startDate = LocalDate.of(2025, 5, 5);
    d.rule = new RecurrenceRule(java.util.EnumSet.of(Weekday.M), Optional.of(2),
        Optional.empty());

    cal.createSeries(d);

    java.util.List<Event> all = cal.allEvents();
    assertEquals(2, all.size());
    assertEquals(java.time.LocalTime.of(8, 0), all.get(0).start().toLocalTime());
    assertEquals(java.time.LocalTime.of(17, 0), all.get(0).end().toLocalTime());
  }
}

