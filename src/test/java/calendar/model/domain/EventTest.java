package calendar.model.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

/**
 * Tests for Event value semantics.
 */
public final class EventTest {
  private static LocalDateTime localDate(int h, int m) {
    return LocalDateTime.of(2025, 1, 1, h, m);
  }

  @Test
  public void builder_rejectsBlankSubject() {
    Event.Builder b = new Event.Builder()
        .subject("  ")
        .start(localDate(10, 0))
        .end(localDate(10, 1));
    assertThrows(IllegalArgumentException.class, b::build);
  }

  @Test
  public void builder_rejectsEndNotAfterStart() {
    Event.Builder eq = new Event.Builder()
        .subject("X").start(localDate(10, 0)).end(localDate(10, 0));
    Event.Builder before = new Event.Builder()
        .subject("X").start(localDate(10, 1)).end(localDate(10, 0));

    assertThrows(IllegalArgumentException.class, eq::build);
    assertThrows(IllegalArgumentException.class, before::build);
  }

  @Test
  public void equalityAndHashCodeBasedOnId() {
    EventId shared = new EventId(UUID.randomUUID());

    Event e1 = new Event.Builder()
        .id(shared)
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 1, 1, 9, 0))
        .end(LocalDateTime.of(2025, 1, 1, 10, 0))
        .build();

    Event e2 = new Event.Builder()
        .id(shared)
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 1, 1, 9, 0))
        .end(LocalDateTime.of(2025, 1, 1, 10, 0))
        .build();

    Event e3 = new Event.Builder()
        .id(new EventId(UUID.randomUUID()))
        .subject("Different")
        .start(LocalDateTime.of(2025, 1, 1, 11, 0))
        .end(LocalDateTime.of(2025, 1, 1, 12, 0))
        .build();

    assertNotEquals(e1, e3);
    assertEquals(shared, e1.id());
    assertEquals(shared, e2.id());
    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
    assertEquals(e1, e1);
    assertNotEquals(e1, null);
    assertNotEquals(e1, "not-an-event");
  }

  @Test
  public void descriptionAndLocation_presentReturnOptionalOf() {
    Event e = new Event.Builder()
        .subject("With meta")
        .start(localDate(9, 0))
        .end(localDate(10, 0))
        .description("desc")
        .location("room-1")
        .build();

    assertEquals(Optional.of("desc"), e.description());
    assertEquals(Optional.of("room-1"), e.location());
  }

  @Test
  public void descriptionAndLocation_absentOrEmptyReturnOptionalEmpty() {
    Event a = new Event.Builder()
        .subject("No meta")
        .start(localDate(10, 0))
        .end(localDate(11, 0))
        .build();
    assertTrue(a.description().isEmpty());
    assertTrue(a.location().isEmpty());

    Event b = new Event.Builder()
        .subject("Empty meta")
        .start(localDate(12, 0))
        .end(localDate(13, 0))
        .description("")
        .location("")
        .build();
    assertTrue(b.description().isEmpty());
    assertTrue(b.location().isEmpty());
  }

  @Test
  public void status_defaultIsPublic_andExplicitPrivateWorks() {
    Event def = new Event.Builder()
        .subject("S")
        .start(localDate(9, 0))
        .end(localDate(10, 0))
        .build();
    assertEquals(Status.PUBLIC, def.status());

    Event pri = new Event.Builder()
        .subject("P")
        .start(localDate(11, 0))
        .end(localDate(12, 0))
        .status(Status.PRIVATE)
        .build();
    assertEquals(Status.PRIVATE, pri.status());
  }

  @Test
  public void hashCode_differsForDifferentIds() {
    Event e1 = new Event.Builder()
        .id(new EventId(UUID.randomUUID()))
        .subject("A").start(localDate(9, 0)).end(localDate(10, 0))
        .build();

    Event e2 = new Event.Builder()
        .id(new EventId(UUID.randomUUID()))
        .subject("B").start(localDate(11, 0)).end(localDate(12, 0))
        .build();

    assertNotEquals(e1, e2);
    assertNotEquals(e1.hashCode(), e2.hashCode());
  }
}
