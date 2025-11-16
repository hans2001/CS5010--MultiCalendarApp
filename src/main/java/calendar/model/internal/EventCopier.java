package calendar.model.internal;

import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.api.EventDraft;
import calendar.model.domain.Event;
import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Copies events between calendars with timezone conversion.
 *
 * <h2>Weak Coupling</h2>
 *
 * <p>This utility class achieves weak coupling through:
 * <ul>
 *   <li><b>Interface dependencies:</b> Operates on {@link TimeZoneInMemoryCalendarInterface}, not
 *       concrete types</li>
 *   <li><b>Static methods:</b> Stateless utility - no instance state, pure functions</li>
 *   <li><b>Single responsibility:</b> Only handles event copying logic, no calendar management</li>
 * </ul>
 *
 * <p>This allows:
 * <ul>
 *   <li>Testing with mock calendar implementations</li>
 *   <li>Reusing copy logic with different calendar types</li>
 *   <li>Future calendar implementations (e.g., DatabaseCalendar) work without modification</li>
 * </ul>
 */
public final class EventCopier {

  private static Event findEvent(TimeZoneInMemoryCalendarInterface sourceCalendar, String eventName,
                                 LocalDateTime eventStart) {
    Objects.requireNonNull(sourceCalendar, "sourceCalendar cannot be null");
    Objects.requireNonNull(eventName, "eventName cannot be null");
    Objects.requireNonNull(eventStart, "eventStart cannot be null");

    List<Event> candidates = sourceCalendar.allEvents().stream()
        .filter(e -> e.subject().equals(eventName.trim())
            && e.start().equals(eventStart))
        .collect(Collectors.toList());

    if (candidates.isEmpty()) {
      throw new NotFoundException("Event '" + eventName + "' not found at " + eventStart);
    }
    if (candidates.size() > 1) {
      throw new ValidationException(
          "Ambiguous event: multiple events with same name and start time");
    }
    return candidates.get(0);
  }

  private static EventDraft createDraftFromEvent(Event sourceEvent, LocalDateTime targetStart,
                                                  LocalDateTime targetEnd) {
    Objects.requireNonNull(sourceEvent, "sourceEvent cannot be null");
    Objects.requireNonNull(targetStart, "targetStart cannot be null");
    Objects.requireNonNull(targetEnd, "targetEnd cannot be null");

    EventDraft draft = new EventDraft();
    draft.subject = sourceEvent.subject();
    draft.start = Optional.of(targetStart);
    draft.end = Optional.of(targetEnd);
    draft.description = sourceEvent.description();
    draft.location = sourceEvent.location();
    draft.status = Optional.of(sourceEvent.status());
    return draft;
  }

  private static ConvertedTimes convertAndOffsetTimes(
      LocalDateTime sourceStart,
      LocalDateTime sourceEnd,
      ZoneId sourceZone,
      ZoneId targetZone,
      TimeZoneInMemoryCalendarInterface targetCalendar,
      long daysOffset) {
    Objects.requireNonNull(sourceStart, "sourceStart cannot be null");
    Objects.requireNonNull(sourceEnd, "sourceEnd cannot be null");
    Objects.requireNonNull(targetCalendar, "targetCalendar cannot be null");

    LocalDateTime targetStart = targetCalendar.convertToLocalDateTime(
        sourceStart, sourceZone, targetZone);
    LocalDateTime targetEnd = targetCalendar.convertToLocalDateTime(
        sourceEnd, sourceZone, targetZone);

    targetStart = targetStart.plusDays(daysOffset);
    targetEnd = targetEnd.plusDays(daysOffset);

    return new ConvertedTimes(targetStart, targetEnd);
  }

  private static void copyEventToCalendar(TimeZoneInMemoryCalendarInterface targetCalendar,
                                          EventDraft draft) {
    Objects.requireNonNull(targetCalendar, "targetCalendar cannot be null");
    Objects.requireNonNull(draft, "draft cannot be null");

    try {
      targetCalendar.create(draft);
    } catch (ConflictException e) {
      throw new ConflictException(
          "Cannot copy event '" + draft.subject + "': " + e.getMessage(), e);
    }
  }

