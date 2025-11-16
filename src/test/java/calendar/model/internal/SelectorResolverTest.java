package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.model.api.EventSelector;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.Status;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Tests for SelectorResolver matching rules.
 */
public final class SelectorResolverTest {

  private static Event ev(String subject, LocalDateTime s, LocalDateTime e) {
    return new Event.Builder()
        .subject(subject)
        .start(s)
        .end(e)
        .status(Status.PUBLIC)
        .build();
  }

  private static EventSelector sel(String subj, LocalDateTime s) {
    EventSelector es = new EventSelector();
    es.subject = subj;
    es.start = s;
    return es;
  }

  /**
   * Resolves when subject, start, and end match exactly.
   */
  @Test
  public void exactMatch_subjectStartEnd() {
    Map<EventId, Event> byId = new HashMap<>();
    Event e = ev("A", LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 1, 10, 0));
    byId.put(e.id(), e);

    final SelectorResolver r = new SelectorResolver(byId);
    EventSelector s = new EventSelector();
    s.subject = "A";
    s.start = LocalDateTime.of(2025, 1, 1, 9, 0);
    s.end = java.util.Optional.of(LocalDateTime.of(2025, 1, 1, 10, 0));

    assertEquals(e, r.resolve(s));
  }

  /**
   * Throws NotFound when end is present but does not match.
   */
  @Test
  public void exactMatch_subjectStartEnd_notFound() {
    Map<EventId, Event> byId = new HashMap<>();
    Event e = ev("A", LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 1, 10, 0));
    byId.put(e.id(), e);

    final SelectorResolver r = new SelectorResolver(byId);
    EventSelector s = new EventSelector();
    s.subject = "A";
    s.start = LocalDateTime.of(2025, 1, 1, 9, 0);
    s.end = java.util.Optional.of(LocalDateTime.of(2025, 1, 1, 10, 30));

    org.junit.Assert.assertThrows(NotFoundException.class,
        () -> r.resolve(s));
  }

  /**
   * Throws NotFound when subject and end match but start differs.
   */
  @Test
  public void exactMatch_subjectAndEnd_match_butStart_mismatch_notFound() {
    Map<EventId, Event> byId = new HashMap<>();
    Event e = ev("A", LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 1, 10, 0));
    byId.put(e.id(), e);

    final SelectorResolver r = new SelectorResolver(byId);
    EventSelector s = new EventSelector();
    s.subject = "A";
    s.start = LocalDateTime.of(2025, 1, 1, 9, 30);
    s.end = java.util.Optional.of(LocalDateTime.of(2025, 1, 1, 10, 0));

    org.junit.Assert.assertThrows(NotFoundException.class,
        () -> r.resolve(s));
  }

  /**
   * Unique-by-start succeeds only when unambiguous; errors on ambiguous or not found.
   */
  @Test
  public void uniqueBySubjectStartOrAmbiguousOrNotFound() {
    Map<EventId, Event> byId = new HashMap<>();
    Event a1 = ev("A", LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 1, 10, 0));
    Event a2 = ev("A", LocalDateTime.of(2025, 1, 2, 9, 0), LocalDateTime.of(2025, 1, 2, 10, 0));
    Event duplicateStart = ev("A", LocalDateTime.of(2025, 1, 1, 9, 0),
        LocalDateTime.of(2025, 1, 1, 11, 0));
    byId.put(a1.id(), a1);
    byId.put(a2.id(), a2);
    byId.put(duplicateStart.id(), duplicateStart);

    SelectorResolver r = new SelectorResolver(byId);

    assertThrows(ValidationException.class,
        () -> r.resolve(sel("A", LocalDateTime.of(2025, 1, 1, 9, 0))));

    assertThrows(NotFoundException.class,
        () -> r.resolve(sel("X", LocalDateTime.of(2025, 1, 1, 9, 0))));
  }
}

