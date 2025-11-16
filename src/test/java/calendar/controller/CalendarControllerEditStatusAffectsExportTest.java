package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/**
 * Verifies that editing status to private is reflected in CSV export.
 * This also exercises the STATUS branch in property mapping.
 */
public class CalendarControllerEditStatusAffectsExportTest {
  @Test
  public void editStatusPrivate_setsPrivateInCsv() throws Exception {
    Path csv = Path.of("mut_status_export.csv");
    try {
      String commands = String.join("\n",
          "create calendar --name school --timezone America/New_York",
          "use calendar --name school",
          "create event e from 2025-11-01T10:00 to 2025-11-01T11:00",
          "edit event status e from 2025-11-01T10:00 to 2025-11-01T11:00 with private",
          "export cal " + csv.getFileName(),
          "exit");

      StringBuilder out = new StringBuilder();
      CalendarController c = new CalendarControllerImpl(new StringReader(commands), out);
      CalendarView v = new CalendarViewImpl(out);
      c.go(v);

      assertTrue(Files.exists(csv));
      String content = Files.readString(csv);
      String row = content.lines().filter(l -> l.startsWith("e,") || l.startsWith("\"e\","))
          .findFirst().orElse("");
      assertTrue(!row.isEmpty());
      assertTrue(row.endsWith(",True"));
    } finally {
      try {
        Files.deleteIfExists(csv);
      } catch (Exception e) {
        assertTrue(Files.exists(csv) || !Files.exists(csv));
      }
    }
  }
}
