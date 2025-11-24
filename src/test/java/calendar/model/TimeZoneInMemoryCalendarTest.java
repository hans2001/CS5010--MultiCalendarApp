package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Test;

/**
 * Tests for {@link TimeZoneInMemoryCalendar}.
 */
public final class TimeZoneInMemoryCalendarTest {

  @Test
  public void constructorRejectsNullTimezone() {
    assertThrows(IllegalArgumentException.class,
        () -> new TimeZoneInMemoryCalendar(null, "Work"));
  }

  @Test
  public void constructorRejectsBlankName() {
    assertThrows(IllegalArgumentException.class,
        () -> new TimeZoneInMemoryCalendar("America/New_York", "   "));
  }

  @Test
  public void constructorRejectsUnsupportedTimezone() {
    assertThrows(IllegalArgumentException.class,
        () -> new TimeZoneInMemoryCalendar("Invalid/Zone", "Work"));
  }

  @Test
  public void setNameTrimsAndStoresValue() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    calendar.setName("  Personal  ");
    assertEquals("Personal", calendar.getName());
  }

  @Test
  public void setZoneIdObjectRejectsNull() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    assertThrows(IllegalArgumentException.class, () -> calendar.setZoneId((ZoneId) null));
  }

  @Test
  public void convertToLocalDateTimeConvertsAcrossZones() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    LocalDateTime noonEastern = LocalDateTime.of(2025, 5, 5, 12, 0);
    LocalDateTime converted = calendar.convertToLocalDateTime(
        noonEastern, ZoneId.of("America/New_York"), ZoneId.of("America/Los_Angeles"));
    assertEquals(LocalDateTime.of(2025, 5, 5, 9, 0), converted);
  }

  @Test
  public void setZoneId_convertsExistingEvents() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    EventDraft draft = new EventDraft();
    draft.subject = "Meeting";
    LocalDateTime originalStart = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime originalEnd = originalStart.plusHours(1);
    draft.start = Optional.of(originalStart);
    draft.end = Optional.of(originalEnd);
    calendar.create(draft);

    ZoneId targetZone = ZoneId.of("America/Los_Angeles");
    LocalDateTime expectedStart = calendar.convertToLocalDateTime(
        originalStart, calendar.getZoneId(), targetZone);
    LocalDateTime expectedEnd = calendar.convertToLocalDateTime(
        originalEnd, calendar.getZoneId(), targetZone);

    calendar.setZoneId(targetZone);

    Event event = calendar.allEvents().get(0);
    assertEquals(expectedStart, event.start());
    assertEquals(expectedEnd, event.end());
  }

  @Test
  public void createDelegatesToInMemoryCalendar() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    EventDraft draft = new EventDraft();
    draft.subject = "Task";
    draft.start = Optional.of(LocalDateTime.of(2025, 6, 1, 10, 0));
    draft.end = Optional.of(LocalDateTime.of(2025, 6, 1, 11, 0));

    assertNotNull(calendar.create(draft));
    assertEquals(1, calendar.allEvents().size());
  }

  @Test
  public void createSeriesDelegatesToInMemoryCalendar() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    SeriesDraft draft = new SeriesDraft();
    draft.subject = "Series";
    draft.startDate = LocalDate.of(2025, 7, 1);
    draft.startTime = Optional.of(LocalTime.of(9, 0));
    draft.endTime = Optional.of(LocalTime.of(10, 0));
    draft.rule = new RecurrenceRule(EnumSet.of(Weekday.M), Optional.of(2), Optional.empty());

    assertNotNull(calendar.createSeries(draft));
    assertEquals(2, calendar.allEvents().size());
  }

  @Test
  public void updateBySelectorDelegatesToInMemoryCalendar() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    EventDraft draft = new EventDraft();
    draft.subject = "RenameMe";
    draft.start = Optional.of(LocalDateTime.of(2025, 6, 2, 9, 0));
    draft.end = Optional.of(LocalDateTime.of(2025, 6, 2, 10, 0));
    calendar.create(draft);

    EventSelector selector = new EventSelector();
    selector.subject = "RenameMe";
    selector.start = draft.start.get();

    EventPatch patch = new EventPatch();
    patch.subject = Optional.of("Renamed");

    calendar.updateBySelector(selector, patch, EditScope.SINGLE);

    assertEquals("Renamed", calendar.allEvents().get(0).subject());
  }

  @Test
  public void statusAtUsesDelegate() {
    final TimeZoneInMemoryCalendar calendar =
        new TimeZoneInMemoryCalendar("America/New_York", "Work");
    EventDraft draft = new EventDraft();
    draft.subject = "Busy";
    draft.start = Optional.of(LocalDateTime.of(2025, 8, 1, 9, 0));
    draft.end = Optional.of(LocalDateTime.of(2025, 8, 1, 10, 0));
    calendar.create(draft);

    assertEquals(BusyStatus.BUSY, calendar.statusAt(LocalDateTime.of(2025, 8, 1, 9, 30)));
    assertEquals(BusyStatus.AVAILABLE, calendar.statusAt(LocalDateTime.of(2025, 8, 1, 11, 0)));
  }

}
