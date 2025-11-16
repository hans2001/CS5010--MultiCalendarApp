package calendar.export;

import static org.junit.Assert.assertTrue;

import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.model.domain.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;

/**
 * Tests for CsvExporter format and escaping.
 */
public final class CsvExporterTest {

  private static Event ev(String subject, LocalDateTime s, LocalDateTime e, Status status) {
    return new Event.Builder()
        .subject(subject)
        .start(s)
        .end(e)
        .status(status)
        .build();
  }

  /**
   * Verifies CSV export format.
   *
   * <p>Header columns are correct.
   * All-day detection produces empty time fields and sets All Day Event to True.
   * Private events set the Private column to True.
   * Values are properly escaped for commas and quotes.
   */
  @Test
  public void exportsHeader_allDayAndPrivateFlags_andEscaping() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    CsvExporter exporter = new CsvExporter(settings);

    LocalDate d = LocalDate.of(2025, 5, 5);
    Event allDay = ev("All, Day", d.atTime(settings.allDayStart()), d.atTime(settings.allDayEnd()),
        Status.PUBLIC);

    Event timed = ev("Quote\"Test", LocalDateTime.of(2025, 5, 6, 9, 0),
        LocalDateTime.of(2025, 5, 6, 10, 0), Status.PRIVATE);

    Event startAtAllDayOnly = ev("Edge", d.atTime(settings.allDayStart()),
        d.atTime(settings.allDayEnd().plusHours(1)),
        Status.PUBLIC);

    Path tmp = Files.createTempFile("calendar", ".csv");
    try {
      Event cross = ev("Cross", LocalDateTime.of(2025, 5, 6, 23, 30),
          LocalDateTime.of(2025, 5, 7, 0, 10), Status.PUBLIC);
      Path out = exporter.export(tmp, List.of(allDay, timed, startAtAllDayOnly, cross));
      List<String> lines = Files.readAllLines(out);
      assertTrue(
          lines.get(0).startsWith("Subject,Start Date,Start Time,End Date,End Time,All Day Event"));
      String r1 = lines.get(1);
      assertTrue(r1.contains("\"All, Day\""));
      assertTrue(r1.contains(",,"));
      assertTrue(r1.contains(",True,"));
      assertTrue(r1.endsWith(",False"));

      String r2 = lines.get(2);
      assertTrue(r2.contains("Quote\"\"Test"));
      assertTrue(r2.contains(",9:00 AM,"));
      assertTrue(r2.endsWith(",True"));
      assertTrue(r2.contains(",False,"));

      String r3 = lines.get(3);
      assertTrue(r3.contains(",8:00 AM,"));
      assertTrue(r3.contains(",6:00 PM,"));

      String r4 = lines.get(4);
      assertTrue(r4.contains("05/06/2025"));
      assertTrue(r4.contains("11:30 PM"));
      assertTrue(r4.contains("05/07/2025"));
      assertTrue(r4.contains("12:10 AM"));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  /**
   * Tests IOException path by attempting to write to a directory.
   */
  @Test(expected = RuntimeException.class)
  public void export_throws_on_io_error() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    CsvExporter exporter = new CsvExporter(settings);

    Path dir = Files.createTempDirectory("calendar-dir");
    try {
      exporter.export(dir, List.of());
    } finally {
      Files.deleteIfExists(dir);
    }
  }

  /**
   * Tests csv() newline quoting and null handling via reflective access.
   */
  @Test
  public void csv_private_newline_and_null() throws Exception {
    CalendarSettings settings = CalendarSettings.defaults();
    CsvExporter exporter = new CsvExporter(settings);

    java.lang.reflect.Method m = CsvExporter.class.getDeclaredMethod("csv", String.class);
    m.setAccessible(true);

    String quoted = (String) m.invoke(exporter, "line1\nline2");
    org.junit.Assert.assertTrue(quoted.startsWith("\"") && quoted.endsWith("\""));

    String empty = (String) m.invoke(exporter, new Object[] {null});
    org.junit.Assert.assertEquals("", empty);
  }
}
