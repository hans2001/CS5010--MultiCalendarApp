package calendar.model.recurrence;

import static org.junit.Assert.assertEquals;

import java.time.DayOfWeek;
import org.junit.Test;

/**
 * Verifies the letter mapping defined by {@link Weekday#from(DayOfWeek)} so the GUI + CLI share
 * identical recurrence semantics.
 */
public final class WeekdayTest {

  @Test
  public void fromCoversEveryDayOfWeek() {
    assertEquals(Weekday.M, Weekday.from(DayOfWeek.MONDAY));
    assertEquals(Weekday.T, Weekday.from(DayOfWeek.TUESDAY));
    assertEquals(Weekday.W, Weekday.from(DayOfWeek.WEDNESDAY));
    assertEquals(Weekday.R, Weekday.from(DayOfWeek.THURSDAY));
    assertEquals(Weekday.F, Weekday.from(DayOfWeek.FRIDAY));
    assertEquals(Weekday.S, Weekday.from(DayOfWeek.SATURDAY));
    assertEquals(Weekday.U, Weekday.from(DayOfWeek.SUNDAY));
  }
}
