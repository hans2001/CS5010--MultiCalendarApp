package calendar.model.api;

import calendar.model.domain.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Creation request for a single event.
 * Required:
 *   - subject (non-blank)
 *   - start (date+time) OR allDayDate (date only)
 * Optional:
 *   - end (if absent and start is present, create an all-day event on start's date, 08:00-17:00)
 *   - description, location, status (default PUBLIC)
 * If allDayDate is present, it takes precedence over start/end.
 */
public final class EventDraft {
  public String subject;
  public Optional<LocalDate> allDayDate = Optional.empty();
  public Optional<LocalDateTime> start = Optional.empty();
  public Optional<LocalDateTime> end = Optional.empty();
  public Optional<String> description = Optional.empty();
  public Optional<String> location = Optional.empty();
  public Optional<Status> status = Optional.empty();
}
