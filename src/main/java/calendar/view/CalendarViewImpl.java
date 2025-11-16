package calendar.view;

import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implements Calendar View and prints out responses to users.
 */
public class CalendarViewImpl implements CalendarView {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
  private final Appendable output;

  /**
   * Creates a view for printing out output.
   *
   * @param output the output system we use (ex file, system, etc.).
   */
  public CalendarViewImpl(Appendable output) {
    this.output = output;
  }

  /**
   * Prints a message.
   *
   * @param message to output.
   */
  public void printMessage(String message) throws IOException {
    try {
      output.append(message).append(System.lineSeparator());
    } catch (IOException e) {
      throw new IOException("Error with outputting: ", e);
    }
  }

  private void appendLine(String text) {
    try {
      output.append(text).append(System.lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException("Failed to append output", e);
    }
  }

  /**
   * Gets all the events on the current date and prints them. (Or none message if none).
   *
   * @param date we query.
   * @param events on this date.
   */
  @Override
  public void printEventsOn(LocalDate date, List<Event> events) {
    appendLine("Events on " + date.format(DATE_FORMAT) + ":");
    for (Event e : events) {
      String startTime = e.start().format(TIME_FORMAT);
      String endTime = e.end().format(TIME_FORMAT);
      String location = e.location().isPresent() ? " at " + e.location().get() : "";

      appendLine("- " + e.subject() + " from " + startTime + " to " + endTime + location);
    }

    appendLine("");
  }

  @Override
  public void printEventsFromTo(LocalDateTime from, LocalDateTime to, List<Event> events) {
    appendLine("Events from " + from.format(DATE_FORMAT) + " " + from.format(TIME_FORMAT)
        + " to " + to.format(DATE_FORMAT) + " " + to.format(TIME_FORMAT) + ":");

    for (Event e : events) {
      String startDate = e.start().format(DATE_FORMAT);
      String startTime = e.start().format(TIME_FORMAT);
      String endDate = e.end().format(DATE_FORMAT);
      String endTime = e.end().format(TIME_FORMAT);
      String location = e.location().isPresent() ? " at " + e.location().get() : "";

      appendLine("- " + e.subject()
          + " starting on " + startDate + " at " + startTime
          + ", ending on " + endDate + " at " + endTime
          + location);
    }

    appendLine("");
  }

  @Override
  public void printStatus(BusyStatus status) {
    appendLine(status == BusyStatus.BUSY ? "User is BUSY" : "User is not BUSY");
  }
}
