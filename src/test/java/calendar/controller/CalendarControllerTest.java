package calendar.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * Tests the controller.
 */
public class CalendarControllerTest {
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

  /**
   * create event subject from dateStringTtimeString to dateStringTtimeString.
   */
  @Test
  public void testCreateSingleEvent() throws Exception {
    String input = DEFAULT_CALENDAR_SETUP + "\n"
        + "create event school from 2025-10-27T13:27 to 2025-10-27T15:27\n"
        + "print events on 2025-10-27\nexit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Welcome to Calendar"));
    assertTrue(result.contains("Events on 2025-10-27:"));
    assertTrue(result.contains("- school from 13:27 to 15:27"));
  }

  /**
   * create event subject from dateStringTtimeString to dateStringTtimeString repeats
   * weekdays for N times.
   */
  @Test
  public void testRepeat() throws Exception {
    String input = DEFAULT_CALENDAR_SETUP + "\n"
        + "create event workout from 2025-11-03T10:00 to 2025-11-10T11:00"
        + " repeats MW for 3 times\nprint events on 2025-11-03\nexit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Welcome to Calendar"));
    assertTrue(result.contains("Events on 2025-11-03:"));
    assertTrue(result.contains("- workout from 10:00 to 11:00"));
  }

  /**
   * create event eventSubject from dateStringTtimeString to dateStringTtimeString
   * repeats weekdays until dateString.
   */
  @Test
  public void testCreateRepeatUntilDate() throws Exception {
    String input = DEFAULT_CALENDAR_SETUP + "\n"
        + "create event study from 2025-11-03T10:00 to 2025-11-01T11:00 "
        + "repeats MW until 2025-11-20\nprint events on 2025-11-03\nexit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Welcome to Calendar"));
    assertTrue(result.contains("Events on 2025-11-03:"));
    assertTrue(result.contains("- study from 10:00 to 11:00"));
  }

  /**
   * create event eventSubject on dateString.
   */
  @Test
  public void testCreateAllDaySingle() throws Exception {
    String input = DEFAULT_CALENDAR_SETUP + "\n"
        + "create event meeting on 2025-12-01\nprint events on 2025-12-01\nexit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Welcome to Calendar"));
    assertTrue(result.contains("Events on 2025-12-01:"));
  }

  /**
   * create event eventSubject on dateString repeats weekdays until dateString.
   */
  @Test
  public void testAlldaysrepeat() throws Exception {
    String input = DEFAULT_CALENDAR_SETUP + "\n"
        + "create event yoga on 2025-12-01 repeats MW for 5 times\n"
        + "print events on 2025-12-01\nexit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Welcome to Calendar"));
    assertTrue(result.contains("Events on 2025-12-01:"));
  }

  /**
   * create event eventSubject on dateString repeats weekdays until dateString.
   */
  @Test
  public void testCreateAllDayRepeatUntilDate() throws Exception {
    String input = DEFAULT_CALENDAR_SETUP + "\n"
        + "create event \"holiday celebration\" on 2025-12-01 repeats "
        + "MTWRF until 2025-12-10\nexit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Welcome to Calendar"));
  }

  /**
   * Tests editing a single event's subject.
   */
  @Test
  public void testEditSingleEvent() throws Exception {
    String input = String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "create event school from 2025-10-27T13:27 to 2025-10-27T15:27",
        "print events on 2025-10-27",
        "edit event subject school from 2025-10-27T13:27 to 2025-10-27T15:27 with study",
        "print events on 2025-10-27",
        "exit");
    String out = runWithInput(input);
    assertTrue(out.contains("- school from 13:27 to 15:27"));
    assertTrue(out.contains("- study from 13:27 to 15:27"));
    assertFalse(out.contains("- class from 13:27 to 15:27"));
  }

  /**
   * Tests editing multiple events in a series (edit events).
   */
  @Test
  public void testEditEventsInSeries() throws Exception {
    String input = DEFAULT_CALENDAR_SETUP + "\n"
        + "create event class from 2025-10-20T09:00 to 2025-10-20T10:00 repeats MW for 3 times\n"
            + "edit events subject class from 2025-10-20T09:00 with classcs50\n"
            + "print events on 2025-10-20\n"
            + "print events on 2025-10-22\n"
            + "print events on 2025-10-27\n"
            + "exit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Events on 2025-10-20:"));
    assertTrue(result.contains("- classcs50 from 09:00 to 10:00"));
    assertTrue(result.contains("- classcs50 from 09:00 to 10:00"));
    assertTrue(result.contains("- classcs50 from 09:00 to 10:00"));
    assertFalse(result.contains("- class from 09:00 to 10:00"));
  }
}
