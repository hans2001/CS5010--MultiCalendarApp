package calendar.controller.commands;

import calendar.model.TimeZoneInMemoryCalendar;
import calendar.view.CalendarView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles commands.
 */
public abstract class Command {
  /**
   * Splits the user command by "" or spaces to try and get keywords and params.
   *
   * @param input user input.
   * @return array of parts for each word in the input.
   */
  protected static String[] splitCommands(String input) {
    List<String> parts = new ArrayList<>();
    Matcher matcher = Pattern.compile("\"[^\"]+\"|\\S+").matcher(input);

    while (matcher.find()) {
      String part = matcher.group();
      if (part.startsWith("\"") && part.endsWith("\"")) {
        part = part.substring(1, part.length() - 1);
      }
      parts.add(part);
    }

    return parts.toArray(new String[0]);
  }

  abstract boolean matches(String input);

  abstract void execute(
      String input, TimeZoneInMemoryCalendar model, CalendarView view, Appendable output
  ) throws IOException;
}