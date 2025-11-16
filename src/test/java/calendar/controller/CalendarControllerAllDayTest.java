package calendar.controller;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * Strengthens assertions for all-day create branches to kill mutants.
 */
public class CalendarControllerAllDayTest {
  private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
      "create calendar --name school --timezone America/New_York",
      "use calendar --name school");

  private String runWithInput(String input) throws Exception {
    PrintStream originalOut = System.out;
    java.io.InputStream originalIn = System.in;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
    ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    try {
      System.setOut(ps);
      System.setIn(bais);

      Class<?> runner = Class.forName("CalendarRunner");
      Method main = runner.getMethod("main", String[].class);

      String [] args = new String[]{
          "--mode", "interactive"
      };

      main.invoke(null, (Object) args);
    } finally {
      System.setOut(originalOut);
      System.setIn(originalIn);
    }

    return baos.toString(StandardCharsets.UTF_8);
  }

  @Test
  public void allDaySinglePrintsTimes() throws Exception {
    String out = runWithInput(String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "create event allday1 on 2025-12-01",
        "print events on 2025-12-01",
        "exit"));
    assertTrue(out.contains("- allday1 from 08:00 to 17:00"));
  }

  @Test
  public void allDayRepeatnPrintsTimes() throws Exception {
    String out = runWithInput(String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "create event allday2 on 2025-12-01 repeats MW for 2 times",
        "print events on 2025-12-01",
        "exit"));
    assertTrue(out.contains("- allday2 from 08:00 to 17:00"));
  }

  @Test
  public void allDayRepeatUntilPrintsTimes() throws Exception {
    String out = runWithInput(String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "create event allday3 on 2025-12-01 repeats MTWRF until 2025-12-03",
        "print events on 2025-12-01",
        "exit"));
    assertTrue(out.contains("- allday3 from 08:00 to 17:00"));
  }
}

