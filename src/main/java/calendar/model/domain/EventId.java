package calendar.model.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for events.
 */
public final class EventId {
  private final UUID value;

  /**
   * Creates an EventId with the given UUID value.
   */
  public EventId(UUID value) {
    this.value = Objects.requireNonNull(value, "value");
  }

  /**
   * Returns the underlying UUID.
   */
  public UUID value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventId)) {
      return false;
    }
    EventId other = (EventId) o;
    return value.equals(other.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return "EventId[" + value + "]";
  }
}
