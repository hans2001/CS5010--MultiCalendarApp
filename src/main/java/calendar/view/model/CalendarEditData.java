package calendar.view.model;

import java.util.Objects;

/**
 * Container for values returned when the user edits an existing calendar via the GUI.
 */
public final class CalendarEditData {
  private final String originalName;
  private final String newName;
  private final String newTimezone;

  /**
   * Constructs edit data containing both original and updated values.
   */
  public CalendarEditData(String originalName, String newName, String newTimezone) {
    this.originalName = Objects.requireNonNull(originalName, "originalName");
    this.newName = Objects.requireNonNull(newName, "newName");
    this.newTimezone = Objects.requireNonNull(newTimezone, "newTimezone");
  }

  /**
   * Returns the calendar name prior to editing.
   */
  public String originalName() {
    return originalName;
  }

  /**
   * Returns the name the user supplied.
   */
  public String newName() {
    return newName;
  }

  /**
   * Returns the timezone the user selected.
   */
  public String newTimezone() {
    return newTimezone;
  }
}
