package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import org.junit.Test;

/**
 * Forces createSeries to throw ValidationException so the controller's catch blocks
 * and safePrintMessage calls are covered.
 */
public class CalendarControllerSeriesValidationCatchTest {
  private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
      "create calendar --name school --timezone America/New_York",
      "use calendar --name school");

  private static String runWith(String commands) throws Exception {
    StringBuilder out = new StringBuilder();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(DEFAULT_CALENDAR_SETUP + "\n" + commands), out);
    CalendarView view = new CalendarViewImpl(out);
    controller.go(view);
    return out.toString();
  }

  @Test
  public void repeatN_seriesValidation_printsInvalidFields() throws Exception {
    String out = runWith(String.join("\n",
        "create event sub from 2025-11-03T10:00 to 2025-11-03T11:00 repeats MW for 0 times",
        "exit"));
    assertTrue(out.contains("Fields are invalid"));
  }

  @Test
  public void repeatUntil_seriesValidation_printsInvalidFields() throws Exception {
    String out = runWith(String.join("\n",
        "create event sub from 2025-11-03T10:00 to 2025-11-03T11:00 repeats MW until 2025-11-01",
        "exit"));
    assertTrue(out.contains("Fields are invalid"));
  }

  @Test
  public void allDayRepeatN_seriesValidation_printsInvalidFields() throws Exception {
    String out = runWith(String.join("\n",
        "create event sub on 2025-12-01 repeats MW for 0 times",
        "exit"));
    assertTrue(out.contains("Fields are invalid"));
  }

  @Test
  public void allDayRepeatUntil_seriesValidation_printsInvalidFields() throws Exception {
    String out = runWith(String.join("\n",
        "create event sub on 2025-12-05 repeats MTWRF until 2025-12-01",
        "exit"));
    assertTrue(out.contains("Fields are invalid"));
  }
}
