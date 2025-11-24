package calendar.view.model;

import java.util.Objects;

/**
 * Container for values returned when the user creates a new calendar via the GUI.
 */
public final class CalendarCreationData {
  private final String name;
  private final String timezone;

  /**
   * Constructs a new calendar creation payload.
   */
  public CalendarCreationData(String name, String timezone) {
    this.name = Objects.requireNonNull(name, "name");
    this.timezone = Objects.requireNonNull(timezone, "timezone");
  }

  /**
   * Returns the chosen calendar name.
   */
  public String name() {
    return name;
  }

  /**
   * Returns the chosen timezone ID.
   */
  public String timezone() {
    return timezone;
  }
}
