package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import org.junit.Test;

/**
 * Tests for invalid command format branches that should print error messages.
 */
public class CalendarControllerInvalidFormatTests {

  private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
      "create calendar --name school --timezone America/New_York",
      "use calendar --name school");

  private static String run(String commands) throws Exception {
    StringBuilder out = new StringBuilder();
    CalendarController c = new CalendarControllerImpl(
        new StringReader(DEFAULT_CALENDAR_SETUP + "\n" + commands), out);
    CalendarView v = new CalendarViewImpl(out);
    c.go(v);
    return out.toString();
  }

  @Test
  public void invalidCreateFormat_printsError() throws Exception {
    String out = run(String.join("\n",
        "create event",
        "exit"));
    assertTrue(out.contains("Error: Invalid create event command format."));
  }

  @Test
  public void invalidEditFormat_printsError() throws Exception {
    String out = run(String.join("\n",
        "edit foo bar",
        "exit"));
    assertTrue(out.contains("Error: Invalid edit command format."));
  }
}
