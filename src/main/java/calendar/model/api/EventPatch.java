package calendar.model.api;

import calendar.model.domain.Status;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Partial update for an existing event.
 *
 * <p>Any non-empty field in this patch replaces the corresponding field in the target event.
 * After application, the model validates the entire event (subject non-blank, end &gt; start,
 * uniqueness of subject/start/end).
 */
public final class EventPatch {
  public Optional<String> subject = Optional.empty();
  public Optional<LocalDateTime> start = Optional.empty();
  public Optional<LocalDateTime> end = Optional.empty();
  public Optional<String> description = Optional.empty();
  public Optional<String> location = Optional.empty();
  public Optional<Status> status = Optional.empty();
}
