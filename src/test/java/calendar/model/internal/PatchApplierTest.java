package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.model.api.EventPatch;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.Status;
import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Tests for PatchApplier behavior.
 */
public final class PatchApplierTest {

  private static Event event(String subject, LocalDateTime s, LocalDateTime e) {
    return new Event.Builder().subject(subject).start(s).end(e).status(Status.PUBLIC).build();
  }

  @Test
  public void apply_updates_and_validates_and_conflicts() {
    Map<EventId, Event> byId = new HashMap<>();
    UniquenessIndex idx = new UniquenessIndex();
    final PatchApplier applier = new PatchApplier(byId, idx);

    Event a = event("A", LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 1, 10, 0));
    byId.put(a.id(), a);
    idx.addOrThrow(a.subject(), a.start(), a.end());

    EventPatch p = new EventPatch();
    p.subject = java.util.Optional.of("A+");
    applier.apply(a.id(), p);
    assertEquals("A+", byId.get(a.id()).subject());

    Event b = event("B", LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 1, 10, 0));
    byId.put(b.id(), b);
    idx.addOrThrow(b.subject(), b.start(), b.end());
    EventPatch collide = new EventPatch();
    collide.subject = java.util.Optional.of("A+");
    assertThrows(ConflictException.class,
        () -> applier.apply(b.id(), collide));

    EventPatch bad = new EventPatch();
    bad.start = java.util.Optional.of(LocalDateTime.of(2025, 1, 1, 10, 0));
    bad.end = java.util.Optional.of(LocalDateTime.of(2025, 1, 1, 10, 0));
    assertThrows(IllegalArgumentException.class, () -> applier.apply(a.id(), bad));

    assertThrows(NotFoundException.class,
        () -> applier.apply(new EventId(java.util.UUID.randomUUID()), new EventPatch()));
  }
}

