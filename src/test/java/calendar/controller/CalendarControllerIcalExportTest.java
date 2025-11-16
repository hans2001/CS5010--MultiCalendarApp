package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.model.config.CalendarSettings;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

/**
 * Verifies .ical export path.
 */
public class CalendarControllerIcalExportTest {

  @Test
  public void exportCreatesIcalFile() throws Exception {
    StringBuilder out = new StringBuilder();
    String commands = String.join(
        "\n",
        "create calendar --name school --timezone America/New_York",
        "use calendar --name school",
        "create event \"ics meeting\" from 2025-11-02T09:00 to 2025-11-02T10:30",
        "export cal sample_export.ical",
        "exit");
    CalendarSettings settings = CalendarSettings.defaults();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(commands), out, settings);
    CalendarView view = new CalendarViewImpl(out);

    controller.go(view);

    Path path = Path.of("sample_export.ical");
    try {
      List<String> lines = Files.readAllLines(path);
      assertTrue(lines.get(0).equals("BEGIN:VCALENDAR"));
      assertTrue(lines.stream().anyMatch(l -> l.startsWith("SUMMARY:ics meeting")));
    } finally {
      Files.deleteIfExists(path);
    }
  }
}
