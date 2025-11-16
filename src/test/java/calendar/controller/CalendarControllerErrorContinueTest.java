package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import org.junit.Test;

/**
 * Tests that invalid input prints an error and the loop continues.
 */
public class CalendarControllerErrorContinueTest {
  private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
      "create calendar --name school --timezone America/New_York",
      "use calendar --name school");

  @Test
  public void invalidCommandPrintsErrorAndLoopContinues() throws Exception {
    StringBuilder out = new StringBuilder();
    CalendarController c = new CalendarControllerImpl(new StringReader(String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "invalid blah blah",
        "print events on 2025-11-01",
        "exit")), out);
    CalendarView v = new CalendarViewImpl(out);
    c.go(v);
    String s = out.toString();
    assertTrue(s.contains("Error: Invalid command"));
    assertTrue(s.contains("Events on 2025-11-01:"));
  }
}
