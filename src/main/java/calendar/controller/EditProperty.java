package calendar.controller;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Properties that can be edited via controller commands.
 */
public enum EditProperty {
  SUBJECT,
  START,
  END,
  DESCRIPTION,
  LOCATION,
  STATUS;

  /**
   * Parses a property identifier (e.g., "subject") into an {@link EditProperty}.
   *
   * @param s the raw property string from user input.
   * @return the matching property if recognized.
   */
  public static Optional<EditProperty> from(String s) {
    Objects.requireNonNull(s, "property cannot be null");
    switch (s.trim().toLowerCase(Locale.ROOT)) {
      case "subject":
        return Optional.of(SUBJECT);
      case "start":
        return Optional.of(START);
      case "end":
        return Optional.of(END);
      case "description":
        return Optional.of(DESCRIPTION);
      case "location":
        return Optional.of(LOCATION);
      case "status":
        return Optional.of(STATUS);
      default:
        return Optional.empty();
    }
  }
}
