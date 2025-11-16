package calendar.model;

import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.SeriesId;
import calendar.model.internal.InMemoryCalendar;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * A Calendar that has a timezone and name.
 *
 * <p>Uses composition to wrap {@link InMemoryCalendar} and adds timezone-related
 * behavior plus calendar naming. All events are stored in the delegate as
 * {@link java.time.LocalDateTime} but interpreted using this calendar's timezone.
 */
public class TimeZoneInMemoryCalendar implements TimeZoneInMemoryCalendarInterface {
  private final CalendarApi delegate;
  private ZoneId zoneId;
  private String name;

  /**
   * Creates a TimeZoneInMemoryCalendar with a timezone.
   *
   * @param timeZoneId the timezone in IANA format (e.g., "America/New_York").
   * @param name the name of the calendar.
   * @throws IllegalArgumentException if the time zone is not valid.
   */
  public TimeZoneInMemoryCalendar(String timeZoneId, String name) throws IllegalArgumentException {
    this(new InMemoryCalendar(), timeZoneId, name);
  }

  /**
   * Package-private constructor that enables injecting a custom delegate (useful for testing).
   */
  TimeZoneInMemoryCalendar(CalendarApi delegate, String timeZoneId, String name) {
    this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    if (timeZoneId == null) {
      throw new IllegalArgumentException("timeZoneId cannot be null");
    }
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    }
    String trimmedTimeZoneId = timeZoneId.trim();
    String trimmedName = name.trim();
    if (trimmedName.isEmpty()) {
      throw new IllegalArgumentException("name cannot be blank");
    }
    this.name = trimmedName;
    try {
      this.zoneId = ZoneId.of(trimmedTimeZoneId);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Unsupported timezone: " + trimmedTimeZoneId, e);
    }
  }

  /**
   * Returns the calendar's name.
   *
   * @return the calendar name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the calendar's name.
   *
   * @param name the new name (must be non-null and non-blank).
   * @throws IllegalArgumentException if name is null or blank.
   */
  public void setName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    }
    String trimmedName = name.trim();
    if (trimmedName.isEmpty()) {
      throw new IllegalArgumentException("name cannot be blank");
    }
    this.name = trimmedName;
  }

  /**
   * Returns the calendar's timezone.
   *
   * @return the timezone.
   */
  public ZoneId getZoneId() {
    return zoneId;
  }

  /**
   * Sets the calendar's timezone.
   *
   * @param timeZoneId the new timezone in IANA format (e.g., "America/New_York").
   * @throws IllegalArgumentException if the timezone is invalid.
   */
  public void setZoneId(String timeZoneId) {
    if (timeZoneId == null) {
      throw new IllegalArgumentException("timeZoneId cannot be null");
    }
    try {
      this.zoneId = ZoneId.of(timeZoneId);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Unsupported timezone: " + timeZoneId, e);
    }
  }

  /**
   * Sets the calendar's timezone using a ZoneId.
   *
   * @param zoneId the new timezone.
   * @throws IllegalArgumentException if zoneId is null.
   */
  public void setZoneId(ZoneId zoneId) {
    if (zoneId == null) {
      throw new IllegalArgumentException("zoneId cannot be null");
    }
    this.zoneId = zoneId;
  }

  @Override
  public ZonedDateTime convertTimeFromOneTimeZoneToAnother(LocalDateTime time, ZoneId currentZoneId,
                                                           ZoneId newZoneId) {
    Objects.requireNonNull(time, "time cannot be null");
    Objects.requireNonNull(currentZoneId, "currentZoneId cannot be null");
    Objects.requireNonNull(newZoneId, "newZoneId cannot be null");
    ZonedDateTime currentZonedTime = time.atZone(currentZoneId);
    return currentZonedTime.withZoneSameInstant(newZoneId);
  }

  /**
   * Converts a LocalDateTime from one timezone to another and returns the LocalDateTime
   * in the target timezone. This is useful for copy operations where events need to be
   * converted between calendars with different timezones.
   *
   * <p>Example: Converting 2pm EST to PST results in 11am PST (same instant, different local time).
   *
   * @param time the LocalDateTime to convert (interpreted in currentZoneId).
   * @param currentZoneId the timezone of the input time.
   * @param newZoneId the target timezone.
   * @return the LocalDateTime in the target timezone (same instant, different local time).
   */
  @Override
  public LocalDateTime convertToLocalDateTime(LocalDateTime time, ZoneId currentZoneId,
                                              ZoneId newZoneId) {
    ZonedDateTime zonedResult = convertTimeFromOneTimeZoneToAnother(time, currentZoneId, newZoneId);
    return zonedResult.toLocalDateTime();
  }

  /** Delegates to the underlying in-memory calendar. */
  @Override
  public List<Event> eventsOn(LocalDate date) {
    return delegate.eventsOn(date);
  }

  /** Delegates to the underlying in-memory calendar. */
  @Override
  public List<Event> eventsOverlapping(LocalDateTime from, LocalDateTime to) {
    return delegate.eventsOverlapping(from, to);
  }

  /** Delegates to the underlying in-memory calendar. */
  @Override
  public List<Event> allEvents() {
    return delegate.allEvents();
  }

  @Override
  public EventId create(EventDraft draft) {
    return delegate.create(draft);
  }

  @Override
  public SeriesId createSeries(SeriesDraft draft) {
    return delegate.createSeries(draft);
  }

  @Override
  public void updateBySelector(EventSelector selector, EventPatch patch, EditScope scope) {
    delegate.updateBySelector(selector, patch, scope);
  }

  @Override
  public BusyStatus statusAt(LocalDateTime instant) {
    return delegate.statusAt(instant);
  }

}
