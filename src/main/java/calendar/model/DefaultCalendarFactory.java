package calendar.model;

/**
 * Default factory implementation for creating TimeZoneInMemoryCalendar instances.
 */
public class DefaultCalendarFactory implements CalendarFactory {
  @Override
  public TimeZoneInMemoryCalendarInterface create(String timezone, String name) {
    return new TimeZoneInMemoryCalendar(timezone, name);
  }
}

