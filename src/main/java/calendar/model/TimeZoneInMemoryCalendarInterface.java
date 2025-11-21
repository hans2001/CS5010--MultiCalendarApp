package calendar.model;

import calendar.model.api.CalendarApi;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * A Calendar that has a timezone and name.
 *
 * <p>Extends {@link CalendarApi} to add timezone-aware calendar functionality.
 * This interface provides methods for calendar identification and timezone conversion.
 */
public interface TimeZoneInMemoryCalendarInterface extends CalendarApi {
  /**
   * Returns the calendar's name.
   *
   * @return the calendar name.
   */
  String getName();

  /**
   * Returns the calendar's timezone.
   *
   * @return the timezone.
   */
  ZoneId getZoneId();

  /**
   * Sets the calendar's name.
   *
   * @param name the new name (must be non-null and non-blank).
   * @throws IllegalArgumentException if name is null or blank.
   */
  void setName(String name);

  /**
   * Sets the calendar's timezone using a ZoneId.
   *
   * @param zoneId the new timezone.
   * @throws IllegalArgumentException if zoneId is null.
   */
  void setZoneId(ZoneId zoneId);

  /**
   * Converts a LocalDateTime in one TimeZone into a ZonedDateTime in another TimeZone.
   * (So 11:30 PM PST is converted to 2:30AM EST in the next day since EST is 3 hrs ahead of PST).
   *
   * @param time          to be converted (interpreted in currentZoneId).
   * @param currentZoneId current time zone.
   * @param newZoneId     new time zone to be converted into.
   * @return the ZonedDateTime in the new time zone (same instant, different local time).
   */
  ZonedDateTime convertTimeFromOneTimeZoneToAnother(LocalDateTime time, ZoneId currentZoneId,
                                                     ZoneId newZoneId);

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
  LocalDateTime convertToLocalDateTime(LocalDateTime time, ZoneId currentZoneId, ZoneId newZoneId);
}
