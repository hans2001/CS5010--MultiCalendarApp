package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.model.api.EventDraft;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Test;

/**
 * Tests for Normalizer time normalization and status resolution.
 */
public final class NormalizerTest {

  /**
   * resolveStatus returns default when status is absent.
   */
  @Test
  public void resolveStatus_absent_returnsDefault() {
    CalendarSettings settings = CalendarSettings.defaults();
    Normalizer normalizer = new Normalizer(settings);

    Status result = normalizer.resolveStatus(Optional.empty());
    assertNotNull(result);
    assertEquals(Status.PUBLIC, result);
  }

  /**
   * resolveStatus returns provided status when present.
   */
  @Test
  public void resolveStatus_present_returnsProvided() {
    CalendarSettings settings = CalendarSettings.defaults();
    Normalizer normalizer = new Normalizer(settings);

    Status result = normalizer.resolveStatus(Optional.of(Status.PRIVATE));
    assertNotNull(result);
    assertEquals(Status.PRIVATE, result);
  }

  /**
   * normalizeTimes expands all-day drafts using settings window.
   */
  @Test
  public void normalizeTimes_allDay_expandsToWindow() {
    CalendarSettings settings = CalendarSettings.defaults();
    Normalizer normalizer = new Normalizer(settings);

    EventDraft d = new EventDraft();
    d.allDayDate = Optional.of(LocalDate.of(2025, 5, 5));

    Normalizer.EventTimes t = normalizer.normalizeTimes(d);
    assertEquals(LocalDateTime.of(2025, 5, 5, 8, 0), t.start);
    assertEquals(LocalDateTime.of(2025, 5, 5, 17, 0), t.end);
  }

  /**
   * normalizeTimes uses provided start and end when both present.
   */
  @Test
  public void normalizeTimes_timedWithEnd_usesProvided() {
    CalendarSettings settings = CalendarSettings.defaults();
    Normalizer normalizer = new Normalizer(settings);

    EventDraft d = new EventDraft();
    d.start = Optional.of(LocalDateTime.of(2025, 5, 5, 10, 0));
    d.end = Optional.of(LocalDateTime.of(2025, 5, 5, 11, 0));

    Normalizer.EventTimes t = normalizer.normalizeTimes(d);
    assertEquals(LocalDateTime.of(2025, 5, 5, 10, 0), t.start);
    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0), t.end);
  }

  /**
   * normalizeTimes expands to all-day when start present and end missing.
   */
  @Test
  public void normalizeTimes_startOnlyNoEnd_expandsToAllDay() {
    CalendarSettings settings = CalendarSettings.defaults();
    Normalizer normalizer = new Normalizer(settings);

    EventDraft d = new EventDraft();
    d.start = Optional.of(LocalDateTime.of(2025, 5, 5, 10, 30));

    Normalizer.EventTimes t = normalizer.normalizeTimes(d);
    assertEquals(LocalDateTime.of(2025, 5, 5, 8, 0), t.start);
    assertEquals(LocalDateTime.of(2025, 5, 5, 17, 0), t.end);
  }
}
