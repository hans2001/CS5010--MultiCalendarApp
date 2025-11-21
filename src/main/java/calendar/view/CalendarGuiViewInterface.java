package calendar.view;

import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * How the controller and view should interact.
 */
public interface CalendarGuiViewInterface {
  /**
   * Makes the GUI visible.
   */
  void makeVisible();

  /**
   * Draws the requested month.
   *
   * @param month month/year to display.
   */
  void drawMonth(YearMonth month);

  /**
   * Highlights the provided date in the grid.
   *
   * @param date selected date.
   */
  void setSelectedDate(LocalDate date);

  /**
   * Shows events for the given date in the side panel.
   *
   * @param date day being displayed.
   * @param events formatted event lines.
   */
  void displayEvents(LocalDate date, List<String> events);

  /**
   * Prompts the user for event creation data prefilled with the date.
   *
   * @param date selected date.
   * @return CLI-style command or empty if cancelled/invalid.
   */
  Optional<String> promptForCreateEvent(LocalDate date);

  /**
   * Attaches the controller as the button listener.
   *
   * @param actionEvent shared listener.
   */
  void setCommandButtonListener(ActionListener actionEvent);

  /**
   * Displays an error dialog.
   *
   * @param message message text.
   */
  void showError(String message);

  /**
   * Displays an informational dialog.
   *
   * @param message message text.
   */
  void showMessage(String message);

  /**
   * Prompts for a new calendar's name/timezone.
   *
   * @return {name, timezone} or {@code null} if cancelled.
   */
  String[] promptNewCalendar();

  /**
   * Updates the active calendar label.
   *
   * @param name active calendar.
   */
  void setActiveCalendarName(String name);

  /**
   * Updates the active timezone label.
   *
   * @param tz timezone id.
   */
  void setActiveCalendarTimezone(String tz);

  /**
   * Adds a calendar to the selector dropdown.
   *
   * @param name calendar name.
   */
  void addCalendarToSelector(String name);

  /**
   * Updates a calendar name inside the selector.
   *
   * @param ogName original name.
   * @param newName updated name.
   */
  void editCalendarInSelector(String ogName, String newName);

  /**
   * Selects the named calendar in the dropdown.
   *
   * @param name calendar to select.
   */
  void selectCalendarOnCalendarSelector(String name);

  /**
   * Returns the calendar currently selected in the dropdown.
   *
   * @return calendar name.
   */
  String getSelectedCalendarName();

  /**
   * Prompts the user to edit a calendar's name/timezone.
   *
   * @param calendarName current name.
   * @param calendarTz current timezone.
   * @return array {originalName, newName, newTimezone}.
   */
  String[] displayEditCalendar(String calendarName, String calendarTz);
}
