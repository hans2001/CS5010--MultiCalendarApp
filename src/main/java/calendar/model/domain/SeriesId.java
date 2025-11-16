package calendar.model.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier for an event series (recurrence group).
 */
public final class SeriesId {
  private final UUID value;

  /**
   * Creates a series id from a UUID.
   *
   * @param value UUID
   */
  public SeriesId(UUID value) {
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
    return (this == o) || (o instanceof SeriesId && value.equals(((SeriesId) o).value));
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
