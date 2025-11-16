package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

/**
 * Tests for RecurrenceExpander.
 */
public final class RecurrenceExpanderTest {
  /**
   * Expands for a fixed count across multiple weekdays, ensuring order and correctness.
   */
  @Test
  public void expand_countOnWeekdays() {
    RecurrenceExpander expander = new RecurrenceExpander();
    RecurrenceRule rule = new RecurrenceRule(
        EnumSet.of(Weekday.M, Weekday.W),
        Optional.of(3),
        Optional.empty());

    List<LocalDate> dates = expander.expand(LocalDate.of(2025, 5, 5), rule);
    assertEquals(3, dates.size());
    assertEquals(LocalDate.of(2025, 5, 5), dates.get(0));
    assertEquals(LocalDate.of(2025, 5, 7), dates.get(1));
    assertEquals(LocalDate.of(2025, 5, 12), dates.get(2));
  }

  /**
   * Expands through an inclusive end date for a single weekday.
   */
  @Test
  public void expand_untilInclusive() {
    RecurrenceExpander expander = new RecurrenceExpander();
    RecurrenceRule rule = new RecurrenceRule(
        EnumSet.of(Weekday.M),
        Optional.empty(),
        Optional.of(LocalDate.of(2025, 5, 19)));

    List<LocalDate> dates = expander.expand(LocalDate.of(2025, 5, 5), rule);
    assertEquals(3, dates.size());
    assertEquals(LocalDate.of(2025, 5, 19), dates.get(2));
  }

  /**
   * Maps all weekdays correctly.
   */
  @Test
  public void map_allWeekdays_areExpandedCorrectly() {
    final RecurrenceExpander expander = new RecurrenceExpander();
    final LocalDate start = LocalDate.of(2025, 5, 5);

    Map<Weekday, DayOfWeek> expected = new EnumMap<>(Weekday.class);
    expected.put(Weekday.M, DayOfWeek.MONDAY);
    expected.put(Weekday.T, DayOfWeek.TUESDAY);
    expected.put(Weekday.W, DayOfWeek.WEDNESDAY);
    expected.put(Weekday.R, DayOfWeek.THURSDAY);
    expected.put(Weekday.F, DayOfWeek.FRIDAY);
    expected.put(Weekday.S, DayOfWeek.SATURDAY);
    expected.put(Weekday.U, DayOfWeek.SUNDAY);

    for (var e : expected.entrySet()) {
      RecurrenceRule rule =
          new RecurrenceRule(EnumSet.of(e.getKey()), Optional.of(1), Optional.empty());
      List<LocalDate> dates = expander.expand(start, rule);
      assertEquals(1, dates.size());
      assertEquals(e.getValue(), dates.get(0).getDayOfWeek());
    }
  }

  @Test
  public void expand_guardThrowsWhenWeekdaysCorrupted() {
    RecurrenceExpander expander = new RecurrenceExpander();
    RecurrenceRule rule = new RecurrenceRule(
        EnumSet.of(Weekday.M),
        Optional.of(1),
        Optional.empty());

    rule.weekdays.clear();

    assertThrows(IllegalStateException.class,
        () -> expander.expand(LocalDate.of(2025, 5, 5), rule));
  }
}
