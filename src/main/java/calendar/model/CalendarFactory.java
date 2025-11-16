package calendar.model;

/**
 * Factory for creating calendar instances.
 * Enables weak coupling by allowing CalendarManager to create calendars
 * without depending on concrete implementations.
 */
public interface CalendarFactory {
  /**
   * Creates a new calendar instance.
   *
   * @param timezone IANA timezone (e.g., "America/New_York").
   * @param name calendar name.
   * @return new calendar instance.
   * @throws IllegalArgumentException if timezone or name is invalid.
   */
  TimeZoneInMemoryCalendarInterface create(String timezone, String name);
}

