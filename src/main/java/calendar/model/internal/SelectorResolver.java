package calendar.model.internal;

import calendar.model.api.EventSelector;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Finds the event matching a selector.
 *
 * <p>If selector includes end time, matches exactly on (subject, start, end).
 * Otherwise matches on (subject, start) and throws if zero or multiple events found.</p>
 */
final class SelectorResolver {
  private final Map<EventId, Event> byId;

  SelectorResolver(Map<EventId, Event> byId) {
    this.byId = Objects.requireNonNull(byId, "byId");
  }

  Event resolve(EventSelector selector) {
    String subj = selector.subject.trim().toLowerCase(Locale.ROOT);
    LocalDateTime s = selector.start;

    if (selector.end.isPresent()) {
      var e = selector.end.get();
      return byId.values().stream()
          .filter(ev -> ev.subject().trim().toLowerCase(Locale.ROOT).equals(subj)
              && ev.start().equals(s)
              && ev.end().equals(e))
          .findFirst()
          .orElseThrow(() -> new NotFoundException("No event found for subject/start/end"));
    }

    List<Event> candidates = byId.values().stream()
        .filter(ev -> ev.subject().trim().toLowerCase(Locale.ROOT).equals(subj)
            && ev.start().equals(s))
        .collect(Collectors.toList());

    if (candidates.isEmpty()) {
      throw new NotFoundException("No event found for subject/start");
    }
    if (candidates.size() > 1) {
      throw new ValidationException(
          "Ambiguous selector: multiple events share subject/start; specify end");
    }
    return candidates.get(0);
  }
}
