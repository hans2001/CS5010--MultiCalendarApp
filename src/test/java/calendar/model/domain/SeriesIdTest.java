package calendar.model.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;

/**
 * Basic tests for SeriesId value semantics.
 */
public final class SeriesIdTest {

  /**
   * Verifies value equality, hashCode contract, and non-empty toString.
   */
  @Test
  public void equals_hash_toString_and_value() {
    UUID u = UUID.randomUUID();
    SeriesId a = new SeriesId(u);
    SeriesId b = new SeriesId(u);
    SeriesId c = new SeriesId(UUID.randomUUID());

    assertEquals(u, a.value());
    assertEquals(a, b);
    assertNotEquals(a, c);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a.hashCode(), c.hashCode());
    assertFalse(a.toString().isEmpty());
  }

  /**
   * Verifies equals against self, null, and non-SeriesId types.
   */
  @Test
  public void equals_covers_self_null_and_other_type() {
    UUID u = UUID.randomUUID();
    SeriesId a = new SeriesId(u);

    assertTrue(a.equals(a));
    assertFalse(a.equals(null));
    assertFalse(a.equals(u));
  }
}