package calendar.model.api;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Identifies which event(s) to edit based on CLI command syntax.
 *
 * <p>For "edit event ... to END" both start and end are provided (exact match).
 * For "edit events/series ... from START" only start is provided (unique-by-start match).</p>
 *
 * <h2>Design: Optional End</h2>
 *
 * <p><b>Why is end Optional?</b> Commands differ. "edit event" includes end time for exact
 * matching. "edit events/series" omits it, so SelectorResolver matches by (subject, start) and
 * confirms only one event matches. Using Optional makes this explicit and avoids null checks.</p>
 */
public final class EventSelector {
  public String subject;
  public LocalDateTime start;
  public Optional<LocalDateTime> end = Optional.empty();
}
