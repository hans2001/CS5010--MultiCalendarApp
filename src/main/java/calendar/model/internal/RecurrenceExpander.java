package calendar.model.internal;

import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Expands a {@link RecurrenceRule} into concrete calendar dates.
 *
 * <p>Implements the assignment semantics:
 * repeat on given weekdays either for N occurrences or until a date (inclusive).</p>
 */
final class RecurrenceExpander {

  List<LocalDate> expand(LocalDate startDate, RecurrenceRule rule) {
    EnumSet<Weekday> weekdays = rule.weekdays;
    Set<DayOfWeek> wanted = weekdays.stream()
        .map(RecurrenceExpander::map)
        .collect(Collectors.toSet());
    if (wanted.isEmpty() || wanted.contains(null)) {
      throw new IllegalStateException("Invalid weekday mapping for recurrence expansion");
    }
    List<LocalDate> out = new ArrayList<>();
    LocalDate d = startDate;

    if (rule.count.isPresent()) {
      int n = rule.count.get();
      PriorityQueue<LocalDate> pq = new PriorityQueue<>();
      int startDow = startDate.getDayOfWeek().getValue(); // 1=Mon..7=Sun
      for (DayOfWeek dow : wanted) {
        int target = dow.getValue();
        int delta = (target - startDow + 7) % 7; // days to next target, 0..6
        pq.add(startDate.plusDays(delta));
      }

      for (int i = 0; i < n; i++) {
        LocalDate next = pq.poll();
        if (next == null) {
          throw new IllegalStateException("Recurrence expansion queue unexpectedly empty");
        }
        out.add(next);
        pq.add(next.plusDays(7));
      }
    } else {
      LocalDate until = rule.untilDate.get();
      while (!d.isAfter(until)) {
        if (wanted.contains(d.getDayOfWeek())) {
          out.add(d);
        }
        d = d.plusDays(1);
      }
    }
    return out;
  }

  private static DayOfWeek map(Weekday w) {
    switch (w) {
      case M: return DayOfWeek.MONDAY;
      case T: return DayOfWeek.TUESDAY;
      case W: return DayOfWeek.WEDNESDAY;
      case R: return DayOfWeek.THURSDAY;
      case F: return DayOfWeek.FRIDAY;
      case S: return DayOfWeek.SATURDAY;
      case U: return DayOfWeek.SUNDAY;
      default: throw new IllegalArgumentException("Unknown weekday: " + w);
    }
  }
}
