package calendar.export;

import calendar.model.domain.Event;
import java.nio.file.Path;
import java.util.List;

/**
 * Strategy interface for exporting events to an external file format.
 */
public interface CalendarExporter {

  /**
   * Writes the provided events to {@code targetFile} in the exporter's format.
   *
   * @param targetFile destination file (may be relative)
   * @param orderedEvents events sorted in the desired order
   * @return absolute path of the written file
   */
  Path export(Path targetFile, List<Event> orderedEvents);
}
