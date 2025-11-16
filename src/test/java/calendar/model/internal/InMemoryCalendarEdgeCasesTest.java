package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.domain.Status;
import calendar.model.exception.ValidationException;
import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Test;

/**
 * Additional edge-case coverage for InMemoryCalendar.
 */
public final class InMemoryCalendarEdgeCasesTest {

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

  private static SeriesDraft weeklyMonWed(String subject, LocalDate startDate, LocalTime start,
                                          LocalTime end, int count) {
    SeriesDraft d = new SeriesDraft();
    d.subject = subject;
    d.allDay = false;
    d.startDate = startDate;
    d.startTime = Optional.of(start);
    d.endTime = Optional.of(end);
    d.rule =
        new RecurrenceRule(EnumSet.of(Weekday.M, Weekday.W), Optional.of(count), Optional.empty());
    d.status = Optional.of(Status.PUBLIC);
    return d;
  }

  /**
   * ENTIRE_SERIES start-only patch preserves duration across instances.
   */
  @Test
  public void adjustPatch_durationPath_isApplied() {
    CalendarApi cal = new InMemoryCalendar();

    cal.createSeries(
        weeklyMonWed("A", LocalDate.of(2025, 5, 5), LocalTime.of(10, 0), LocalTime.of(11, 15), 2));

    EventPatch p = new EventPatch();
    p.start = Optional.of(LocalDateTime.of(2025, 5, 5, 9, 30));

    EventSelector s = new EventSelector();
    s.subject = "A";
    s.start = LocalDate.of(2025, 5, 5).atTime(10, 0);

    cal.updateBySelector(s, p, EditScope.ENTIRE_SERIES);

    Event e = cal.allEvents().stream()
        .filter(ev -> ev.start().toLocalDate().equals(LocalDate.of(2025, 5, 5)))
        .findFirst().orElseThrow(() -> new AssertionError("event not found"));
    assertEquals(LocalDateTime.of(2025, 5, 5, 9, 30), e.start());
    assertEquals(LocalDateTime.of(2025, 5, 5, 10, 45), e.end());
  }

  /**
   * SINGLE start change detaches the instance from its series.
   */
  @Test
  public void singleScope_startChange_detachesFromSeries() {
    CalendarApi cal = new InMemoryCalendar();

    cal.createSeries(
        weeklyMonWed("First", LocalDate.of(2025, 5, 5),
            LocalTime.of(10, 0), LocalTime.of(11, 0), 2));

    EventPatch p = new EventPatch();
    p.start = Optional.of(LocalDateTime.of(2025, 5, 5, 10, 30));
    p.end = Optional.of(LocalDateTime.of(2025, 5, 5, 11, 30));

    EventSelector s = new EventSelector();
    s.subject = "First";
    s.start = LocalDate.of(2025, 5, 5).atTime(10, 0);

    cal.updateBySelector(s, p, EditScope.SINGLE);

    EventPatch rename = new EventPatch();
    rename.subject = Optional.of("Renamed");

    EventSelector s2 = new EventSelector();
    s2.subject = "First";
    s2.start = LocalDate.of(2025, 5, 7).atTime(10, 0);

    cal.updateBySelector(s2, rename, EditScope.ENTIRE_SERIES);

    long renamed = cal.allEvents().stream().filter(e -> e.subject().equals("Renamed")).count();
    long first = cal.allEvents().stream().filter(e -> e.subject().equals("First")).count();
    assertEquals(1, renamed);
    assertEquals(1, first);
  }

  /**
   * eventsOverlapping rejects invalid ranges (end <= start).
   */
  @Test
  public void eventsOverlapping_invalidRange_rejected() {
    CalendarApi cal = new InMemoryCalendar();
    assertThrows(ValidationException.class,
        () -> cal.eventsOverlapping(at(2025, 1, 1, 10, 0), at(2025, 1, 1, 10, 0)));
    assertThrows(ValidationException.class,
        () -> cal.eventsOverlapping(at(2025, 1, 1, 10, 0), at(2025, 1, 1, 9, 0)));
  }

