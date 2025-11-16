package calendar.export;

import static org.junit.Assert.assertTrue;

import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

/**
 * Tests for iCal export format.
 */
public final class IcalExporterTest {

  private static Event event(String subject, LocalDateTime start,
                             LocalDateTime end, Status status) {
    return new Event.Builder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Line1\nLine2")
        .location("Room 101")
        .status(status)
        .build();
  }

  @Test
  public void writesCalendarEnvelope_andEventFields() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    IcalExporter exporter = new IcalExporter(settings);
    Event event = event("Subject", LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0), Status.PUBLIC);

    Path tmp = Files.createTempFile("calendar", ".ics");
    try {
      exporter.export(tmp, List.of(event));
      List<String> lines = Files.readAllLines(tmp);
      assertTrue(lines.get(0).equals("BEGIN:VCALENDAR"));
      assertTrue(lines.stream().anyMatch("VERSION:2.0"::equals));
      assertTrue(lines.stream().anyMatch("PRODID:-//CS3500//TimeZoneCalendar//EN"::equals));
      assertTrue(lines.stream().anyMatch("CALSCALE:GREGORIAN"::equals));
      assertTrue(lines.stream().anyMatch(l -> l.startsWith("BEGIN:VEVENT")));
      assertTrue(lines.stream().anyMatch(l -> l.startsWith("SUMMARY:Subject")));
      assertTrue(lines.stream().anyMatch(l -> l.contains("DESCRIPTION:Line1\\nLine2")));
      assertTrue(lines.stream().anyMatch(l -> l.startsWith("LOCATION:Room 101")));
      assertTrue(lines.stream().anyMatch(l -> l.startsWith("CLASS:PUBLIC")));
      assertTrue(lines.stream().anyMatch("END:VEVENT"::equals));
      assertTrue(lines.get(lines.size() - 1).equals("END:VCALENDAR"));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  @Test(expected = RuntimeException.class)
  public void exportFails_forDirectoryTarget() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    IcalExporter exporter = new IcalExporter(settings);
    Path dir = Files.createTempDirectory("ics-export");
    try {
      exporter.export(dir, List.of());
    } finally {
      Files.deleteIfExists(dir);
    }
  }

  @Test
  public void allDayEventUsesDateValueAndExclusiveEnd() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    IcalExporter exporter = new IcalExporter(settings);
    Event allDay = new Event.Builder()
        .subject("AllDay")
        .start(LocalDateTime.of(2025, 6, 1, 0, 0).withHour(settings.allDayStart().getHour())
            .withMinute(settings.allDayStart().getMinute()))
        .end(LocalDateTime.of(2025, 6, 1, 0, 0).withHour(settings.allDayEnd().getHour())
            .withMinute(settings.allDayEnd().getMinute()))
        .status(Status.PUBLIC)
        .build();

    Path tmp = Files.createTempFile("calendar-all-day", ".ics");
    try {
      exporter.export(tmp, List.of(allDay));
      List<String> lines = Files.readAllLines(tmp);
      assertTrue(lines.stream().anyMatch(
          line -> line.equals("DTSTART;VALUE=DATE:20250601")));
      assertTrue(lines.stream().anyMatch(
          line -> line.equals("DTEND;VALUE=DATE:20250602")));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  @Test
  public void sanitizesSpecialCharactersInSummaryAndLocation() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    IcalExporter exporter = new IcalExporter(settings);
    Event event = new Event.Builder()
        .subject("Review\\,;")
        .start(LocalDateTime.of(2025, 7, 1, 10, 0))
        .end(LocalDateTime.of(2025, 7, 1, 11, 0))
        .location("Room; 1, \\Notes")
        .status(Status.PRIVATE)
        .build();

    Path tmp = Files.createTempFile("calendar-sanitize", ".ics");
    try {
      exporter.export(tmp, List.of(event));
      List<String> lines = Files.readAllLines(tmp);
      String expectedSummary = "SUMMARY:" + sanitizeForTest("Review\\,;");
      String expectedLocation = "LOCATION:" + sanitizeForTest("Room; 1, \\Notes");
      assertTrue(lines.stream().anyMatch(line -> line.equals(expectedSummary)));
      assertTrue(lines.stream().anyMatch(line -> line.equals(expectedLocation)));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  @Test
  public void timedEventsEmitUtcDateTimes() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    IcalExporter exporter = new IcalExporter(settings);
    Event event = event("Timed", LocalDateTime.of(2025, 4, 2, 14, 30),
        LocalDateTime.of(2025, 4, 2, 15, 30), Status.PUBLIC);

    Path tmp = Files.createTempFile("calendar-datetime", ".ics");
    try {
      exporter.export(tmp, List.of(event));
      List<String> lines = Files.readAllLines(tmp);
      String startLine = lines.stream()
          .filter(l -> l.startsWith("DTSTART:"))
          .findFirst()
          .orElse("");
      String endLine = lines.stream()
          .filter(l -> l.startsWith("DTEND:"))
          .findFirst()
          .orElse("");
      assertTrue("Expected DTSTART line with timestamp", startLine.length() > "DTSTART:".length());
      assertTrue("Expected DTEND line with timestamp", endLine.length() > "DTEND:".length());
      assertTrue(lines.stream().noneMatch(l -> l.startsWith("DTSTART;VALUE=DATE")));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  @Test
  public void eventHeadersEmitOneLinePerField() throws IOException {
    CalendarSettings settings = CalendarSettings.defaults();
    IcalExporter exporter = new IcalExporter(settings);
    UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000042");
    Event event = new Event.Builder()
        .id(new EventId(uuid))
        .subject("HeaderCheck")
        .start(LocalDateTime.of(2025, 8, 1, 9, 0))
        .end(LocalDateTime.of(2025, 8, 1, 10, 0))
        .status(Status.PUBLIC)
        .build();

    Path tmp = Files.createTempFile("calendar-header", ".ics");
    try {
      exporter.export(tmp, List.of(event));
      List<String> lines = Files.readAllLines(tmp);
      assertTrue("BEGIN:VEVENT should be on its own line", lines.contains("BEGIN:VEVENT"));
      assertTrue("UID should be on its own line", lines.contains("UID:" + uuid));
      assertTrue("DTSTAMP line missing", lines.stream()
          .anyMatch(line -> line.matches("DTSTAMP:\\d{8}T\\d{6}Z")));
      assertTrue("DTSTART line missing", lines.stream()
          .anyMatch(line -> line.startsWith("DTSTART:")));
      assertTrue("DTEND line missing", lines.stream()
          .anyMatch(line -> line.startsWith("DTEND:")));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  private static String sanitizeForTest(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n");
  }
}
