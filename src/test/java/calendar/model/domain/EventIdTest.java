package calendar.model.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;

/**
 * Tests for EventId value semantics.
 */
public final class EventIdTest {
  @Test
  public void ctor_rejectsNull() {
    assertThrows(NullPointerException.class, () -> new EventId(null));
  }

  @Test
  public void value_getterReturnsUnderlyingUuid() {
    UUID u = UUID.randomUUID();
    EventId id = new EventId(u);
    assertEquals(u, id.value());
  }

  @Test
  public void equals_hashCode_toString_coverAllBranches() {
    UUID u = UUID.randomUUID();
    EventId a = new EventId(u);
    EventId b = new EventId(UUID.fromString(u.toString()));

    assertEquals(a, a);
    assertNotEquals(a, "not-an-event-id");
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    EventId c = new EventId(UUID.randomUUID());
    assertNotEquals(a, c);
    assertNotEquals(a, null);

    assertTrue(a.toString().contains("EventId["));
    assertTrue(a.toString().contains(u.toString()));
  }

  @Test
  public void hashCode_reflectsUnderlyingUuidAndDiffersForDifferentIds() {
    UUID u1 = UUID.randomUUID();
    UUID u2 = UUID.randomUUID();

    EventId a = new EventId(u1);
    EventId b = new EventId(u2);

    assertEquals(u1.hashCode(), a.hashCode());
    assertNotEquals(a.hashCode(), b.hashCode());
  }
}
