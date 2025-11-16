package calendar.view;

import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Outputs information to the user to showcase the actions to take and taken.
 */
public interface CalendarView {
  /**
   * Prints a message.
   *
   * @param message to output.
   */
  void printMessage(String message) throws IOException;

  /**
   * Gets all the events on the current date and prints them. (Or none message if none).
   *
   * @param date we query.
   * @param events on this date.
   */
  void printEventsOn(LocalDate date, List<Event> events);

  /**
   * Gets all the events on a certain interval and prints them. (Or none message if none).
   *
   * @param from starting date we query.
   * @param to ending date we query.
   * @param events on this date.
   */
  void printEventsFromTo(LocalDateTime from, LocalDateTime to, List<Event> events);

  /**
   * Checks if the user is busy or not at this date time.
   *
   * @param status of the user, BUSY or AVAILABLE.
   */
  void printStatus(BusyStatus status);
}
