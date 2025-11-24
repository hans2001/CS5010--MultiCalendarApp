package calendar.model;

import java.time.YearMonth;

/**
 * Interface for Calendar with GUI features.
 */
public interface GuiCalendarInterface {
  /**
   * Get the current month.
   */
  YearMonth getMonth();

  /**
   * Get the next month.
   */
  YearMonth getNextMonth();

  /**
   * Get the previous month.
   */
  YearMonth getPreviousMonth();

  /**
   * Gets name.
   *
   * @return name of calendar.
   */
  String getName();

  /**
   * Gets tz.
   *
   * @return tz of calendar.
   */
  String getZoneId();

  /**
   * Switch the current calendar for a new one.
   *
   * @param calendar new calendar.
   */
  void switchCalendar(TimeZoneInMemoryCalendarInterface calendar);
}
