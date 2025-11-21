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
   * Make the view visible.
   */
  void makeVisible();

  /**
   * Draws the current month.
   */
  void drawMonth(YearMonth month);

  /**
   * Highlights the selected date.
   *
   * @param date selected date.
   */
  void setSelectedDate(LocalDate date);

  /**
   * Displays events for the given date.
   *
   * @param date   date being displayed.
   * @param events formatted event lines.
   */
  void displayEvents(LocalDate date, List<String> events);

  /**
   * Prompts the user for event details tied to the provided date.
   *
   * @param date date to prefill.
   * @return formatted CLI command or empty if cancelled/invalid.
   */
  Optional<String> promptForCreateEvent(LocalDate date);

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
}
