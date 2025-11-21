package calendar.controller.service;

import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a normalized request for creating either a single event or a recurring series.
 * Instances of this class capture the semantic data parsed from the CLI or supplied by GUI forms,
 * enabling controllers to reuse the same validation logic across views.
 */
public final class EventCreationRequest {
  /**
   * Supported command shapes from the CLI. GUI forms can construct the appropriate request
   * directly, bypassing textual parsing altogether.
   */
  public enum Pattern {
    SINGLE_TIMED,
    SINGLE_ALL_DAY,
    RECURRING_TIMED_COUNT,
    RECURRING_TIMED_UNTIL,
    RECURRING_ALL_DAY_COUNT,
    RECURRING_ALL_DAY_UNTIL
  }

  private final Pattern pattern;
  private final String subject;
  private final Optional<LocalDateTime> startDateTime;
  private final Optional<LocalDateTime> endDateTime;
  private final Optional<LocalDate> allDayDate;
  private final EnumSet<Weekday> weekdays;
  private final Optional<Integer> occurrences;
  private final Optional<LocalDate> untilDate;

  private EventCreationRequest(Builder builder) {
    this.pattern = Objects.requireNonNull(builder.pattern, "pattern");
    this.subject = Objects.requireNonNull(builder.subject, "subject");
    this.startDateTime = Optional.ofNullable(builder.startDateTime);
    this.endDateTime = Optional.ofNullable(builder.endDateTime);
    this.allDayDate = Optional.ofNullable(builder.allDayDate);
    this.weekdays = builder.weekdays == null ? EnumSet.noneOf(Weekday.class) : builder.weekdays;
    this.occurrences = Optional.ofNullable(builder.occurrences);
    this.untilDate = Optional.ofNullable(builder.untilDate);
  }

  /**
   * Returns the type of creation request (single vs. recurring, all-day vs. timed).
   */
  public Pattern pattern() {
    return pattern;
  }

  /**
   * Returns the requested subject/title.
   */
  public String subject() {
    return subject;
  }

  /**
   * Start date/time if provided (absent for all-day events).
   */
  public Optional<LocalDateTime> startDateTime() {
    return startDateTime;
  }

  /**
   * End date/time if provided (absent for all-day events).
   */
  public Optional<LocalDateTime> endDateTime() {
    return endDateTime;
  }

  /**
   * Date for all-day events/series.
   */
  public Optional<LocalDate> allDayDate() {
    return allDayDate;
  }

  /**
   * Weekdays used for recurring events (empty for single events).
   */
  public EnumSet<Weekday> weekdays() {
    return weekdays;
  }

  /**
   * Optional finite number of occurrences for recurring series.
   */
  public Optional<Integer> occurrences() {
    return occurrences;
  }

  /**
   * Optional end date for recurring series.
   */
  public Optional<LocalDate> untilDate() {
    return untilDate;
  }

  /**
   * Builder for {@link EventCreationRequest} to avoid telescoping constructors.
   */
  public static final class Builder {
    private Pattern pattern;
    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDate allDayDate;
    private EnumSet<Weekday> weekdays;
    private Integer occurrences;
    private LocalDate untilDate;

    /**
     * Sets the creation pattern (single, recurring, etc.).
     */
    public Builder pattern(Pattern pattern) {
      this.pattern = pattern;
      return this;
    }

    /**
     * Sets the subject/title.
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start date/time (timed events only).
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the end date/time (timed events only).
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the all-day date (all-day events only).
     */
    public Builder allDayDate(LocalDate allDayDate) {
      this.allDayDate = allDayDate;
      return this;
    }

    /**
     * Sets the recurrence weekdays.
     */
    public Builder weekdays(EnumSet<Weekday> weekdays) {
      this.weekdays = weekdays;
      return this;
    }

    /**
     * Sets the number of occurrences for repeating events.
     */
    public Builder occurrences(Integer occurrences) {
      this.occurrences = occurrences;
      return this;
    }

    /**
     * Sets the inclusive until date for repeating events.
     */
    public Builder untilDate(LocalDate untilDate) {
      this.untilDate = untilDate;
      return this;
    }

    /**
     * Builds an immutable {@link EventCreationRequest}.
     */
    public EventCreationRequest build() {
      return new EventCreationRequest(this);
    }
  }
}
