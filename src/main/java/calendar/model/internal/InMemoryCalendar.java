package calendar.model.internal;

import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.SeriesId;
import calendar.model.domain.Status;
import calendar.model.exception.ValidationException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link CalendarApi}.
 *
 * <p>This class orchestrates calendar operations by delegating to specialized components:
 * Normalizer, UniquenessIndex, SeriesIndex, SelectorResolver, PatchApplier, RecurrenceExpander.</p>
 *
 * <h2>Design: MVC Model Layer</h2>
 *
 * <p>This is the core Model with no IO dependencies. The Controller parses commands and calls
 * these methods. The View formats results for display. This separation allows the same model
 * to work with CLI, GUI, or web interfaces.</p>
 *
 * <h2>Data Representation</h2>
 *
 * <p><b>Why HashMap for events?</b> Edit operations need fast lookups by ID. HashMap gives O(1)
 * access vs. O(n) for a List. Since events are accessed by ID frequently (every edit, query),
 * this performance matters.</p>
 *
 * <p><b>Why separate indices?</b> Keeps concerns separate. UniquenessIndex handles collision
 * detection. SeriesIndex handles recurring event relationships. Each can be tested alone.</p>
 *
 * <p><b>Why store series as individual events?</b> Makes queries simple--there's just one event
 * type. Series metadata lives in SeriesIndex, so events can be detached or split without
 * special handling.</p>
 */
public class InMemoryCalendar implements CalendarApi {

  private final Map<EventId, Event> byId = new HashMap<>();

  private final UniquenessIndex uniqueness = new UniquenessIndex();

  private final SeriesIndex seriesIndex = new SeriesIndex();

  private final CalendarSettings settings;
  private final Normalizer normalizer;
  private final RecurrenceExpander expander;

  /**
   * Adjusts a patch for series-wide edits (FOLLOWING/ENTIRE_SERIES).
   *
   * <p>When editing multiple events in a series, we apply the time change to each event's
   * own date, not literally copy the datetime. For example, changing start to "10:30 on May 5"
   * means "10:30 on each event's date" (May 5, May 12, etc.).</p>
   *
   * <p><b>Why preserve duration?</b> Commands let you change start without specifying end.
   * If we don't adjust the end time, you'd get invalid events (end before start). Users
   * expect the meeting length to stay the same--just the time shifts. This matches how
   * Google Calendar works.</p>
   *
   * <p><b>Example:</b> Series meets 10:00-11:15 on May 5, 12, 19. Edit start to 9:30 (entire
   * series). Result: May 5 9:30-10:45, May 12 9:30-10:45, May 19 9:30-10:45. The 1hr 15min
   * duration is preserved.</p>
   */
  private static EventPatch adjustPatchForEvent(
      Event current,
      EventPatch patch
  ) {
    EventPatch per = new EventPatch();
    per.subject = patch.subject;
    per.description = patch.description;
    per.location = patch.location;
    per.status = patch.status;

    if (patch.start.isPresent()) {
      LocalDateTime newStart =
          current.start().toLocalDate().atTime(patch.start.get().toLocalTime());
      per.start = Optional.of(newStart);

      if (patch.end.isPresent()) {
        LocalDateTime newEnd = current.end().toLocalDate().atTime(patch.end.get().toLocalTime());
        per.end = Optional.of(newEnd);
      } else {
        Duration dur = Duration.between(current.start(), current.end());
        per.end = Optional.of(newStart.plus(dur));
      }
    } else {
      per.end = patch.end;
    }
    return per;
  }

  /**
   * Creates a calendar with settings (all-day window, default status).
   *
   * @param settings configuration for service policy
   */
  public InMemoryCalendar(CalendarSettings settings) {
    this.settings = Objects.requireNonNull(settings, "settings");
    this.normalizer = new Normalizer(this.settings);
    this.expander = new RecurrenceExpander();
  }

  /**
   * Convenience constructor with sensible defaults.
   */
  public InMemoryCalendar() {
    this(CalendarSettings.defaults());
  }

  @Override
  public synchronized EventId create(EventDraft draft) {
    Objects.requireNonNull(draft, "draft");
    if (draft.subject == null || draft.subject.trim().isEmpty()) {
      throw new ValidationException("Subject is required");
    }
    Normalizer.EventTimes t = normalizer.normalizeTimes(draft);
    if (!t.end.isAfter(t.start)) {
      throw new ValidationException("End must be after start");
    }
    Status status = normalizer.resolveStatus(draft.status);

    uniqueness.addOrThrow(draft.subject, t.start, t.end);

    Event e = new Event.Builder()
        .subject(draft.subject.trim())
        .start(t.start)
        .end(t.end)
        .description(draft.description.orElse(""))
        .location(draft.location.orElse(""))
        .status(status)
        .build();

    byId.put(e.id(), e);
    return e.id();
  }

