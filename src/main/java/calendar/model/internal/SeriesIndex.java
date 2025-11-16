package calendar.model.internal;

import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tracks which events belong to which series.
 *
 * <h2>Design: Bidirectional Mapping</h2>
 *
 * <p>Uses two maps for fast lookups both ways:</p>
 * <ul>
 *   <li><b>eventToSeries</b>: Given an event, find its series (O(1))</li>
 *   <li><b>seriesToEvents</b>: Given a series, get all its events (O(1))</li>
 * </ul>
 *
 * <p><b>Why both directions?</b> Edit operations need different lookups:
 * <ul>
 *   <li>SINGLE edit with start change: Check if event is in a series (to detach it)</li>
 *   <li>FOLLOWING edit: Find all events after the anchor</li>
 *   <li>ENTIRE_SERIES edit: Find all events in the series</li>
 * </ul>
 *
 * <p><b>Why not store series ID in Event?</b> Events are immutable, so adding/removing
 * series membership would require recreating the event. Also, finding all events in a
 * series would still need scanning. Keeping series data separate is cleaner.</p>
 */
final class SeriesIndex {
  private final Map<EventId, UUID> eventToSeries = new HashMap<>();

  private final Map<UUID, List<EventId>> seriesToEvents = new HashMap<>();

  /**
   * Registers a new series with its ordered event IDs.
   */
  UUID registerSeries(List<EventId> ids) {
    UUID sid = UUID.randomUUID();
    seriesToEvents.put(sid, new ArrayList<>(ids));
    for (EventId id : ids) {
      eventToSeries.put(id, sid);
    }
    return sid;
  }

  /**
   * Returns the series UUID an event belongs to, if any.
   */
  Optional<UUID> seriesOf(EventId id) {
    return Optional.ofNullable(eventToSeries.get(id));
  }

  /**
   * Returns all event IDs in this series that start at or after the cutoff.
   */
  List<EventId> following(UUID sid, LocalDateTime cutoff, Map<EventId, Event> byId) {
    return seriesToEvents.getOrDefault(sid, List.of()).stream()
        .filter(id -> {
          Event e = byId.get(id);
          return e.start().isEqual(cutoff) || e.start().isAfter(cutoff);
        })
        .sorted(Comparator.comparing(id -> byId.get(id).start()))
        .collect(Collectors.toList());
  }

  /**
   * Returns all event IDs in the series.
   */
  List<EventId> all(UUID sid) {
    return seriesToEvents.getOrDefault(sid, List.of());
  }

  /**
   * Detaches a single event from its series, removing empty series if needed.
   */
  void detach(EventId id) {
    UUID sid = eventToSeries.remove(id);
    if (sid == null) {
      return;
    }
    List<EventId> list = seriesToEvents.get(sid);
    if (list != null) {
      list.remove(id);
      if (list.isEmpty()) {
        seriesToEvents.remove(sid);
      }
    }
  }

  /**
   * Splits a series at a cutoff point.
   * Returns the new series ID holding all events at or after the cutoff.
   * If nothing to move, returns the original ID.
   */
  UUID splitFollowing(UUID sid, LocalDateTime cutoff, Map<EventId, Event> byId) {
    List<EventId> existing = seriesToEvents.getOrDefault(sid, List.of());
    List<EventId> keep = new ArrayList<>();
    List<EventId> move = new ArrayList<>();

    for (EventId eid : existing) {
      Event e = byId.get(eid);
      if (e != null && (e.start().isEqual(cutoff) || e.start().isAfter(cutoff))) {
        move.add(eid);
      } else {
        keep.add(eid);
      }
    }

    if (move.isEmpty()) {
      return sid;
    }

    UUID newSid = UUID.randomUUID();
    seriesToEvents.put(sid, keep);
    seriesToEvents.put(newSid, move);

    for (EventId eid : move) {
      eventToSeries.put(eid, newSid);
    }

    if (keep.isEmpty()) {
      seriesToEvents.remove(sid);
    }

    return newSid;
  }
}
