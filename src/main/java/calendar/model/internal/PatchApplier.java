package calendar.model.internal;

import calendar.model.api.EventPatch;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.Status;
import calendar.model.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Applies partial updates to events.
 *
 * <p>Merges patch values with existing event fields, creates new immutable Event instances,
 * validates constraints, and updates the uniqueness index. Extracted from InMemoryCalendar
 * to keep that class focused on orchestration.</p>
 */
final class PatchApplier {
  private final Map<EventId, Event> byId;
  private final UniquenessIndex index;

  PatchApplier(Map<EventId, Event> byId, UniquenessIndex index) {
    this.byId = Objects.requireNonNull(byId, "byId");
    this.index = Objects.requireNonNull(index, "index");
  }

  void apply(EventId id, EventPatch patch) {
    Event cur = byId.get(id);
    if (cur == null) {
      throw new NotFoundException("No event with id " + id);
    }

    String subject = patch.subject.orElse(cur.subject());
    LocalDateTime start = patch.start.orElse(cur.start());
    LocalDateTime end = patch.end.orElse(cur.end());
    String desc = patch.description.orElse(cur.description().orElse(""));
    String loc = patch.location.orElse(cur.location().orElse(""));
    Status status = patch.status.orElse(cur.status());

    Event updated = new Event.Builder()
        .id(cur.id())
        .subject(subject)
        .start(start)
        .end(end)
        .description(desc)
        .location(loc)
        .status(status)
        .build();

    String oldKey = UniquenessIndex.key(cur.subject(), cur.start(), cur.end());
    String newKey = UniquenessIndex.key(updated.subject(), updated.start(), updated.end());
    index.replaceOrThrow(oldKey, newKey);
    byId.put(id, updated);
  }
}
