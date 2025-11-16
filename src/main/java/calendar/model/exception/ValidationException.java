package calendar.model.exception;

/**
 * Thrown when input data is malformed or logically invalid.
 */
public final class ValidationException extends RuntimeException {
  /**
   Creates a ValidationException with the given message.
   */
  public ValidationException(String message) {
    super(message);
  }
}
