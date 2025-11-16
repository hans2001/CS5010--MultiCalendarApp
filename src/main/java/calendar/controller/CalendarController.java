package calendar.controller;

import calendar.view.CalendarView;
import java.io.IOException;

/**
 * Controller for the Calendar. Maps user input to model interactions.
 * Runs an event loop that listens for user input until they exit.
 */
public interface CalendarController {
  /**
   * Starts off the calendar event loop where users can enter commands.
   *
   * @param view the UI/CLI view used to present feedback to the user.
   * @throws IOException if commands are invalid.
   */
  void go(CalendarView view) throws IOException;
}