  @Override
  public synchronized SeriesId createSeries(SeriesDraft draft) {
    Objects.requireNonNull(draft, "draft");
    draft.precheck();

    // Normalize time-of-day template and status
    Status status = normalizer.resolveStatus(draft.status);
    LocalTime[] times = draft.allDay
        ? new LocalTime[] {settings.allDayStart(), settings.allDayEnd()}
        : new LocalTime[] {draft.startTime.get(), draft.endTime.get()};

    List<LocalDate> dates = expander.expand(draft.startDate, draft.rule);

    List<EventId> created = new ArrayList<>();
    for (LocalDate date : dates) {
      LocalDateTime s = date.atTime(times[0]);
      LocalDateTime e = date.atTime(times[1]);
      uniqueness.addOrThrow(draft.subject, s, e);

      Event ev = new Event.Builder()
          .subject(draft.subject.trim())
          .start(s)
          .end(e)
          .description(draft.description.orElse(""))
          .location(draft.location.orElse(""))
          .status(status)
          .build();

      byId.put(ev.id(), ev);
      created.add(ev.id());
    }

    UUID sid = seriesIndex.registerSeries(created);
    return new SeriesId(sid);
  }

  @Override
  public synchronized void updateBySelector(EventSelector selector, EventPatch patch,
                                            EditScope scope) {
    Objects.requireNonNull(selector, "selector");
    Objects.requireNonNull(patch, "patch");
    Objects.requireNonNull(scope, "scope");

    SelectorResolver resolver = new SelectorResolver(byId);
    Event anchor = resolver.resolve(selector);

    Optional<UUID> sidOpt = seriesIndex.seriesOf(anchor.id());
    EditScope effective = sidOpt.isPresent() ? scope : EditScope.SINGLE;

    boolean changesStart = patch.start
        .map(newStart -> !newStart.equals(anchor.start()))
        .orElse(false);

    PatchApplier applier = new PatchApplier(byId, uniqueness);

    switch (effective) {
      case SINGLE:
        {
          if (sidOpt.isPresent() && changesStart) {
            seriesIndex.detach(anchor.id());
          }
          applier.apply(anchor.id(), patch);
          break;
        }

      case FOLLOWING:
        {
          UUID sid = sidOpt.get();
          final List<EventId> targets;
          if (changesStart) {
            UUID newSid = seriesIndex.splitFollowing(sid, anchor.start(), byId);
            targets = seriesIndex.all(newSid);
          } else {
            targets = seriesIndex.following(sid, anchor.start(), byId);
          }
          for (EventId id : targets) {
            applier.apply(id, adjustPatchForEvent(byId.get(id), patch));
          }
          break;
        }

      case ENTIRE_SERIES:
        {
          UUID sid = sidOpt.get();
          for (EventId id : seriesIndex.all(sid)) {
            applier.apply(id, adjustPatchForEvent(byId.get(id), patch));
          }
          break;
        }

      default:
        throw new IllegalArgumentException("Unknown scope: " + effective);
    }
  }

  @Override
  public synchronized List<Event> eventsOn(LocalDate date) {
    LocalDateTime a = date.atStartOfDay();
    LocalDateTime b = date.plusDays(1).atStartOfDay();
    return eventsOverlapping(a, b);
  }

  @Override
  public synchronized List<Event> eventsOverlapping(LocalDateTime from, LocalDateTime to) {
    if (!to.isAfter(from)) {
      throw new ValidationException("Range end must be after start");
    }
    return byId.values().stream()
        .filter(e -> e.start().isBefore(to) && e.end().isAfter(from))
        .sorted(Comparator.comparing(Event::start))
        .collect(Collectors.toList());
  }

  @Override
  public synchronized BusyStatus statusAt(LocalDateTime instant) {
    boolean busy = byId.values().stream()
        .anyMatch(e -> !e.start().isAfter(instant) && e.end().isAfter(instant));
    return busy ? BusyStatus.BUSY : BusyStatus.AVAILABLE;
  }

  @Override
  public synchronized List<Event> allEvents() {
    return byId.values().stream()
        .sorted(Comparator.comparing(Event::start))
        .collect(Collectors.toList());
  }

}
