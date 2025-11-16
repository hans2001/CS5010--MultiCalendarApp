package calendar.model;

import java.time.YearMonth;

/**
 * Interface for Calendar with GUI features.
 */
public interface GuiCalendarInterface extends TimeZoneInMemoryCalendarInterface {
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
}
