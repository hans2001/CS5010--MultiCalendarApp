package calendar.view;

import java.awt.event.ActionListener;
import java.time.YearMonth;
import java.util.List;

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
}
