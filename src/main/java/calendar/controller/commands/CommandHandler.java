package calendar.controller.commands;

import calendar.view.CalendarView;
import java.io.IOException;

/**
 * Takes in user input and maps to a command.
 */
public interface CommandHandler {
  /**
   * Handles command inputted.
   *
   * @param input user input.
   * @param view view.
   *
   * @throws IOException handles errors.
   */
  void handle(String input, CalendarView view) throws IOException;
}
