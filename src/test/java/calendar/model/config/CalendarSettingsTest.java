package calendar.model.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import calendar.model.domain.Status;
import java.time.LocalTime;
import org.junit.Test;

/**
 * Tests for CalendarSettings validations.
 */
public final class CalendarSettingsTest {

  @Test
  public void end_not_after_start_rejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new CalendarSettings(LocalTime.of(9, 0), LocalTime.of(9, 0), Status.PUBLIC));
  }

  /**
   * defaults() returns non-null with PUBLIC status.
   */
  @Test
  public void defaults_returnsValidSettings() {
    CalendarSettings settings = CalendarSettings.defaults();
    assertNotNull(settings);
    assertNotNull(settings.defaultStatus());
    assertEquals(Status.PUBLIC, settings.defaultStatus());
    assertNotNull(settings.allDayStart());
    assertNotNull(settings.allDayEnd());
  }
}

