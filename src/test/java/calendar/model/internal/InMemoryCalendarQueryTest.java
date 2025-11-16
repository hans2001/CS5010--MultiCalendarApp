package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.model.api.CalendarApi;
import calendar.model.api.EventDraft;
import calendar.model.domain.BusyStatus;
import calendar.model.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Test;

/**
 * Query-flow tests for InMemoryCalendar: eventsOn, eventsOverlapping, statusAt.
 */
public final class InMemoryCalendarQueryTest {

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
   * eventsOverlapping boundaries and validation errors.
   */
  @Test
  public void eventsOverlapping_boundaries_and_validation() {
    CalendarApi cal = new InMemoryCalendar();
    cal.create(timed("A", at(2025, 1, 1, 10, 0), at(2025, 1, 1, 11, 0)));

    assertTrue(cal.eventsOverlapping(at(2025, 1, 1, 9, 30), at(2025, 1, 1, 10, 0)).isEmpty());
    assertTrue(cal.eventsOverlapping(at(2025, 1, 1, 11, 0), at(2025, 1, 1, 11, 30)).isEmpty());
    assertEquals(1, cal.eventsOverlapping(at(2025, 1, 1, 10, 30), at(2025, 1, 1, 10, 45)).size());
    assertThrows(ValidationException.class,
        () -> cal.eventsOverlapping(at(2025, 1, 1, 11, 0), at(2025, 1, 1, 11, 0)));
  }

  /**
   * statusAt start-inclusive and end-exclusive semantics.
   */
  @Test
  public void statusAt_inclusiveExclusiveSemantics() {
    CalendarApi cal = new InMemoryCalendar();
    cal.create(timed("A", at(2025, 1, 1, 10, 0), at(2025, 1, 1, 11, 0)));

    assertEquals(BusyStatus.BUSY, cal.statusAt(at(2025, 1, 1, 10, 0)));
    assertEquals(BusyStatus.AVAILABLE, cal.statusAt(at(2025, 1, 1, 11, 0)));
  }

  /**
   * eventsOn returns non-empty when events exist on that date.
   */
  @Test
  public void eventsOn_returnsNonEmptyWhenEventExists_onThatDate() {
    CalendarApi cal = new InMemoryCalendar();
    cal.create(timed("Day Event", at(2025, 1, 1, 9, 0), at(2025, 1, 1, 10, 0)));

    assertTrue(!cal.eventsOn(LocalDate.of(2025, 1, 1)).isEmpty());
  }
}

