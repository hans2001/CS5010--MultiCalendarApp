package calendar.model.exception;

/**
 * Thrown when an operation would violate the uniqueness constraint
 * on (subject, start, end).
 */
public final class ConflictException extends RuntimeException {
  /**
   Creates a ConflictException with the given message.
   */
  public ConflictException(String message) {
    super(message);
  }

  /**
   * Creates a ConflictException with the given message and root cause.
   *
   * @param message error description
   * @param cause underlying exception
   */
  public ConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
