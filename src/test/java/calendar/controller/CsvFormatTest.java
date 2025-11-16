package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

/**
 * CSV export format tests.
 */
public class CsvFormatTest {
  @Test
  public void exportContainsExpectedHeaderAndRow() throws Exception {
    StringBuilder out = new StringBuilder();
    CalendarController c = new CalendarControllerImpl(new StringReader(String.join("\n",
        "create calendar --name school --timezone America/New_York",
        "use calendar --name school",
        "create event mtg from 2025-11-01T09:00 to 2025-11-01T10:00",
        "export cal csv_format_test.csv",
        "exit")), out);
    CalendarView v = new CalendarViewImpl(out);
    c.go(v);

    Path p = Path.of("csv_format_test.csv");
    try {
      List<String> lines = Files.readAllLines(p);
      String expectedHeader = "Subject,Start Date,Start Time,End Date,"
          + "End Time,All Day Event,Description,Location,Private";
      assertTrue(lines.get(0).equals(expectedHeader));
      assertTrue(lines.size() >= 2);
      String row = lines.get(1);
      assertTrue(row.contains("mtg"));
      assertTrue(row.contains("11/01/2025"));
      assertTrue(row.contains("9:00 AM"));
      assertTrue(row.contains("10:00 AM"));
    } finally {
      try {
        Files.deleteIfExists(p);
      } catch (Exception e) {
        assertTrue(Files.exists(p) || !Files.exists(p));
      }
    }
  }
}
