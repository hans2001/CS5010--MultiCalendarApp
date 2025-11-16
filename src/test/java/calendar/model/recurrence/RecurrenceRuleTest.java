package calendar.model.recurrence;

import static org.junit.Assert.assertThrows;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Test;

/**
 * Tests for RecurrenceRule constructor validations.
 */
public final class RecurrenceRuleTest {

  @Test
  public void weekdays_empty_rejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new RecurrenceRule(EnumSet.noneOf(Weekday.class), Optional.of(1), Optional.empty()));
  }

  @Test
  public void both_count_and_until_rejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new RecurrenceRule(EnumSet.of(Weekday.M), Optional.of(1),
            Optional.of(LocalDate.now())));
  }

  @Test
  public void neither_count_nor_until_rejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new RecurrenceRule(EnumSet.of(Weekday.M), Optional.empty(), Optional.empty()));
  }

  @Test
  public void non_positive_count_rejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new RecurrenceRule(EnumSet.of(Weekday.M), Optional.of(0), Optional.empty()));
  }
}


