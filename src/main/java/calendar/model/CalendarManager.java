package calendar.model;

import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import calendar.model.internal.EventCopier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Facade for managing multiple calendars and coordinating cross-calendar operations.
 *
 * <h2>Facade Pattern</h2>
 *
 * <p>This class acts as a Facade that simplifies access to a complex subsystem:
 * <ul>
 *   <li><b>Calendar Storage:</b> Manages collection of calendars (HashMap-based storage)</li>
 *   <li><b>Event Copying:</b> Delegates to EventCopier for cross-calendar event operations</li>
 *   <li><b>Calendar Lifecycle:</b> Creates, retrieves, edits calendars through a unified
 *       interface</li>
 * </ul>
 *
 * <p>Instead of clients directly interacting with multiple classes
 * (TimeZoneInMemoryCalendar, EventCopier, storage mechanisms), they use this single facade which
 * coordinates all operations.
 *
 * <h2>Weak Coupling</h2>
 *
 * <p>This class achieves weak coupling through:
 * <ul>
 *   <li><b>Interface-based storage:</b> Stores {@link TimeZoneInMemoryCalendarInterface}, not
 *       concrete types</li>
 *   <li><b>Factory pattern:</b> Uses {@link CalendarFactory} to create calendars, allowing
 *       different implementations</li>
 *   <li><b>Static utility delegation:</b> Delegates to {@link EventCopier} static methods
 *       (stateless utility)</li>
 * </ul>
 *
 * <p>This allows:
 * <ul>
 *   <li>Swapping calendar implementations without changing CalendarManager</li>
 *   <li>Testing with mock calendars via factory</li>
 *   <li>Future extensions (e.g., DatabaseCalendar) without modifying this class</li>
 * </ul>
 */
public class CalendarManager {
  private final Map<String, TimeZoneInMemoryCalendarInterface> calendars = new HashMap<>();
  private final CalendarFactory factory;

  /**
   * Creates CalendarManager with default factory.
   */
  public CalendarManager() {
    this(new DefaultCalendarFactory());
  }

  /**
   * Creates CalendarManager with custom factory (enables dependency injection for testing).
   *
   * @param factory factory for creating calendar instances.
   */
  public CalendarManager(CalendarFactory factory) {
    this.factory = Objects.requireNonNull(factory, "factory cannot be null");
  }

  /**
   * Creates calendar with unique name and timezone.
   *
   * @param name unique calendar name.
   * @param timezone IANA timezone (e.g., "America/New_York").
   * @return created calendar.
   * @throws IllegalArgumentException if timezone invalid.
   * @throws ConflictException if name exists.
   * @throws ValidationException if name blank.
   */
  public TimeZoneInMemoryCalendarInterface createCalendar(String name, String timezone) {
    Objects.requireNonNull(name, "name cannot be null");
    Objects.requireNonNull(timezone, "timezone cannot be null");
    
    String trimmedName = name.trim();
    if (trimmedName.isEmpty()) {
      throw new ValidationException("Calendar name cannot be blank");
    }

    if (calendars.containsKey(trimmedName)) {
      throw new ConflictException("Calendar with name '" + trimmedName + "' already exists");
    }

    TimeZoneInMemoryCalendarInterface calendar = factory.create(timezone, trimmedName);
    calendars.put(trimmedName, calendar);
    return calendar;
  }

  /**
   * Gets calendar by name.
   *
   * @param name calendar name.
   * @return calendar.
   * @throws NotFoundException if not found.
   */
  public TimeZoneInMemoryCalendarInterface getCalendar(String name) {
    Objects.requireNonNull(name, "name cannot be null");
    TimeZoneInMemoryCalendarInterface calendar = calendars.get(name.trim());
    if (calendar == null) {
      throw new NotFoundException("Calendar '" + name + "' not found");
    }
    return calendar;
  }

  /**
   * Renames a calendar while enforcing uniqueness.
   *
   * @param oldName current calendar name
   * @param newName desired new name
   */
  public void editCalendarName(String oldName, String newName) {
    Objects.requireNonNull(oldName, "oldName cannot be null");
    Objects.requireNonNull(newName, "newName cannot be null");
    
    String trimmedOldName = oldName.trim();
    String trimmedNewName = newName.trim();
    
    if (trimmedNewName.isEmpty()) {
      throw new ValidationException("Calendar name cannot be blank");
    }

    TimeZoneInMemoryCalendarInterface calendar = getCalendar(trimmedOldName);

    if (!trimmedOldName.equals(trimmedNewName) && calendars.containsKey(trimmedNewName)) {
      throw new ConflictException("Calendar with name '" + trimmedNewName + "' already exists");
    }

    calendar.setName(trimmedNewName);
    calendars.remove(trimmedOldName);
    calendars.put(trimmedNewName, calendar);
  }

  /** Updates a calendar's timezone using a {@link ZoneId}. */
  public void editCalendarTimezone(String name, ZoneId zoneId) {
    Objects.requireNonNull(name, "name cannot be null");
    Objects.requireNonNull(zoneId, "zoneId cannot be null");
    getCalendar(name.trim()).setZoneId(zoneId);
  }

