package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * Covers single-edit error paths in the controller: NotFound, Conflict, IllegalArgumentException.
 */
public class CalendarControllerSingleEditErrorPathsTest {

  private static String runWithInput(String input) throws Exception {
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    PrintStream outPs = new PrintStream(outBuf, true, StandardCharsets.UTF_8);
    ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    CalendarView view = new CalendarViewImpl(outPs);
    CalendarController controller = new CalendarControllerImpl(new InputStreamReader(in), outPs);
    controller.go(view);
    return outBuf.toString(StandardCharsets.UTF_8);
  }

  private static String runWithDefaultInput(String input) throws Exception {
    String prefixed = String.join("\n",
        "create calendar --name school --timezone America/New_York",
        "use calendar --name school",
        input);
    return runWithInput(prefixed);
  }

  @Test
  public void singleEdit_notFound_and_invalidStatus_and_conflict_areHandled() throws Exception {
    StringBuilder sb = new StringBuilder();
    // Seed two events on 2025-05-05
    sb.append("create event \"A\" from 2025-05-05T10:00 to 2025-05-05T11:00\n");
    sb.append("create event \"B\" from 2025-05-05T11:00 to 2025-05-05T12:00\n");

    sb.append("edit event subject \"NO_SUCH\" from 2025-05-05T10:00 to 2025-05-05T11:00 with X\n");

    sb.append("edit event status \"A\" from 2025-05-05T10:00 to 2025-05-05T11:00 with BAD\n");

    sb.append(
        "edit event start \"B\" from 2025-05-05T11:00 to 2025-05-05T12:00 with 2025-05-05T10:00\n");
    sb.append(
        "edit event end \"B\" from 2025-05-05T10:00 to 2025-05-05T12:00 with 2025-05-05T11:00\n");
    sb.append(
        "edit event subject \"B\" from 2025-05-05T10:00 to 2025-05-05T11:00 with \"A\"\n");

    sb.append("exit\n");

    String out = runWithDefaultInput(sb.toString());

    assertTrue(out.contains("No matching event found:"));
    assertTrue(out.contains("Invalid property value:"));
    assertTrue(out.contains("Update failed due to event conflict:"));
  }
}
