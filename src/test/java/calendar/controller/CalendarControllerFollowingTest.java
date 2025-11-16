package calendar.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import org.junit.Test;

/**
 * Tests FOLLOWING semantics for 'edit events'.
 */
public class CalendarControllerFollowingTest {
  private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
      "create calendar --name school --timezone America/New_York",
      "use calendar --name school");

  private String run(String commands) throws Exception {
    StringBuilder out = new StringBuilder();
    CalendarController c = new CalendarControllerImpl(
        new StringReader(DEFAULT_CALENDAR_SETUP + "\n" + commands), out);
    CalendarView v = new CalendarViewImpl(out);
    c.go(v);
    return out.toString();
  }

  @Test
  public void editEventsAffectsFollowingOnly() throws Exception {
    String commands = String.join("\n",
        "create event class from 2025-10-20T09:00 to 2025-10-20T10:00 repeats MW for 3 times",
        "edit events subject class from 2025-10-22T09:00 with cs50",
        "print events on 2025-10-20",
        "print events on 2025-10-22",
        "print events on 2025-10-27",
        "exit");
    String out = run(commands);
    assertTrue(out.contains("Events on 2025-10-20:"));
    assertTrue(out.contains("- class from 09:00 to 10:00"));
    assertFalse(out.contains("- cs50 from 09:00 to 10:00\n\nEvents on 2025-10-20:"));
    assertTrue(out.contains("Events on 2025-10-22:"));
    assertTrue(out.contains("- cs50 from 09:00 to 10:00"));
    assertTrue(out.contains("Events on 2025-10-27:"));
    assertTrue(out.contains("- cs50 from 09:00 to 10:00"));
  }
}
