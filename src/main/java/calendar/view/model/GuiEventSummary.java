package calendar.view.model;

import calendar.model.domain.Status;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Lightweight summary used by the GUI to identify an event.
 */
public final class GuiEventSummary {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;
  private final String location;
  private final Status status;

  /**
   * Constructs a summary of an event.
   */
  public GuiEventSummary(String subject,
                         LocalDateTime start,
                         LocalDateTime end,
                         String description,
                         String location,
                         Status status) {
    this.subject = Objects.requireNonNull(subject, "subject");
    this.start = Objects.requireNonNull(start, "start");
    this.end = Objects.requireNonNull(end, "end");
    this.description = description == null ? "" : description;
    this.location = location == null ? "" : location;
    this.status = Objects.requireNonNull(status, "status");
  }

  /** Returns subject/title. */
  public String subject() {
    return subject;
  }

  /** Returns start timestamp. */
  public LocalDateTime start() {
    return start;
  }

  /** Returns end timestamp. */
  public LocalDateTime end() {
    return end;
  }

  /** Returns description if present. */
  public Optional<String> description() {
    return description.isEmpty() ? Optional.empty() : Optional.of(description);
  }

  /** Returns location if present. */
  public Optional<String> location() {
    return location.isEmpty() ? Optional.empty() : Optional.of(location);
  }

  /** Returns visibility status. */
  public Status status() {
    return status;
  }

  @Override
  public String toString() {
    return subject + " (" + start + " - " + end + ")";
  }
}
