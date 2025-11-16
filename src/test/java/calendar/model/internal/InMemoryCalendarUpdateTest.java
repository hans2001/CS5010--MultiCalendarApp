package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.exception.ConflictException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

/**
 * Update-flow tests (SINGLE scope and conflicts) for {@link InMemoryCalendar}.
 */
public final class InMemoryCalendarUpdateTest {

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

  private static EventSelector sel(String subject, LocalDateTime start, LocalDateTime end) {
    EventSelector s = new EventSelector();
    s.subject = subject;
    s.start = start;
    s.end = Optional.of(end);
    return s;
  }

  /**
   * Patches subject/start/end for a single event and rejects conflicts.
   */
  @Test
  public void updateBySelector_patchFields_and_conflictRejected() {
    CalendarApi cal = new InMemoryCalendar();

    cal.create(timed("B", at(2025, 1, 1, 12, 0), at(2025, 1, 1, 13, 0)));

    EventPatch patch = new EventPatch();
    patch.subject = Optional.of("A+");
    patch.start = Optional.of(at(2025, 1, 1, 10, 30));
    patch.end = Optional.of(at(2025, 1, 1, 11, 30));

    EventId a = cal.create(timed("A", at(2025, 1, 1, 10, 0), at(2025, 1, 1, 11, 0)));
    cal.updateBySelector(sel("A", at(2025, 1, 1, 10, 0), at(2025, 1, 1, 11, 0)), patch,
        EditScope.SINGLE);

    Event updated = cal.allEvents().stream().filter(e -> e.id().equals(a)).findFirst()
        .orElseThrow(() -> new AssertionError("Updated event not found"));
    assertEquals("A+", updated.subject());
    assertEquals(at(2025, 1, 1, 10, 30), updated.start());
    assertEquals(at(2025, 1, 1, 11, 30), updated.end());

    EventPatch conflict = new EventPatch();
    conflict.subject = Optional.of("A+");
    conflict.start = Optional.of(at(2025, 1, 1, 10, 30));
    conflict.end = Optional.of(at(2025, 1, 1, 11, 30));

    assertThrows(ConflictException.class, () ->
        cal.updateBySelector(sel("B", at(2025, 1, 1, 12, 0), at(2025, 1, 1, 13, 0)), conflict,
            EditScope.SINGLE));

    List<Event> all = cal.allEvents();
    assertEquals(2, all.size());
    assertEquals(2, all.stream().map(e -> e.id()).distinct().count());
  }
}

