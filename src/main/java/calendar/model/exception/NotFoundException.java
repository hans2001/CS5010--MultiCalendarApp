package calendar.model.exception;

/**
 * Thrown when an entity could not be found by its identifier.
 */
public final class NotFoundException extends RuntimeException {
  /**
   Creates a NotFoundException with the given message.
   */
  public NotFoundException(String message) {
    super(message);
  }
}
