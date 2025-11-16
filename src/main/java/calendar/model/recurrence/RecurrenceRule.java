package calendar.model.recurrence;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

/**
 * Recurrence rule for an event series.
 *
 * <p>Exactly one of {@code count} or {@code untilDate} should be present.
 * At least one weekday must be provided.
 */
public final class RecurrenceRule {
  public final EnumSet<Weekday> weekdays;
  public final Optional<Integer> count;
  public final Optional<LocalDate> untilDate;

  /**
   * Constructs a rule.
   *
   * @param weekdays weekdays on which events repeat
   * @param count optional finite number of occurrences
   * @param untilDate optional inclusive end date
   */
  public RecurrenceRule(EnumSet<Weekday> weekdays,
                        Optional<Integer> count,
                        Optional<LocalDate> untilDate) {
    this.weekdays = Objects.requireNonNull(weekdays, "weekdays");
    this.count = Objects.requireNonNull(count, "count");
    this.untilDate = Objects.requireNonNull(untilDate, "untilDate");
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("At least one weekday required");
    }
    if (count.isPresent() == untilDate.isPresent()) {
      throw new IllegalArgumentException("Specify exactly one of count or untilDate");
    }
    if (count.isPresent() && count.get() <= 0) {
      throw new IllegalArgumentException("count must be positive");
    }
  }
}
