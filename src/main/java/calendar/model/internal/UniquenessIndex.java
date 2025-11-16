package calendar.model.internal;

import calendar.model.exception.ConflictException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Enforces uniqueness of (subject, start, end) triples.
 *
 * <p>Collisions are case-insensitive on subject, exact on times.</p>
 *
 * <h2>Design: String Keys</h2>
 *
 * <p><b>Why concatenate strings instead of a Triple class?</b> Simpler. String already
 * has equals() and hashCode(). HashSet gives O(1) lookups. Creating a custom class just
 * for this index would add code with no benefit.</p>
 *
 * <p><b>Key format:</b> {@code "subject|start|end"} where subject is lowercased and
 * times use ISO-8601 format. Pipe delimiter is unlikely in subjects (unlike quotes/commas).</p>
 */
final class UniquenessIndex {
  private final Set<String> triples = new HashSet<>();

  static String key(String subject, LocalDateTime start, LocalDateTime end) {
    Objects.requireNonNull(subject, "subject");
    return subject.trim().toLowerCase(Locale.ROOT) + "|" + start + "|" + end;
  }

  void addOrThrow(String subject, LocalDateTime start, LocalDateTime end) {
    String k = key(subject, start, end);
    if (triples.contains(k)) {
      throw new ConflictException("Duplicate event (subject/start/end) exists");
    }
    triples.add(k);
  }

  void replaceOrThrow(String oldKey, String newKey) {
    if (!oldKey.equals(newKey) && triples.contains(newKey)) {
      throw new ConflictException("Update would duplicate an existing event");
    }
    triples.remove(oldKey);
    triples.add(newKey);
  }
}
