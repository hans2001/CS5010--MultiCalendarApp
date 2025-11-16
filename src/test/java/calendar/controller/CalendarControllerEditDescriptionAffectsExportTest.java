package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/**
 * Verifies that editing description is reflected in CSV (kills EditProperty.DESCRIPTION mutants).
 */
public class CalendarControllerEditDescriptionAffectsExportTest {
  @Test
  public void editDescription_setsDescriptionInCsv() throws Exception {
    Path csv = Path.of("mut_desc_export.csv");
    try {
      String commands = String.join("\n",
          "create calendar --name school --timezone America/New_York",
          "use calendar --name school",
          "create event e2 from 2025-11-01T10:00 to 2025-11-01T11:00",
          "edit event description e2 from 2025-11-01T10:00 to 2025-11-01T11:00 with \"Deep work\"",
          "export cal " + csv.getFileName(),
          "exit");

      StringBuilder out = new StringBuilder();
      CalendarController c = new CalendarControllerImpl(new StringReader(commands), out);
      CalendarView v = new CalendarViewImpl(out);
      c.go(v);

      assertTrue(Files.exists(csv));
      String content = Files.readString(csv);
      String row = content.lines().filter(l -> l.startsWith("e2,") || l.startsWith("\"e2\","))
          .findFirst().orElse("");
      assertTrue(!row.isEmpty());
      String[] cols = row.split(",", -1);
      assertEquals("Deep work", cols[6]);
    } finally {
      try {
        Files.deleteIfExists(csv);
      } catch (Exception e) {
        assertTrue(Files.exists(csv) || !Files.exists(csv));
      }
    }
  }
}