  /**
   * Copies single event between calendars. Duration preserved.
   *
   * @param sourceCalendar source calendar.
   * @param eventName event subject.
   * @param eventStart event start in source calendar.
   * @param targetCalendar target calendar.
   * @param targetStart target start in target timezone.
   * @throws NotFoundException if event not found.
   * @throws ConflictException if copy conflicts.
   */
  public static void copyEvent(TimeZoneInMemoryCalendarInterface sourceCalendar, String eventName,
                               LocalDateTime eventStart,
                               TimeZoneInMemoryCalendarInterface targetCalendar,
                               LocalDateTime targetStart) {
    Objects.requireNonNull(sourceCalendar, "sourceCalendar cannot be null");
    Objects.requireNonNull(targetCalendar, "targetCalendar cannot be null");

    Event sourceEvent = findEvent(sourceCalendar, eventName, eventStart);

    Duration duration = Duration.between(sourceEvent.start(), sourceEvent.end());
    LocalDateTime targetEnd = targetStart.plus(duration);

    EventDraft draft = createDraftFromEvent(sourceEvent, targetStart, targetEnd);
    copyEventToCalendar(targetCalendar, draft);
  }

  /**
   * Copies all events on a date. Times converted to target timezone.
   *
   * @param sourceCalendar source calendar.
   * @param date source date.
   * @param targetCalendar target calendar.
   * @param targetDate target date.
   * @throws ConflictException if any copy conflicts.
   */
  public static void copyEventsOn(TimeZoneInMemoryCalendarInterface sourceCalendar, LocalDate date,
                                  TimeZoneInMemoryCalendarInterface targetCalendar,
                                  LocalDate targetDate) {
    Objects.requireNonNull(sourceCalendar, "sourceCalendar cannot be null");
    Objects.requireNonNull(targetCalendar, "targetCalendar cannot be null");
    Objects.requireNonNull(date, "date cannot be null");
    Objects.requireNonNull(targetDate, "targetDate cannot be null");

    List<Event> eventsOnDate = sourceCalendar.eventsOn(date);
    ZoneId sourceZone = sourceCalendar.getZoneId();
    ZoneId targetZone = targetCalendar.getZoneId();
    long daysOffset = ChronoUnit.DAYS.between(date, targetDate);

    for (Event sourceEvent : eventsOnDate) {
      ConvertedTimes times = convertAndOffsetTimes(
          sourceEvent.start(), sourceEvent.end(),
          sourceZone, targetZone, targetCalendar, daysOffset);

      EventDraft draft = createDraftFromEvent(sourceEvent, times.start(), times.end());
      copyEventToCalendar(targetCalendar, draft);
    }
  }

  /**
   * Copies events in date range. Only overlapping events copied.
   *
   * @param sourceCalendar source calendar.
   * @param startDate range start (inclusive).
   * @param endDate range end (inclusive).
   * @param targetCalendar target calendar.
   * @param targetStartDate target start date.
   * @throws ValidationException if date range invalid.
   * @throws ConflictException if any copy conflicts.
   */
  public static void copyEventsBetween(TimeZoneInMemoryCalendarInterface sourceCalendar,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       TimeZoneInMemoryCalendarInterface targetCalendar,
                                       LocalDate targetStartDate) {
    Objects.requireNonNull(sourceCalendar, "sourceCalendar cannot be null");
    Objects.requireNonNull(targetCalendar, "targetCalendar cannot be null");
    Objects.requireNonNull(startDate, "startDate cannot be null");
    Objects.requireNonNull(endDate, "endDate cannot be null");
    Objects.requireNonNull(targetStartDate, "targetStartDate cannot be null");

    if (endDate.isBefore(startDate)) {
      throw new ValidationException("End date must be after or equal to start date");
    }

    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.plusDays(1).atStartOfDay();

    List<Event> overlappingEvents = sourceCalendar.eventsOverlapping(rangeStart, rangeEnd);
    ZoneId sourceZone = sourceCalendar.getZoneId();
    ZoneId targetZone = targetCalendar.getZoneId();
    long daysOffset = Duration
        .between(startDate.atStartOfDay(), targetStartDate.atStartOfDay())
        .toDays();

    for (Event sourceEvent : overlappingEvents) {
      ConvertedTimes times = convertAndOffsetTimes(
          sourceEvent.start(), sourceEvent.end(),
          sourceZone, targetZone, targetCalendar, daysOffset);

      EventDraft draft = createDraftFromEvent(sourceEvent, times.start(), times.end());
      copyEventToCalendar(targetCalendar, draft);
    }
  }

  private static final class ConvertedTimes {
    private final LocalDateTime start;
    private final LocalDateTime end;

    private ConvertedTimes(LocalDateTime start, LocalDateTime end) {
      this.start = Objects.requireNonNull(start, "start cannot be null");
      this.end = Objects.requireNonNull(end, "end cannot be null");
    }

    private LocalDateTime start() {
      return start;
    }

    private LocalDateTime end() {
      return end;
    }
  }
}
