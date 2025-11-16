package calendar.model.internal;

import calendar.model.api.EventDraft;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.Status;
import calendar.model.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Normalizes event times and status using configured policies.
 *
 * <p>Converts missing end times into all-day windows (8am-5pm by default).
 * Converts allDayDate into the configured window. Provides default status when absent.</p>
 *
 * <p>Settings-driven so all-day hours can be changed (e.g., 9am-6pm) without code changes.</p>
 */
final class Normalizer {
  static final class EventTimes {
    final LocalDateTime start;
    final LocalDateTime end;

    EventTimes(LocalDateTime start, LocalDateTime end) {
      this.start = start;
      this.end = end;
    }
  }

  private final CalendarSettings settings;

  Normalizer(CalendarSettings settings) {
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  EventTimes normalizeTimes(EventDraft draft) {
    if (draft.allDayDate.isPresent()) {
      LocalDate d = draft.allDayDate.get();
      return new EventTimes(d.atTime(settings.allDayStart()), d.atTime(settings.allDayEnd()));
    }
    var start = draft.start.orElseThrow(() -> new ValidationException("start is required"));
    if (draft.end.isPresent()) {
      return new EventTimes(start, draft.end.get());
    }
    LocalDate d = start.toLocalDate();
    return new EventTimes(d.atTime(settings.allDayStart()), d.atTime(settings.allDayEnd()));
  }

  Status resolveStatus(Optional<Status> maybeStatus) {
    return maybeStatus.orElse(settings.defaultStatus());
  }
}