  /** Returns whether a calendar exists for the provided name. */
  public boolean hasCalendar(String name) {
    Objects.requireNonNull(name, "name cannot be null");
    return calendars.containsKey(name.trim());
  }

  /** Returns an immutable snapshot of all calendar names. */
  public Set<String> getCalendarNames() {
    return Collections.unmodifiableSet(calendars.keySet());
  }

  /** Returns an immutable snapshot of calendar instances keyed by name. */
  public Map<String, TimeZoneInMemoryCalendarInterface> getAllCalendars() {
    return Collections.unmodifiableMap(calendars);
  }

  /** Returns number of calendars managed by this instance. */
  public int size() {
    return calendars.size();
  }

  /** Returns {@code true} if no calendars have been registered. */
  public boolean isEmpty() {
    return calendars.isEmpty();
  }

  /**
   * Copies event between calendars. Duration preserved.
   * 
   * <p>This method acts as part of the Facade - it hides the complexity of:
   * <ul>
   *   <li>Retrieving calendars by name</li>
   *   <li>Delegating to EventCopier for timezone conversion and event copying</li>
   *   <li>Error handling and validation</li>
   * </ul>
   *
   * @param sourceCalendarName source calendar name.
   * @param eventName event subject.
   * @param eventStart event start in source calendar.
   * @param targetCalendarName target calendar name.
   * @param targetStart target start in target timezone.
   * @throws NotFoundException if calendar/event not found.
   * @throws ConflictException if copy conflicts.
   */
  public void copyEvent(String sourceCalendarName, String eventName, LocalDateTime eventStart,
                       String targetCalendarName, LocalDateTime targetStart) {
    Objects.requireNonNull(sourceCalendarName, "sourceCalendarName cannot be null");
    Objects.requireNonNull(eventName, "eventName cannot be null");
    Objects.requireNonNull(eventStart, "eventStart cannot be null");
    Objects.requireNonNull(targetCalendarName, "targetCalendarName cannot be null");
    Objects.requireNonNull(targetStart, "targetStart cannot be null");

    TimeZoneInMemoryCalendarInterface sourceCalendar = getCalendar(sourceCalendarName);
    TimeZoneInMemoryCalendarInterface targetCalendar = getCalendar(targetCalendarName);

    // Weak coupling: Delegates to static utility (no state, pure function)
    EventCopier.copyEvent(sourceCalendar, eventName, eventStart, targetCalendar, targetStart);
  }

  /**
   * Copies events on a date. Times converted to target timezone.
   *
   * @param sourceCalendarName source calendar name.
   * @param date source date.
   * @param targetCalendarName target calendar name.
   * @param targetDate target date.
   * @throws NotFoundException if calendar not found.
   * @throws ConflictException if any copy conflicts.
   */
  public void copyEventsOn(String sourceCalendarName, LocalDate date,
                          String targetCalendarName, LocalDate targetDate) {
    Objects.requireNonNull(sourceCalendarName, "sourceCalendarName cannot be null");
    Objects.requireNonNull(date, "date cannot be null");
    Objects.requireNonNull(targetCalendarName, "targetCalendarName cannot be null");
    Objects.requireNonNull(targetDate, "targetDate cannot be null");

    TimeZoneInMemoryCalendarInterface sourceCalendar = getCalendar(sourceCalendarName);
    TimeZoneInMemoryCalendarInterface targetCalendar = getCalendar(targetCalendarName);

    EventCopier.copyEventsOn(sourceCalendar, date, targetCalendar, targetDate);
  }

  /**
   * Copies events in date range. Only overlapping events copied.
   *
   * @param sourceCalendarName source calendar name.
   * @param startDate range start (inclusive).
   * @param endDate range end (inclusive).
   * @param targetCalendarName target calendar name.
   * @param targetStartDate target start date.
   * @throws NotFoundException if calendar not found.
   * @throws ConflictException if any copy conflicts.
   * @throws ValidationException if date range invalid.
   */
  public void copyEventsBetween(String sourceCalendarName, LocalDate startDate, LocalDate endDate,
                                String targetCalendarName, LocalDate targetStartDate) {
    Objects.requireNonNull(sourceCalendarName, "sourceCalendarName cannot be null");
    Objects.requireNonNull(startDate, "startDate cannot be null");
    Objects.requireNonNull(endDate, "endDate cannot be null");
    Objects.requireNonNull(targetCalendarName, "targetCalendarName cannot be null");
    Objects.requireNonNull(targetStartDate, "targetStartDate cannot be null");

    TimeZoneInMemoryCalendarInterface sourceCalendar = getCalendar(sourceCalendarName);
    TimeZoneInMemoryCalendarInterface targetCalendar = getCalendar(targetCalendarName);

    EventCopier.copyEventsBetween(
        sourceCalendar, startDate, endDate, targetCalendar, targetStartDate);
  }
}
