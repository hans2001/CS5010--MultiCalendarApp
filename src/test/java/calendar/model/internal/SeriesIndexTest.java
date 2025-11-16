package calendar.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.model.domain.Event;
import calendar.model.domain.EventId;
import calendar.model.domain.Status;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

/**
 * Tests for SeriesIndex detach and splitFollowing semantics.
 */
public final class SeriesIndexTest {
  private static Event ev(LocalDateTime s, LocalDateTime e) {
    return new Event.Builder()
        .subject("S")
        .start(s)
        .end(e)
        .status(Status.PUBLIC)
        .build();
  }

  /**
   * Detach removes an event from its series and prunes empty series mappings.
   */
  @Test
  public void detach_removesFromSeries_andCleansEmptySeries() {
    SeriesIndex index = new SeriesIndex();

    List<EventId> ids = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      Event e =
          ev(LocalDateTime.of(2025, 5, 5, 10 + i, 0), LocalDateTime.of(2025, 5, 5, 11 + i, 0));
      ids.add(e.id());
    }

    UUID sid = index.registerSeries(ids);
    assertEquals(2, index.all(sid).size());

    index.detach(ids.get(0));
    assertEquals(1, index.all(sid).size());

    index.detach(ids.get(1));
    assertTrue(index.all(sid).isEmpty());
  }

  /**
   * splitFollowing moves instances at/after cutoff into a new series.
   */
  @Test
  public void splitFollowing_movesEventsAtOrAfterCutoffToNewSeries() {
    SeriesIndex index = new SeriesIndex();
    Map<EventId, Event> byId = new HashMap<>();

    List<EventId> ids = new ArrayList<>();
    LocalDateTime base = LocalDateTime.of(2025, 5, 5, 10, 0);
    for (int i = 0; i < 3; i++) {
      Event e = ev(base.plusDays(i), base.plusDays(i).plusHours(1));
      byId.put(e.id(), e);
      ids.add(e.id());
    }

    UUID sid = index.registerSeries(ids);
    assertEquals(3, index.all(sid).size());

    LocalDateTime cutoff = base.plusDays(1);
    UUID newSid = index.splitFollowing(sid, cutoff, byId);

    assertEquals(1, index.all(sid).size());
    assertEquals(2, index.all(newSid).size());

    assertTrue(index.following(sid, cutoff, byId).isEmpty());
    assertEquals(2, index.following(newSid, cutoff, byId).size());
  }

  /**
   * Detach on unknown event id is a safe no-op (sid == null).
   */
  @Test
  public void detach_onUnknownEvent_isNoOp() {
    SeriesIndex index = new SeriesIndex();
    Event e = ev(LocalDateTime.of(2025, 5, 5, 10, 0), LocalDateTime.of(2025, 5, 5, 11, 0));
    index.detach(e.id());
    org.junit.Assert.assertTrue(index.seriesOf(e.id()).isEmpty());
  }

  /**
   * splitFollowing with cutoff after last event moves nothing (returns original sid).
   */
  @Test
  public void splitFollowing_nothingToMove_returnsOriginalSid() {
    SeriesIndex index = new SeriesIndex();
    Map<EventId, Event> byId = new HashMap<>();

    List<EventId> ids = new ArrayList<>();
    LocalDateTime base = LocalDateTime.of(2025, 5, 5, 10, 0);
    for (int i = 0; i < 2; i++) {
      Event e = ev(base.plusDays(i), base.plusDays(i).plusHours(1));
      byId.put(e.id(), e);
      ids.add(e.id());
    }

    UUID sid = index.registerSeries(ids);
    LocalDateTime cutoff = base.plusDays(3);
    UUID res = index.splitFollowing(sid, cutoff, byId);
    assertEquals(sid, res);
    assertEquals(2, index.all(sid).size());
  }

  /**
   * splitFollowing with cutoff at/before first event moves all and removes original sid.
   */
  @Test
  public void splitFollowing_movesAll_removesOriginalSid() {
    SeriesIndex index = new SeriesIndex();
    Map<EventId, Event> byId = new HashMap<>();

    List<EventId> ids = new ArrayList<>();
    LocalDateTime base = LocalDateTime.of(2025, 5, 5, 10, 0);
    for (int i = 0; i < 2; i++) {
      Event e = ev(base.plusDays(i), base.plusDays(i).plusHours(1));
      byId.put(e.id(), e);
      ids.add(e.id());
    }

    UUID sid = index.registerSeries(ids);
    UUID newSid = index.splitFollowing(sid, base, byId);
    org.junit.Assert.assertNotEquals(sid, newSid);
    assertTrue(index.all(sid).isEmpty());
    assertEquals(2, index.all(newSid).size());
  }

  /**
   * splitFollowing ignores ids missing from byId (e == null treated as keep).
   */
  @Test
  public void splitFollowing_ignoresNullEventsInById() {
    final SeriesIndex index = new SeriesIndex();
    Map<EventId, Event> byId = new HashMap<>();

    List<EventId> ids = new ArrayList<>();
    LocalDateTime base = LocalDateTime.of(2025, 5, 5, 10, 0);
    Event present = ev(base, base.plusHours(1));
    byId.put(present.id(), present);
    ids.add(present.id());
    Event missing = ev(base.plusDays(1), base.plusDays(1).plusHours(1));
    ids.add(missing.id());

    UUID sid = index.registerSeries(ids);

    UUID res = index.splitFollowing(sid, base.plusDays(1), byId);
    assertEquals(sid, res);
    assertEquals(2, index.all(sid).size());
  }

  /**
   * detach is safe when series list is missing (list == null), simulated via reflection.
   */
  @Test
  public void detach_whenSeriesListMissing_isSafeNoOp() throws Exception {
    SeriesIndex index = new SeriesIndex();

    List<EventId> ids = new ArrayList<>();
    Event e = ev(LocalDateTime.of(2025, 5, 5, 10, 0), LocalDateTime.of(2025, 5, 5, 11, 0));
    ids.add(e.id());
    UUID sid = index.registerSeries(ids);

    java.lang.reflect.Field f = SeriesIndex.class.getDeclaredField("seriesToEvents");
    f.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<UUID, List<EventId>> map = (Map<UUID, List<EventId>>) f.get(index);
    map.remove(sid);

    index.detach(e.id());

    assertTrue(index.seriesOf(e.id()).isEmpty());
  }
}

