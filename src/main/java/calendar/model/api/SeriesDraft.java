package calendar.model.api;

import calendar.model.domain.Status;
import calendar.model.recurrence.RecurrenceRule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Creation request for a recurring event series.
 *
 * <p>For all-day series, provide only {@code startDate} and set {@code allDay = true}.
 * For timed series, provide {@code startDate}, {@code startTime}, and {@code endTime}.</p>
 */
public final class SeriesDraft {
  public String subject;
  public boolean allDay;
  public LocalDate startDate;
  public Optional<LocalTime> startTime = Optional.empty();
  public Optional<LocalTime> endTime = Optional.empty();
  public RecurrenceRule rule;
  public Optional<String> description = Optional.empty();
  public Optional<String> location = Optional.empty();
  public Optional<Status> status = Optional.empty();

  /** Performs cheap validations. */
  public void precheck() {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("subject is required");
    }
    Objects.requireNonNull(startDate, "startDate missing");
    Objects.requireNonNull(rule, "rule missing");
    if (!allDay) {
      if (startTime.isEmpty() || endTime.isEmpty()) {
        throw new IllegalArgumentException("timed series require startTime and endTime");
      }
      if (!endTime.get().isAfter(startTime.get())) {
        throw new IllegalArgumentException("endTime must be after startTime");
      }
    }
  }
}