  /**
   * FOLLOWING without start change updates anchor and subsequent instances.
   */
  @Test
  public void following_noStartChange_updatesAnchorAndFollowingOnly() {
    CalendarApi cal = new InMemoryCalendar();

    cal.createSeries(
        weeklyMonWed("Topic", LocalDate.of(2025, 5, 5),
            LocalTime.of(10, 0), LocalTime.of(11, 0), 3));

    EventPatch p = new EventPatch();
    p.subject = Optional.of("NewTopic");

    EventSelector s = new EventSelector();
    s.subject = "Topic";
    s.start = LocalDate.of(2025, 5, 7).atTime(10, 0);

    cal.updateBySelector(s, p, EditScope.FOLLOWING);

    long renamed = cal.allEvents().stream().filter(e -> e.subject().equals("NewTopic")).count();
    long original = cal.allEvents().stream().filter(e -> e.subject().equals("Topic")).count();
    assertEquals(2, renamed);
    assertEquals(1, original);
  }

  /**
   * SINGLE without start change keeps membership in the series.
   */
  @Test
  public void singleScope_noStartChange_keepsSeries() {
    CalendarApi cal = new InMemoryCalendar();

    cal.createSeries(
        weeklyMonWed("Keep", LocalDate.of(2025, 6, 2),
            LocalTime.of(10, 0), LocalTime.of(11, 0), 2));

    EventPatch p = new EventPatch();
    p.description = Optional.of("note");

    EventSelector s = new EventSelector();
    s.subject = "Keep";
    s.start = LocalDate.of(2025, 6, 2).atTime(10, 0);

    cal.updateBySelector(s, p, EditScope.SINGLE);

    EventPatch rename = new EventPatch();
    rename.subject = Optional.of("RenamedAll");
    cal.updateBySelector(s, rename, EditScope.ENTIRE_SERIES);

    long renamed = cal.allEvents().stream().filter(e -> e.subject().equals("RenamedAll")).count();
    org.junit.Assert.assertEquals(2, renamed);
  }

  /**
   * SINGLE start provided equals anchor: ensure no detach and series-wide rename still applies.
   */
  @Test
  public void singleScope_startProvided_butEqual_keepsSeries() {
    CalendarApi cal = new InMemoryCalendar();

    cal.createSeries(
        weeklyMonWed("Eq", LocalDate.of(2025, 8, 4), LocalTime.of(10, 0), LocalTime.of(11, 0), 2));

    EventSelector s = new EventSelector();
    s.subject = "Eq";
    s.start = LocalDate.of(2025, 8, 4).atTime(10, 0);

    EventPatch p = new EventPatch();
    p.start = Optional.of(LocalDateTime.of(2025, 8, 4, 10, 0));
    p.description = Optional.of("same-start");

    cal.updateBySelector(s, p, EditScope.SINGLE);

    EventPatch rename = new EventPatch();
    rename.subject = Optional.of("EQREN");
    cal.updateBySelector(s, rename, EditScope.ENTIRE_SERIES);

    long cnt = cal.allEvents().stream().filter(e -> e.subject().equals("EQREN")).count();
    org.junit.Assert.assertEquals(2L, cnt);
  }

  /**
   * statusAt: inclusive at start, exclusive at end.
   */
  @Test
  public void statusAt_includesStart_excludesEnd() {
    InMemoryCalendar cal = new InMemoryCalendar();
    cal.create(timed("Slot", at(2025, 7, 1, 10, 0), at(2025, 7, 1, 11, 0)));

    org.junit.Assert.assertEquals(BusyStatus.BUSY,
        cal.statusAt(at(2025, 7, 1, 10, 0)));
    org.junit.Assert.assertEquals(BusyStatus.AVAILABLE,
        cal.statusAt(at(2025, 7, 1, 11, 0)));
    org.junit.Assert.assertEquals(BusyStatus.AVAILABLE,
        cal.statusAt(at(2025, 7, 1, 9, 59)));
  }
}
