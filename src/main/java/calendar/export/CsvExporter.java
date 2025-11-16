package calendar.export;

import calendar.model.config.CalendarSettings;
import calendar.model.domain.Event;
import calendar.model.domain.Status;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Exports events in CSV format compatible with spreadsheet tools.
 */
public final class CsvExporter implements CalendarExporter {
  private final CalendarSettings settings;

  /**
   * Creates an exporter that formats CSV rows per the provided {@link CalendarSettings}.
   *
   * @param settings calendar policy used to detect all-day events
   */
  public CsvExporter(CalendarSettings settings) {
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  @Override
  public Path export(Path targetFile, List<Event> orderedEvents) {
    Objects.requireNonNull(targetFile, "targetFile");
    Objects.requireNonNull(orderedEvents, "orderedEvents");
    DateTimeFormatter dfmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter tfmt = DateTimeFormatter.ofPattern("h:mm a");

    try (BufferedWriter w = Files.newBufferedWriter(targetFile.toAbsolutePath())) {
      w.write("Subject,Start Date,Start Time,End Date,"
          + "End Time,All Day Event,Description,Location,Private");
      w.newLine();
      for (Event e : orderedEvents) {
        boolean sameDay = e.start().toLocalDate().equals(e.end().toLocalDate());
        boolean isAllDay = sameDay && e.start().toLocalTime().equals(settings.allDayStart())
            && e.end().toLocalTime().equals(settings.allDayEnd());

        String startDate = e.start().toLocalDate().format(dfmt);
        String endDate = e.end().toLocalDate().format(dfmt);
        String startTime = isAllDay ? "" : e.start().toLocalTime().format(tfmt);
        String endTime = isAllDay ? "" : e.end().toLocalTime().format(tfmt);
        String allDay = isAllDay ? "True" : "False";
        String priv = e.status() == Status.PRIVATE ? "True" : "False";

        w.write(String.join(",",
            csv(e.subject()),
            csv(startDate),
            csv(startTime),
            csv(endDate),
            csv(endTime),
            csv(allDay),
            csv(e.description().orElse("")),
            csv(e.location().orElse("")),
            csv(priv)));
        w.newLine();
      }
      return targetFile.toAbsolutePath();
    } catch (IOException ioe) {
      throw new RuntimeException("Failed to export CSV: " + ioe.getMessage(), ioe);
    }
  }

  private static String csv(String s) {
    String v = s == null ? "" : s;
    if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
      return "\"" + v.replace("\"", "\"\"") + "\"";
    }
    return v;
  }
}
