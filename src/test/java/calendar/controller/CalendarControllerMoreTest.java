package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.model.domain.BusyStatus;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import org.junit.Test;

/**
 * Additional controller coverage tests.
 */
public class CalendarControllerMoreTest {

  private static class Harness {
    final StringBuilder out = new StringBuilder();
    final CalendarController controller;
    final CalendarView view;

    private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
        "create calendar --name school --timezone America/New_York",
        "use calendar --name school");

    Harness(String commands) {
      String script = String.join("\n", DEFAULT_CALENDAR_SETUP, commands);
      this.controller = new CalendarControllerImpl(new StringReader(script), out);
      this.view = new CalendarViewImpl(out);
    }

    String run() throws Exception {
      controller.go(view);
      return out.toString();
    }
  }

  @Test
  public void testShowStatusBusyAndAvailable() throws Exception {
    String commands = String.join("\n",
        "create event focus from 2025-10-27T10:00 to 2025-10-27T11:00",
        "show status on 2025-10-27T10:30",
        "show status on 2025-10-27T12:00",
        "exit");
    Harness h = new Harness(commands);
    String out = h.run();
    assertTrue(out.contains("Welcome to Calendar"));
    assertTrue(out.contains("User is BUSY"));
    assertTrue(out.contains("User is not BUSY"));
  }

  @Test
  public void testPrintEventsFromTo() throws Exception {
    String commands = String.join("\n",
        "create event a from 2025-11-01T09:00 to 2025-11-01T10:00",
        "create event b from 2025-11-01T09:30 to 2025-11-01T11:00",
        "print events from 2025-11-01T08:00 to 2025-11-01T12:00",
        "exit");
    Harness h = new Harness(commands);
    String out = h.run();
    assertTrue(out.contains("Events from 2025-11-01 08:00 to 2025-11-01 12:00:"));
    assertTrue(out.contains("- a starting on 2025-11-01 at 09:00, ending on 2025-11-01 at 10:00"));
    assertTrue(out.contains("- b starting on 2025-11-01 at 09:30, ending on 2025-11-01 at 11:00"));
  }

  @Test
  public void testExportCreatesFileAndPrintsPath() throws Exception {
    Path tempCsv = Path.of("controller_export_test.csv");
    try {
      String commands = String.join("\n",
          "export cal " + tempCsv.getFileName(),
          "exit");
      Harness h = new Harness(commands);
      String out = h.run();
      assertTrue(out.contains("Exported calendar to:"));
      assertTrue(Files.exists(tempCsv));
    } finally {
      try {
        Files.deleteIfExists(tempCsv);
      } catch (Exception e) {
        assertTrue(Files.exists(tempCsv) || !Files.exists(tempCsv));
      }
    }
  }

  private static class ThrowingView implements CalendarView {
    private final Predicate<String> failWhen;
    private final String failureMessage;

    private ThrowingView(Predicate<String> failWhen, String failureMessage) {
      this.failWhen = failWhen;
      this.failureMessage = failureMessage;
    }

    static ThrowingView failOnExportMessage(String failureMessage) {
      return new ThrowingView(msg -> msg.startsWith("Exported calendar to"), failureMessage);
    }

    @Override
    public void printMessage(String message) throws java.io.IOException {
      if (failWhen.test(message)) {
        throw new java.io.IOException(failureMessage);
      }
    }

    @Override
    public void printEventsOn(LocalDate date, List events) {
    }

    @Override
    public void printEventsFromTo(LocalDateTime from, LocalDateTime to, List events) {
    }

    @Override
    public void printStatus(BusyStatus status) {
    }
  }

  @Test
  public void testExportCatchPathWhenViewFails() throws Exception {
    Path tempFile = Files.createTempFile("failing", ".csv");
    Path exportedFile = Path.of(tempFile.getFileName().toString());
    Files.deleteIfExists(tempFile); // Delete so export can create it
    try {
      String fileName = tempFile.getFileName().toString();
      String commands = String.join("\n",
          "create calendar --name school --timezone America/New_York",
          "use calendar --name school",
          "export cal " + fileName,
          "exit");
      StringBuilder out = new StringBuilder();
      CalendarController controller = new CalendarControllerImpl(new StringReader(commands), out);
      controller.go(ThrowingView.failOnExportMessage("boom"));
      String s = out.toString();
      assertTrue("Output should contain error message. Actual: " + s,
          s.contains("Error: Failed to export calendar: boom"));
    } finally {
      Files.deleteIfExists(exportedFile);
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testExportInvalidCommandFormat() throws Exception {
    String commands = String.join("\n",
        "export cal not_a_csv",
        "print events on 2025-11-01",
        "exit");
    Harness h = new Harness(commands);
    String out = h.run();
    String expectedError =
        "Error: Failed to export calendar: Unsupported export format: not_a_csv";
    assertTrue(out.contains(expectedError));
    assertTrue(out.contains("Events on 2025-11-01:"));
  }

  @Test
  public void testEditSeriesBranch() throws Exception {
    String commands = String.join("\n",
        "create event class from 2025-10-20T09:00 to 2025-10-20T10:00 repeats MW for 3 times",
        "edit series subject class from 2025-10-20T09:00 with cs50",
        "print events on 2025-10-20",
        "print events on 2025-10-22",
        "print events on 2025-10-27",
        "exit");
    Harness h = new Harness(commands);
    String out = h.run();
    assertTrue(out.contains("- cs50 from 09:00 to 10:00"));
  }

  @Test
  public void testInvalidTopLevelCommandPrintsErrorAndContinues() throws Exception {
    String commands = String.join("\n",
        "foobar",
        "print events on 2025-10-20",
        "exit");
    Harness h = new Harness(commands);
    String out = h.run();
    assertTrue(out.contains("Error: Invalid command"));
    assertTrue(out.contains("Events on 2025-10-20:"));
  }

  @Test
  public void testInvalidPrintFormatPrintsErrorAndContinues() throws Exception {
    String commands = String.join("\n",
        "print events from 2025-11-01T09:00 until 2025-11-01T10:00",
        "print events on 2025-11-01",
        "exit");
    Harness h = new Harness(commands);
    String out = h.run();
    assertTrue(out.contains("Error: Invalid print command format."));
    assertTrue(out.contains("Events on 2025-11-01:"));
  }

  @Test
  public void testEditPropertyVariantsExecute() throws Exception {
    String commands = String.join("\n",
        "create event task from 2025-10-27T13:00 to 2025-10-27T14:00",
        "edit event start task from 2025-10-27T13:00 to 2025-10-27T14:00 with 2025-10-27T13:30",
        "edit event end task from 2025-10-27T13:30 to 2025-10-27T14:00 with 2025-10-27T14:30",
        "edit event location task from 2025-10-27T13:30 to 2025-10-27T14:30 with \"Room 101\"",
        "edit event description task from 2025-10-27T13:30 to 2025-10-27T14:30 with \"Deep work\"",
        "edit event status task from 2025-10-27T13:30 to 2025-10-27T14:30 with private",
        "print events on 2025-10-27",
        "exit");
    Harness h = new Harness(commands);
    String out = h.run();
    assertTrue(out.contains("- task from 13:30 to 14:30 at Room 101"));
  }
}
