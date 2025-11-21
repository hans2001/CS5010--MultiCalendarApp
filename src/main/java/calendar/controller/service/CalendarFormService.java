package calendar.controller.service;

import static calendar.controller.CommandPatterns.CREATE_ALLDAY;
import static calendar.controller.CommandPatterns.CREATE_ALLDAY_REPEAT_N;
import static calendar.controller.CommandPatterns.CREATE_ALLDAY_REPEAT_UNTIL;
import static calendar.controller.CommandPatterns.CREATE_REPEAT_N;
import static calendar.controller.CommandPatterns.CREATE_REPEAT_UNTIL;
import static calendar.controller.CommandPatterns.CREATE_SINGLE;
import static calendar.controller.CommandPatterns.EDIT_EVENTS;
import static calendar.controller.CommandPatterns.EDIT_SERIES;
import static calendar.controller.CommandPatterns.EDIT_SINGLE;

import calendar.controller.EditProperty;
import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;

/**
 * Shared service layer that encapsulates parsing + validation logic for CLI commands so that
 * future GUI flows can reuse the same builders and error messages.
 */
public class CalendarFormService {
  private final EventDraftFactory draftFactory = new EventDraftFactory();
  private final EventPatchFactory patchFactory = new EventPatchFactory();

  /**
   * Parses a "create event" CLI command into a normalized {@link EventCreationRequest}.
   *
   * @param input raw user command
   * @return normalized request
   */
  public EventCreationRequest parseCreateEventCommand(String input) {
    String trimmed = input.trim();

    if (trimmed.matches(CREATE_SINGLE)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      String subject = parts[2];
      LocalDateTime from = LocalDateTime.parse(parts[4]);
      LocalDateTime to = LocalDateTime.parse(parts[6]);

      return new EventCreationRequest.Builder()
          .pattern(EventCreationRequest.Pattern.SINGLE_TIMED)
          .subject(subject)
          .startDateTime(from)
          .endDateTime(to)
          .build();
    } else if (trimmed.matches(CREATE_REPEAT_N)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      String subject = parts[2];
      LocalDateTime start = LocalDateTime.parse(parts[4]);
      LocalDateTime end = LocalDateTime.parse(parts[6]);
      EnumSet<Weekday> weekdays = weekdaysSetFromString(parts[8]);
      int n = Integer.parseInt(parts[10]);

      return new EventCreationRequest.Builder()
          .pattern(EventCreationRequest.Pattern.RECURRING_TIMED_COUNT)
          .subject(subject)
          .startDateTime(start)
          .endDateTime(end)
          .weekdays(weekdays)
          .occurrences(n)
          .build();
    } else if (trimmed.matches(CREATE_REPEAT_UNTIL)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      String subject = parts[2];
      LocalDateTime start = LocalDateTime.parse(parts[4]);
      LocalDateTime end = LocalDateTime.parse(parts[6]);
      EnumSet<Weekday> weekdays = weekdaysSetFromString(parts[8]);
      LocalDate until = LocalDate.parse(parts[10]);
      LocalDate startDate = start.toLocalDate();
      if (!until.isAfter(startDate) && !until.isEqual(startDate)) {
        throw new IllegalArgumentException(
            "Fields are invalid Until date must be after or equal to start date");
      }

      return new EventCreationRequest.Builder()
          .pattern(EventCreationRequest.Pattern.RECURRING_TIMED_UNTIL)
          .subject(subject)
          .startDateTime(start)
          .endDateTime(end)
          .weekdays(weekdays)
          .untilDate(until)
          .build();
    } else if (trimmed.matches(CREATE_ALLDAY)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      String subject = parts[2];
      LocalDate onDate = LocalDate.parse(parts[4]);

      return new EventCreationRequest.Builder()
          .pattern(EventCreationRequest.Pattern.SINGLE_ALL_DAY)
          .subject(subject)
          .allDayDate(onDate)
          .build();
    } else if (trimmed.matches(CREATE_ALLDAY_REPEAT_N)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      String subject = parts[2];
      LocalDate onDate = LocalDate.parse(parts[4]);
      EnumSet<Weekday> weekdays = weekdaysSetFromString(parts[6]);
      int n = Integer.parseInt(parts[8]);

      return new EventCreationRequest.Builder()
          .pattern(EventCreationRequest.Pattern.RECURRING_ALL_DAY_COUNT)
          .subject(subject)
          .allDayDate(onDate)
          .weekdays(weekdays)
          .occurrences(n)
          .build();
    } else if (trimmed.matches(CREATE_ALLDAY_REPEAT_UNTIL)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      String subject = parts[2];
      LocalDate onDate = LocalDate.parse(parts[4]);
      EnumSet<Weekday> weekdays = weekdaysSetFromString(parts[6]);
      LocalDate until = LocalDate.parse(parts[8]);
      if (!until.isAfter(onDate) && !until.isEqual(onDate)) {
        throw new IllegalArgumentException(
            "Fields are invalid Until date must be after or equal to start date");
      }

      return new EventCreationRequest.Builder()
          .pattern(EventCreationRequest.Pattern.RECURRING_ALL_DAY_UNTIL)
          .subject(subject)
          .allDayDate(onDate)
          .weekdays(weekdays)
          .untilDate(until)
          .build();
    }

    throw new IllegalArgumentException("Error: Invalid create event command format.");
  }

