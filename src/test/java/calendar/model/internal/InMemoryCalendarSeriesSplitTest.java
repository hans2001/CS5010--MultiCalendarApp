package calendar.model.internal;

import static org.junit.Assert.assertEquals;

import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.domain.Event;
import calendar.model.domain.Status;
import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

/**
 * Tests that series edits that change start time split/detach as designed.
 */
public final class InMemoryCalendarSeriesSplitTest {

  private static SeriesDraft weeklyMonWed(String subject, LocalDate startDate, LocalTime start,
                                          LocalTime end, int count) {
    SeriesDraft d = new SeriesDraft();
    d.subject = subject;
    d.allDay = false;
    d.startDate = startDate;
    d.startTime = Optional.of(start);
    d.endTime = Optional.of(end);
    d.rule =
        new RecurrenceRule(EnumSet.of(Weekday.M, Weekday.W), Optional.of(count), Optional.empty());
    d.status = Optional.of(Status.PUBLIC);
    return d;
  }

  private static EventSelector at(String subject, LocalDate date, int h, int m) {
    EventSelector s = new EventSelector();
    s.subject = subject;
    s.start = date.atTime(h, m);
    return s;
  }

  /**
   * FOLLOWING edit that changes start time splits the series at the anchor; a subsequent
   * ENTIRE_SERIES rename impacts only the original pre-split subset.
   */
  @Test
  public void followingEdit_changesStart_splitsSeriesFromAnchor() {
    CalendarApi cal = new InMemoryCalendar();

    cal.createSeries(
        weeklyMonWed("First", LocalDate.of(2025, 5, 5),
            LocalTime.of(10, 0), LocalTime.of(11, 0), 4));

    EventPatch p = new EventPatch();
    p.start = Optional.of(LocalDateTime.of(2025, 5, 12, 10, 30));
    p.end = Optional.of(LocalDateTime.of(2025, 5, 12, 11, 30));
    cal.updateBySelector(at("First", LocalDate.of(2025, 5, 12), 10, 0), p, EditScope.FOLLOWING);

    EventPatch rename = new EventPatch();
    rename.subject = Optional.of("Fourth");
    cal.updateBySelector(at("First", LocalDate.of(2025, 5, 5), 10, 0), rename,
        EditScope.ENTIRE_SERIES);

    List<Event> all = cal.allEvents();
    long fourth = all.stream().filter(e -> e.subject().equals("Fourth")).count();
    long first = all.stream().filter(e -> e.subject().equals("First")).count();
    assertEquals(2, fourth);
    assertEquals(2, first);
  }
}

