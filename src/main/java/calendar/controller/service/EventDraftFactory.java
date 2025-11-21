package calendar.controller.service;

import calendar.model.api.EventDraft;
import calendar.model.api.SeriesDraft;
import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Factory that knows how to convert normalized {@link EventCreationRequest} data into
 * {@link EventDraft} or {@link SeriesDraft} objects understood by the model.
 */
public class EventDraftFactory {
  /**
   * Builds a timed single event.
   */
  public EventDraft timedEvent(String subject, LocalDateTime start, LocalDateTime end) {
    EventDraft draft = new EventDraft();
    draft.subject = subject;
    draft.start = Optional.of(start);
    draft.end = Optional.of(end);
    return draft;
  }

  /**
   * Builds an all-day single event.
   */
  public EventDraft allDayEvent(String subject, LocalDate onDate) {
    EventDraft draft = new EventDraft();
    draft.subject = subject;
    draft.allDayDate = Optional.of(onDate);
    return draft;
  }

  /**
   * Builds a recurring timed series (count or until).
   */
  public SeriesDraft recurringTimedSeries(String subject,
                                          LocalDate startDate,
                                          LocalTime startTime,
                                          LocalTime endTime,
                                          EnumSet<Weekday> weekdays,
                                          Optional<Integer> count,
                                          Optional<LocalDate> untilDate) {
    SeriesDraft draft = baseSeriesDraft(subject, weekdays, count, untilDate);
    draft.startDate = startDate;
    draft.startTime = Optional.of(startTime);
    draft.endTime = Optional.of(endTime);
    draft.allDay = false;
    return draft;
  }

  /**
   * Builds a recurring all-day series (count or until).
   */
  public SeriesDraft recurringAllDaySeries(String subject,
                                           LocalDate startDate,
                                           EnumSet<Weekday> weekdays,
                                           Optional<Integer> count,
                                           Optional<LocalDate> untilDate) {
    SeriesDraft draft = baseSeriesDraft(subject, weekdays, count, untilDate);
    draft.startDate = startDate;
    draft.allDay = true;
    return draft;
  }

  private SeriesDraft baseSeriesDraft(String subject,
                                      EnumSet<Weekday> weekdays,
                                      Optional<Integer> count,
                                      Optional<LocalDate> untilDate) {
    SeriesDraft draft = new SeriesDraft();
    draft.subject = subject;
    Optional<Integer> normalizedCount = count == null ? Optional.empty() : count;
    Optional<LocalDate> normalizedUntil = untilDate == null ? Optional.empty() : untilDate;
    draft.rule = new RecurrenceRule(weekdays, normalizedCount, normalizedUntil);
    return draft;
  }
}
