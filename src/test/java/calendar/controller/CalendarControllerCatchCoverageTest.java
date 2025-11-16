package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import org.junit.Test;

/**
 * Tests to exercise controller catch blocks by inducing model/view exceptions.
 */
public class CalendarControllerCatchCoverageTest {
  private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
      "create calendar --name school --timezone America/New_York",
      "use calendar --name school");

  private String run(String commands) throws Exception {
    StringBuilder out = new StringBuilder();
    CalendarController c = new CalendarControllerImpl(new StringReader(commands), out);
    CalendarView v = new CalendarViewImpl(out);
    c.go(v);
    return out.toString();
  }

  private String runWithDefault(String commands) throws Exception {
    return run(DEFAULT_CALENDAR_SETUP + "\n" + commands);
  }

  @Test
  public void createSingle_validationException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event bad from 2025-11-01T10:00 to 2025-11-01T09:00",
        "exit"));
    assertTrue(out.contains("Fields are invalid"));
  }

  @Test
  public void createSingle_conflictException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event dup from 2025-11-01T10:00 to 2025-11-01T11:00",
        "create event dup from 2025-11-01T10:00 to 2025-11-01T11:00",
        "exit"));
    assertTrue(out.contains("Event conflict:"));
  }

  @Test
  public void createRepeatUntil_conflictException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event r1 from 2025-11-03T10:00 to 2025-11-03T11:00",
        "create event r1 from 2025-11-03T10:00 to 2025-11-03T11:00 repeats MW until 2025-11-10",
        "exit"));
    assertTrue(out.contains("Event conflict:"));
  }

  @Test
  public void createRepeatN_conflictException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event r2 from 2025-11-03T10:00 to 2025-11-03T11:00 repeats MW for 1 times",
        "create event r2 from 2025-11-03T10:00 to 2025-11-03T11:00 repeats MW for 1 times",
        "exit"));
    assertTrue(out.contains("Event conflict:"));
  }

  @Test
  public void createAllDay_validationException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event \"   \" on 2025-12-01",
        "exit"));
    assertTrue(out.contains("Fields are invalid"));
  }

  @Test
  public void createAllDayRepeatN_conflictException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event alldayx on 2025-12-01 repeats MW for 1 times",
        "create event alldayx on 2025-12-01 repeats MW for 1 times",
        "exit"));
    assertTrue(out.contains("Event conflict:"));
  }

  @Test
  public void editEvents_validationException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event e1 from 2025-10-27T10:00 to 2025-10-27T11:00",
        "create event e1 from 2025-10-27T10:00 to 2025-10-27T12:00",
        "edit events location e1 from 2025-10-27T10:00 with \"Room\"",
        "exit"));
    assertTrue(out.contains("Invalid update values:"));
  }

  @Test
  public void editEvents_conflictException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event A from 2025-10-27T10:00 to 2025-10-27T11:00",
        "create event B from 2025-10-27T10:00 to 2025-10-27T11:00",
        "edit events subject B from 2025-10-27T10:00 with A",
        "exit"));
    assertTrue(out.contains("Update failed due to event conflict:"));
  }

  @Test
  public void editSeries_notFound_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "edit series subject ghost from 2025-10-20T09:00 with x",
        "exit"));
    assertTrue(out.contains("No matching event found:"));
  }

  @Test
  public void editSeries_validationException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event cls from 2025-10-20T09:00 to 2025-10-20T10:00",
        "create event cls from 2025-10-20T09:00 to 2025-10-20T11:00",
        "edit series location cls from 2025-10-20T09:00 with \"Room\"",
        "exit"));
    assertTrue(out.contains("Invalid update values:"));
  }

  @Test
  public void editSeries_conflictException_printsMessage() throws Exception {
    String out = runWithDefault(String.join("\n",
        "create event anchor from 2025-10-20T10:30 to 2025-10-20T11:30",
        "create event cs from 2025-10-20T10:30 to 2025-10-20T11:30 repeats MW for 1 times",
        "edit series subject cs from 2025-10-20T10:30 with anchor",
        "exit"));
    assertTrue(out.contains("Update failed due to event conflict:"));
  }

  @Test
  public void export_modelThrows_printsError() throws Exception {
    String commands = String.join("\n",
        "export cal failing.txt", // unsupported extension triggers exception
        "exit");

    String out = runWithDefault(commands);
    String expectedMessage =
        "Error: Failed to export calendar: Unsupported export format: failing.txt";
    assertTrue(out.contains(expectedMessage));
  }

  @Test
  public void export_viewFailsAfterSuccess_appendsFallback() throws Exception {
    Path tempFile = Files.createTempFile("export_success", ".csv");
    Files.deleteIfExists(tempFile); // Delete so export can create it
    try {
      String fileName = tempFile.getFileName().toString();
      String commands = String.join("\n",
          DEFAULT_CALENDAR_SETUP,
          "export cal " + fileName,
          "exit");

      StringBuilder out = new StringBuilder();
      CalendarController controller = new CalendarControllerImpl(new StringReader(commands), out);
      CalendarView throwingView = new SelectiveThrowingView(
          msg -> msg.startsWith("Exported calendar to"), "view boom");

      controller.go(throwingView);
      String output = out.toString();
      assertTrue("Output should contain error message. Actual: " + output,
          output.contains("Error: Failed to export calendar: view boom"));
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void export_viewFailsAfterError_appendsFallback() throws Exception {
    String commands = String.join("\n",
        "export cal invalid.txt",
        "exit");

    StringBuilder out = new StringBuilder();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(DEFAULT_CALENDAR_SETUP + "\n" + commands), out);
    CalendarView throwingView = new SelectiveThrowingView(
        msg -> msg.startsWith("Error: Failed to export calendar"), "view fail");

    controller.go(throwingView);
    String expectedMessage =
        "Error: Failed to export calendar: Unsupported export format: invalid.txt";
    assertTrue(out.toString().contains(expectedMessage));
  }

  private static final class SelectiveThrowingView implements CalendarView {
    private final Predicate<String> shouldThrow;
    private final String failureMessage;

    private SelectiveThrowingView(Predicate<String> shouldThrow, String failureMessage) {
      this.shouldThrow = shouldThrow;
      this.failureMessage = failureMessage;
    }

    @Override
    public void printMessage(String message) throws IOException {
      if (shouldThrow.test(message)) {
        throw new IOException(failureMessage);
      }
    }

    @Override
    public void printEventsOn(LocalDate date, List<Event> events) {
      // no-op for tests
    }

    @Override
    public void printEventsFromTo(LocalDateTime from, LocalDateTime to, List<Event> events) {
      // no-op for tests
    }

    @Override
    public void printStatus(BusyStatus status) {
      // no-op for tests
    }
  }
}
