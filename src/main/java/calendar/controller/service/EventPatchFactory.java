package calendar.controller.service;

import calendar.controller.EditProperty;
import calendar.model.api.EventPatch;
import calendar.model.domain.Status;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Converts edit properties and new values into {@link EventPatch} instances.
 * Encapsulates parsing so multiple controllers can reuse the conversion.
 */
public class EventPatchFactory {
  /**
   * Creates an {@link EventPatch} for the provided property/new value pair.
   *
   * @param property property to edit.
   * @param newValue string representation supplied by the user.
   * @return populated patch.
   */
  public EventPatch create(EditProperty property, String newValue) {
    EventPatch patch = new EventPatch();
    switch (property) {
      case SUBJECT:
        patch.subject = Optional.of(newValue);
        break;
      case START:
        patch.start = Optional.of(LocalDateTime.parse(newValue));
        break;
      case END:
        patch.end = Optional.of(LocalDateTime.parse(newValue));
        break;
      case DESCRIPTION:
        patch.description = Optional.of(newValue);
        break;
      case LOCATION:
        patch.location = Optional.of(newValue);
        break;
      case STATUS:
        patch.status = Optional.of(Status.valueOf(newValue.toUpperCase()));
        break;
      default:
        break;
    }
    return patch;
  }
}
