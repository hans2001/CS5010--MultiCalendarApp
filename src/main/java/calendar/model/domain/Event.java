package calendar.model.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable calendar event.
 *
 * <h2>Design: Immutability</h2>
 *
 * <p><b>Why immutable?</b> Prevents accidental changes. Once an event is created, its
 * fields can't be modified. To "update" an event, InMemoryCalendar creates a new Event
 * with the changed values. This ensures all updates go through validation and index updates.</p>
 *
 * <p>Immutability also simplifies reasoning--if you have an Event reference, you know it
 * won't change under you. You can safely pass it around, put it in collections, return it
 * from methods without worrying about who else has a reference.</p>
 *
 * <p>The Builder pattern lets you construct events flexibly while keeping them immutable.</p>
 */
public final class Event {
  private final EventId id;
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;
  private final String location;
  private final Status status;

  private Event(Builder b) {
    this.id = b.id != null ? b.id : new EventId(UUID.randomUUID());
    this.subject = Objects.requireNonNull(b.subject, "subject").trim();
    this.start = Objects.requireNonNull(b.start, "start");
    this.end = Objects.requireNonNull(b.end, "end");
    this.description = b.description == null ? "" : b.description;
    this.location = b.location == null ? "" : b.location;
    this.status = (b.status == null) ? Status.PUBLIC : b.status;

    if (subject.isEmpty()) {
      throw new IllegalArgumentException("subject cannot be blank");
    }
    if (!end.isAfter(start)) {
      throw new IllegalArgumentException("end must be strictly after start");
    }
  }

  /**
   * Unique identifier.
   */
  public EventId id() {
    return id;
  }

  /**
   * Subject/title.
   */
  public String subject() {
    return subject;
  }

  /**
   * Start instant (inclusive).
   */
  public LocalDateTime start() {
    return start;
  }

  /**
   * End instant (exclusive).
   */
  public LocalDateTime end() {
    return end;
  }

  /**
   * Optional description.
   */
  public Optional<String> description() {
    return description.isEmpty() ? Optional.empty() : Optional.of(description);
  }

  /**
   * Optional location.
   */
  public Optional<String> location() {
    return location.isEmpty() ? Optional.empty() : Optional.of(location);
  }

  /**
   * Visibility status.
   */
  public Status status() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    return (this == o) || (o instanceof Event && id.equals(((Event) o).id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Builder for {@link Event}.
   */
  public static final class Builder {
    private EventId id;
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String description;
    private String location;
    private Status status;

    /**
     * Optional explicit id.
     */
    public Builder id(EventId id) {
      this.id = id;
      return this;
    }

    /**
     * Required subject.
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Required start.
     */
    public Builder start(LocalDateTime start) {
      this.start = start;
      return this;
    }

    /**
     * Required end.
     */
    public Builder end(LocalDateTime end) {
      this.end = end;
      return this;
    }

    /**
     * Optional description.
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Optional location.
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Optional status. The service decides defaults--domain does not assume a default.
     */
    public Builder status(Status status) {
      this.status = status;
      return this;
    }

    /**
     * Builds an {@link Event} and enforces invariants.
     */
    public Event build() {
      return new Event(this);
    }
  }
}
