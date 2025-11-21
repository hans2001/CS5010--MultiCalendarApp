package calendar.view.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lightweight summary used by the GUI to identify an event.
 */
public final class GuiEventSummary {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Constructs a summary of an event.
   */
  public GuiEventSummary(String subject, LocalDateTime start, LocalDateTime end) {
    this.subject = Objects.requireNonNull(subject, "subject");
    this.start = Objects.requireNonNull(start, "start");
    this.end = Objects.requireNonNull(end, "end");
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

  @Override
  public String toString() {
    return subject + " (" + start + " - " + end + ")";
  }
}
