package calendar.controller;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Properties that can be edited via controller commands.
 */
enum EditProperty {
  SUBJECT,
  START,
  END,
  DESCRIPTION,
  LOCATION,
  STATUS;

  static Optional<EditProperty> from(String s) {
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


