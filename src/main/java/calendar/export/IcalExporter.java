package calendar.export;

import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.model.domain.Status;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Writes events to an iCalendar (.ics/.ical) file.
 */
public final class IcalExporter implements CalendarExporter {
  private static final DateTimeFormatter DATE_TIME_FMT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  private final CalendarSettings settings;

  /**
   * Creates an exporter that formats events using the provided {@link CalendarSettings}.
   *
   * @param settings calendar policy used to detect all-day events
   */
  public IcalExporter(CalendarSettings settings) {
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  @Override
  public Path export(Path targetFile, List<Event> orderedEvents) {
    Objects.requireNonNull(targetFile, "targetFile");
    Objects.requireNonNull(orderedEvents, "orderedEvents");
    try (BufferedWriter writer = Files.newBufferedWriter(targetFile.toAbsolutePath())) {
      writer.write("BEGIN:VCALENDAR");
      writer.newLine();
      writer.write("VERSION:2.0");
      writer.newLine();
      writer.write("PRODID:-//CS3500//TimeZoneCalendar//EN");
      writer.newLine();
      writer.write("CALSCALE:GREGORIAN");
      writer.newLine();

      for (Event event : orderedEvents) {
        writeEvent(writer, event);
      }

      writer.write("END:VCALENDAR");
      writer.newLine();
      return targetFile.toAbsolutePath();
    } catch (IOException ioe) {
      throw new RuntimeException("Failed to export iCal: " + ioe.getMessage(), ioe);
    }
  }

  private void writeEvent(BufferedWriter writer, Event event) throws IOException {
    writer.write("BEGIN:VEVENT");
    writer.newLine();
    writer.write("UID:" + sanitize(event.id().value().toString()));
    writer.newLine();
    writer.write("DTSTAMP:" + ZonedDateTime.now(ZoneOffset.UTC).format(DATE_TIME_FMT) + "Z");
    writer.newLine();

    boolean isAllDay = isAllDay(event);
    if (isAllDay) {
      writer.write("DTSTART;VALUE=DATE:" + event.start().toLocalDate().format(DATE_FMT));
      writer.newLine();
      LocalDate endExclusive = event.end().toLocalDate().plusDays(1);
      writer.write("DTEND;VALUE=DATE:" + endExclusive.format(DATE_FMT));
      writer.newLine();
    } else {
      writer.write("DTSTART:" + formatDateTime(event.start()));
      writer.newLine();
      writer.write("DTEND:" + formatDateTime(event.end()));
      writer.newLine();
    }

    writer.write("SUMMARY:" + sanitize(event.subject()));
    writer.newLine();

    if (event.description().isPresent()) {
      writer.write("DESCRIPTION:" + sanitize(event.description().get()));
      writer.newLine();
    }

    if (event.location().isPresent()) {
      writer.write("LOCATION:" + sanitize(event.location().get()));
      writer.newLine();
    }

    writer.write("CLASS:" + (event.status() == Status.PRIVATE ? "PRIVATE" : "PUBLIC"));
    writer.newLine();
    writer.write("END:VEVENT");
    writer.newLine();
  }

  private boolean isAllDay(Event event) {
    return event.start().toLocalDate().equals(event.end().toLocalDate())
        && event.start().toLocalTime().equals(settings.allDayStart())
        && event.end().toLocalTime().equals(settings.allDayEnd());
  }

  private static String formatDateTime(LocalDateTime time) {
    return time.atZone(ZoneOffset.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
        .format(DATE_TIME_FMT) + "Z";
  }

  private static String sanitize(String value) {
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
