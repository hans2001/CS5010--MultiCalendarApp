package calendar.model.internal;

import static org.junit.Assert.assertThrows;

import calendar.model.exception.ConflictException;
import java.time.LocalDateTime;
import org.junit.Test;

/**
 * Tests for UniquenessIndex semantics.
 */
public final class UniquenessIndexTest {

  @Test
  public void add_duplicate_rejected_and_replace_checks() {
    UniquenessIndex idx = new UniquenessIndex();
    LocalDateTime s = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime e = LocalDateTime.of(2025, 1, 1, 11, 0);
    idx.addOrThrow("A", s, e);

    assertThrows(ConflictException.class,
        () -> idx.addOrThrow("a", s, e));

    String oldKey = UniquenessIndex.key("A", s, e);
    String newKey = UniquenessIndex.key("A", s.plusMinutes(1), e.plusMinutes(1));
    idx.replaceOrThrow(oldKey, newKey);

    idx.addOrThrow("B", s, e);
    String collide = UniquenessIndex.key("b", s, e);
    assertThrows(ConflictException.class,
        () -> idx.replaceOrThrow(newKey, collide));
  }
}

