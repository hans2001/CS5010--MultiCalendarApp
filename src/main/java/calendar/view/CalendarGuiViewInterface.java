package calendar.view;

import java.awt.event.ActionListener;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

/**
 * How the controller and view should interact.
 */
public interface CalendarGuiViewInterface {
  /**
   * Make the view visible.
   */
  void makeVisible();

  /**
   * Draws the current month.
   */
  void drawMonth(YearMonth month);

  /**
   * Set an actionListener to a button, giving the button functionality.
   *
   * @param actionEvent event to be fired.
   */
  void setCommandButtonListener(ActionListener actionEvent);

  /**
   * Display an error popup if the user made an invalid event.
   *
   * @param message the error message.
   */
  void showError(String message);

  /**
   * Display a message.
   *
   * @param message the message.
   */
  void showMessage(String message);

  /**
   * Prompts the user to create a new calendar.
   *
   * @return String array where first value is name, second is timezone.
   * */
  String[] promptNewCalendar();

  /**
   * Sets the current calendar name.
   *
   * @param name of calendar.
   */
  void setActiveCalendarName(String name);

  /**
   * Sets the current calendar timezone.
   *
   * @param tz of calendar.
   */
  void setActiveCalendarTimezone(String tz);

  /**
   * Add a new calendar to the dropdown list of calendars.
   *
   * @param name of new calendar.
   */
  void addCalendarToCalendarList(String name);

  /**
   * Set the selector list to this calendar name.
   *
   * @param name of calendar.
   */
  void selectCalendarOnCalendarSelector(String name);

  /**
   * Get the selected calendar name.
   *
   * @return name of selected calendar.
   */
  String getSelectedCalendarName();

  /**
   * Displays the box to edit the calendar.
   *
   * @return index zero is edited name, index one is edited timezone
   */
  String[] displayEditCalendar(String calendarName, String calendarTz);
}
