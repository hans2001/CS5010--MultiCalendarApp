package calendar.view;

import java.time.LocalDate;

/**
 * High-level callbacks exposed by the GUI view.
 */
public interface CalendarGuiFeatures {
  /** Move to the previous month. */
  void goToPreviousMonth();

  /** Move to the next month. */
  void goToNextMonth();

  /** User clicked "New Event". */
  void requestEventCreation();

  /** User clicked "New Calendar". */
  void requestCalendarCreation();

  /** User clicked "Edit Calendar". */
  void requestCalendarEdit();

  /** User selected a calendar from the dropdown. */
  void calendarSelected(String name);

  /** User clicked on a specific day in the month grid. */
  void daySelected(LocalDate date);
}
