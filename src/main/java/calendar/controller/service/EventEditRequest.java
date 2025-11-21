package calendar.controller.service;

import calendar.controller.EditProperty;
import calendar.model.api.EditScope;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Normalized representation of an edit command, capturing the target selector,
 * requested change, and scope (single instance, following, or entire series).
 */
public final class EventEditRequest {
  private final EditProperty property;
  private final String subject;
  private final LocalDateTime start;
  private final Optional<LocalDateTime> end;
  private final String newValue;
  private final EditScope scope;

  private EventEditRequest(Builder builder) {
    this.property = Objects.requireNonNull(builder.property, "property");
    this.subject = Objects.requireNonNull(builder.subject, "subject");
    this.start = Objects.requireNonNull(builder.start, "start");
    this.end = Optional.ofNullable(builder.end);
    this.newValue = Objects.requireNonNull(builder.newValue, "newValue");
    this.scope = Objects.requireNonNull(builder.scope, "scope");
  }

  /**
   * Property being edited.
   */
  public EditProperty property() {
    return property;
  }

  /**
   * Subject used to locate target events.
   */
  public String subject() {
    return subject;
  }

  /**
   * Selector start time.
   */
  public LocalDateTime start() {
    return start;
  }

  /**
   * Optional selector end time (for single-instance edits).
   */
  public Optional<LocalDateTime> end() {
    return end;
  }

  /**
   * New value to apply for the property.
   */
  public String newValue() {
    return newValue;
  }

  /**
   * Scope of the edit (single event, following, or entire series).
   */
  public EditScope scope() {
    return scope;
  }

  /** Builder to compose immutable {@link EventEditRequest} objects. */
  public static final class Builder {
    private EditProperty property;
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String newValue;
    private EditScope scope;

    /**
     * Sets which property should be edited.
     */
    public Builder property(EditProperty property) {
      this.property = property;
      return this;
    }

    /**
     * Sets the subject selector.
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the starting selector time.
     */
    public Builder start(LocalDateTime start) {
      this.start = start;
      return this;
    }

    /**
     * Sets the optional selector end time.
     */
    public Builder end(LocalDateTime end) {
      this.end = end;
      return this;
    }

    /**
     * Sets the new property value as a string.
     */
    public Builder newValue(String newValue) {
      this.newValue = newValue;
      return this;
    }

    /**
     * Sets the edit scope.
     */
    public Builder scope(EditScope scope) {
      this.scope = scope;
      return this;
    }

    /**
     * Builds the request.
     */
    public EventEditRequest build() {
      return new EventEditRequest(this);
    }
  }
}
