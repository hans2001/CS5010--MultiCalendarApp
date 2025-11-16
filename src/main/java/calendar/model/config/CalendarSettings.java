package calendar.model.config;

import calendar.model.domain.Status;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Service-level policy for the calendar model.
 *
 * <p>Holds configurable choices so the domain remains free of magic numbers:
 * <ul>
 *   <li>All-day window (start inclusive, end exclusive)</li>
 *   <li>Default {@link Status} when the caller omits status</li>
 * </ul>
 *
 * <p>All dates/times are interpreted as EST, per assignment assumptions.</p>
 */
public final class CalendarSettings {
  private final LocalTime allDayStart;
  private final LocalTime allDayEnd;
  private final Status defaultStatus;

  /**
   * Constructs a settings instance.
   *
   * @param allDayStart   start time for all-day normalization (e.g., 08:00)
   * @param allDayEnd     end time for all-day normalization (e.g., 17:00)
   * @param defaultStatus default status when a draft omits status
   */
  public CalendarSettings(LocalTime allDayStart, LocalTime allDayEnd, Status defaultStatus) {
    this.allDayStart = Objects.requireNonNull(allDayStart, "allDayStart");
    this.allDayEnd = Objects.requireNonNull(allDayEnd, "allDayEnd");
    this.defaultStatus = Objects.requireNonNull(defaultStatus, "defaultStatus");
    if (!allDayEnd.isAfter(allDayStart)) {
      throw new IllegalArgumentException("All-day end must be after start");
    }
  }

  /**
   * Common defaults: 08:00-17:00 and PUBLIC.
   */
  public static CalendarSettings defaults() {
    return new CalendarSettings(LocalTime.of(8, 0), LocalTime.of(17, 0), Status.PUBLIC);
  }

  /** Returns the configured all-day start time. */
  public LocalTime allDayStart() {
    return allDayStart;
  }

  /** Returns the configured all-day end time. */
  public LocalTime allDayEnd() {
    return allDayEnd;
  }

  /** Returns the default status used when callers omit status. */
  public Status defaultStatus() {
    return defaultStatus;
  }
}
