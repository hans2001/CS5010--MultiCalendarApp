package calendar.export;

import static org.testng.Assert.assertTrue;

import calendar.controller.CalendarControllerImpl;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.view.CalendarView;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;

/**
 * Tests for io failure coverage.
 */
public class CoverageTests {

  /**
   * A CalendarView that gives io exceptions.
   */
  private static class FailingView implements CalendarView {
    @Override
    public void printMessage(String msg) throws IOException {
      throw new IOException("Forced failure");
    }

    @Override
    public void printEventsOn(LocalDate date, List<Event> events) {

    }

    @Override
    public void printEventsFromTo(LocalDateTime from, LocalDateTime to, List<Event> events) {

    }

    @Override
    public void printStatus(BusyStatus status) {

    }

    public void printStatus(Object status) throws IOException {
      throw new IOException("Forced failure");
    }
  }

  @Test
  public void test() throws IOException {
    StringReader input = new StringReader(
        "create calendar work\n"
            +
            "use calendar work\n"
            +
            "export cals test.csv\n"
            +
            "exit\n"
    );

    StringBuilder output = new StringBuilder();

    CalendarControllerImpl controller = new CalendarControllerImpl(input, output);

    CalendarView failingView = new FailingView();

    controller.go(failingView);
  }

  @Test
  public void testExportByExtension_icsCallsIcalExporter() throws IOException {
    CalendarControllerImpl controller =
        new CalendarControllerImpl(new StringReader(""), new StringBuilder());

    List<Event> events = List.of();

    Path temp = Files.createTempFile("test", ".ics");

    Path exported = controller.exportByExtension(temp, events);

    String content = Files.readString(exported);

    assertTrue(content.contains("BEGIN:VCALENDAR"),
        "Expected iCal exporter to produce VCALENDAR content");
  }

  @Test
  public void testExportByExtension_icalCallsIcalExporter() throws IOException {
    CalendarControllerImpl controller =
        new CalendarControllerImpl(new StringReader(""), new StringBuilder());

    List<Event> events = List.of();

    Path temp = Files.createTempFile("test", ".ical");

    Path exported = controller.exportByExtension(temp, events);

    String content = Files.readString(exported);

    assertTrue(content.contains("BEGIN:VCALENDAR"),
        "Expected iCal exporter to handle .ical extension as well");
  }


}
