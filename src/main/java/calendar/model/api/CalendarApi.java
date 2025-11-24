package calendar.model.api;

import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.SeriesId;
import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Calendar operations (model layer, EST timezone).
 *
 * <h2>MVC Design</h2>
 *
 * <p>This is the Model interface. Controller parses commands and calls these methods.
 * View formats results for display. This separation means the same model works with
 * different interfaces (CLI, GUI, REST API).</p>
 *
 * <p>Controller depends on this interface, not InMemoryCalendar directly. This lets us
 * swap implementations (e.g., DatabaseCalendar) without changing the controller.</p>
 */
public interface CalendarApi {

  /**
   * Creates a single event.
   *
   * @param draft event details (subject required, end optional)
   * @return generated event ID
   * @throws ValidationException if fields are invalid
   * @throws ConflictException   if (subject,start,end) already exists
   */
  EventId create(EventDraft draft);

  /**
   * Creates a recurring event series.
   *
   * @param draft recurring event details and rules
   * @return series identifier
   * @throws ValidationException if series is invalid
   * @throws ConflictException   if any instance conflicts with existing events
   */
  SeriesId createSeries(SeriesDraft draft);

  /**
   * Updates event(s) using a selector and edit scope.
   *
   * @param selector identifies the event (subject/start[/end])
   * @param patch    fields to modify
   * @param scope    which events to update (single, following, series)
   * @throws NotFoundException   if no event matches
   * @throws ValidationException if new values are invalid
   * @throws ConflictException   if update creates a duplicate
   */
  void updateBySelector(EventSelector selector, EventPatch patch, EditScope scope);

  /**
   * Returns all events overlapping a given date.
   *
   * @param date date in EST
   * @return list of events sorted by start time
   */
  List<Event> eventsOn(LocalDate date);

  /**
   * Returns all events overlapping a time interval.
   *
   * @param from interval start (inclusive)
   * @param to   interval end (exclusive)
   * @return events sorted by start time
   * @throws ValidationException if {@code to <= from}
   */
  List<Event> eventsOverlapping(LocalDateTime from, LocalDateTime to);

  /**
   * Returns calendar status at a given instant.
   *
   * @param instant timestamp in EST
   * @return {@link BusyStatus#BUSY} or {@link BusyStatus#AVAILABLE}
   */
  BusyStatus statusAt(LocalDateTime instant);

  /**
   * Returns a snapshot of all events.
   *
   * @return list of all events sorted by start time
   */
  List<Event> allEvents();

  /**
   * Returns the series identifier an event belongs to, if any.
   *
   * @param eventId event identifier
   * @return optional series identifier
   */
  Optional<SeriesId> seriesOfEvent(EventId eventId);

  /**
   * Converts each event timestamp from {@code fromZone} to {@code toZone}.
   *
   * @param fromZone original timezone for stored timestamps
   * @param toZone   new timezone to interpret the timestamps in
   */
  void convertTimeZone(ZoneId fromZone, ZoneId toZone);

}