  /**
   * Applies the provided {@link EventCreationRequest} to the supplied calendar.
   */
  public void applyCreateEvent(EventCreationRequest request, CalendarApi calendar) {
    switch (request.pattern()) {
      case SINGLE_TIMED:
        EventDraft timed = draftFactory.timedEvent(
            request.subject(),
            request.startDateTime().orElseThrow(),
            request.endDateTime().orElseThrow());
        calendar.create(timed);
        break;
      case SINGLE_ALL_DAY:
        EventDraft allDay = draftFactory.allDayEvent(
            request.subject(),
            request.allDayDate().orElseThrow());
        calendar.create(allDay);
        break;
      case RECURRING_TIMED_COUNT:
        SeriesDraft timedCount = draftFactory.recurringTimedSeries(
            request.subject(),
            request.startDateTime().orElseThrow().toLocalDate(),
            request.startDateTime().orElseThrow().toLocalTime(),
            request.endDateTime().orElseThrow().toLocalTime(),
            request.weekdays(),
            request.occurrences(),
            Optional.empty());
        calendar.createSeries(timedCount);
        break;
      case RECURRING_TIMED_UNTIL:
        SeriesDraft timedUntil = draftFactory.recurringTimedSeries(
            request.subject(),
            request.startDateTime().orElseThrow().toLocalDate(),
            request.startDateTime().orElseThrow().toLocalTime(),
            request.endDateTime().orElseThrow().toLocalTime(),
            request.weekdays(),
            Optional.empty(),
            request.untilDate());
        calendar.createSeries(timedUntil);
        break;
      case RECURRING_ALL_DAY_COUNT:
        SeriesDraft allDayCount = draftFactory.recurringAllDaySeries(
            request.subject(),
            request.allDayDate().orElseThrow(),
            request.weekdays(),
            request.occurrences(),
            Optional.empty());
        calendar.createSeries(allDayCount);
        break;
      case RECURRING_ALL_DAY_UNTIL:
        SeriesDraft allDayUntil = draftFactory.recurringAllDaySeries(
            request.subject(),
            request.allDayDate().orElseThrow(),
            request.weekdays(),
            Optional.empty(),
            request.untilDate());
        calendar.createSeries(allDayUntil);
        break;
      default:
        throw new IllegalStateException("Unsupported creation pattern: " + request.pattern());
    }
  }

  /**
   * Parses an "edit ..." command into an {@link EventEditRequest}.
   *
   * @param input raw command.
   */
  public EventEditRequest parseEditEventCommand(String input) {
    String trimmed = input.trim();

    if (trimmed.matches(EDIT_SINGLE)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      EditProperty property = EditProperty.from(parts[2])
          .orElseThrow(() -> new IllegalArgumentException("Invalid property: " + parts[2]));
      String subject = parts[3];
      LocalDateTime from = LocalDateTime.parse(parts[5]);
      LocalDateTime to = LocalDateTime.parse(parts[7]);
      String newValue = parts[9];

      return new EventEditRequest.Builder()
          .property(property)
          .subject(subject)
          .start(from)
          .end(to)
          .newValue(newValue)
          .scope(EditScope.SINGLE)
          .build();
    } else if (trimmed.matches(EDIT_EVENTS)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      EditProperty property = EditProperty.from(parts[2])
          .orElseThrow(() -> new IllegalArgumentException("Invalid property: " + parts[2]));
      String subject = parts[3];
      LocalDateTime from = LocalDateTime.parse(parts[5]);
      String newValue = parts[7];

      return new EventEditRequest.Builder()
          .property(property)
          .subject(subject)
          .start(from)
          .newValue(newValue)
          .scope(EditScope.FOLLOWING)
          .build();
    } else if (trimmed.matches(EDIT_SERIES)) {
      String[] parts = CommandTokenizer.tokenize(trimmed);
      EditProperty property = EditProperty.from(parts[2])
          .orElseThrow(() -> new IllegalArgumentException("Invalid property: " + parts[2]));
      String subject = parts[3];
      LocalDateTime from = LocalDateTime.parse(parts[5]);
      String newValue = parts[7];

      return new EventEditRequest.Builder()
          .property(property)
          .subject(subject)
          .start(from)
          .newValue(newValue)
          .scope(EditScope.ENTIRE_SERIES)
          .build();
    }

    throw new IllegalArgumentException("Error: Invalid edit command format.");
  }

  /**
   * Applies an edit request to the {@link CalendarApi}.
   */
  public void applyEditEvent(EventEditRequest request, CalendarApi calendar) {
    EventSelector selector = new EventSelector();
    selector.subject = request.subject();
    selector.start = request.start();
    request.end().ifPresent(value -> selector.end = Optional.of(value));

    EventPatch patch;
    try {
      patch = patchFactory.create(request.property(), request.newValue());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid property value: " + e.getMessage(), e);
    }

    calendar.updateBySelector(selector, patch, request.scope());
  }

  private EnumSet<Weekday> weekdaysSetFromString(String weekdays) {
    EnumSet<Weekday> set = EnumSet.noneOf(Weekday.class);
    for (char c : weekdays.toCharArray()) {
      set.add(Weekday.valueOf(String.valueOf(c).toUpperCase(Locale.ROOT)));
    }
    return set;
  }
}
