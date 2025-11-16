package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import org.junit.Test;

/**
 * Covers the invalid branch in handleShowStatus (regex mismatch).
 */
public class CalendarControllerShowStatusInvalidTest {
  @Test
  public void showStatusInvalidFormat_printsError() throws Exception {
    String commands = String.join("\n",
        "create calendar --name school --timezone America/New_York",
        "use calendar --name school",
        "show status on 2025-10-27",
        "exit");
    StringBuilder out = new StringBuilder();
    CalendarController c = new CalendarControllerImpl(new StringReader(commands), out);
    CalendarView v = new CalendarViewImpl(out);

    c.go(v);

    assertTrue(out.toString().contains("Error: Invalid print command format."));
  }
}
