package calendar.model.api;

import static org.junit.Assert.assertThrows;

import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Test;

/**
 * Tests for SeriesDraft precheck validations.
 */
public final class SeriesDraftTest {

  private static RecurrenceRule rule() {
    return new RecurrenceRule(EnumSet.of(Weekday.M), Optional.of(1), Optional.empty());
  }

  /**
   * Validates subject is required and non-blank.
   */
  @Test
  public void subject_null_or_blank_rejected() {
    SeriesDraft d1 = new SeriesDraft();
    d1.subject = null;
    d1.startDate = LocalDate.now();
    d1.rule = rule();
    assertThrows(IllegalArgumentException.class, d1::precheck);

    SeriesDraft d2 = new SeriesDraft();
    d2.subject = "  ";
    d2.startDate = LocalDate.now();
    d2.rule = rule();
    assertThrows(IllegalArgumentException.class, d2::precheck);
  }

  /**
   * Validates startDate and rule are required.
   */
  @Test
  public void missing_startDate_or_rule_rejected() {
    SeriesDraft d1 = new SeriesDraft();
    d1.subject = "S";
    d1.rule = rule();
    assertThrows(NullPointerException.class, d1::precheck);

    SeriesDraft d2 = new SeriesDraft();
    d2.subject = "S";
    d2.startDate = LocalDate.now();
    assertThrows(NullPointerException.class, d2::precheck);
  }

  /**
   * Validates timed series require times and end > start (including missing end).
   */
  @Test
  public void timed_series_require_times_and_end_after_start() {
    SeriesDraft d1 = new SeriesDraft();
    d1.subject = "S";
    d1.startDate = LocalDate.now();
    d1.rule = rule();
    d1.allDay = false;
    assertThrows(IllegalArgumentException.class, d1::precheck);

    SeriesDraft d2 = new SeriesDraft();
    d2.subject = "S";
    d2.startDate = LocalDate.now();
    d2.rule = rule();
    d2.allDay = false;
    d2.startTime = Optional.of(LocalTime.of(10, 0));
    d2.endTime = Optional.of(LocalTime.of(10, 0));
    assertThrows(IllegalArgumentException.class, d2::precheck);

    SeriesDraft d3 = new SeriesDraft();
    d3.subject = "S";
    d3.startDate = LocalDate.now();
    d3.rule = rule();
    d3.allDay = false;
    d3.startTime = Optional.of(LocalTime.of(9, 0));
    assertThrows(IllegalArgumentException.class, d3::precheck);
  }

  /**
   * All-day series do not require explicit times.
   */
  @Test
  public void allDay_series_allow_missing_times() {
    SeriesDraft d = new SeriesDraft();
    d.subject = "S";
    d.startDate = LocalDate.now();
    d.rule = rule();
    d.allDay = true;
    d.precheck();
  }
}

