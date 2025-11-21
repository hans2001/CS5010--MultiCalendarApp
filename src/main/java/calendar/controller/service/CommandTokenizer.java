package calendar.controller.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility responsible for splitting CLI commands into tokens while honoring quoted segments.
 * Centralizing this logic lets both CLI and future GUI adapters reuse the exact same parsing.
 */
public final class CommandTokenizer {
  private static final Pattern TOKEN_PATTERN = Pattern.compile("\"[^\"]+\"|\\S+");

  private CommandTokenizer() {
  }

  /**
   * Splits the provided input into tokens. Quoted values (e.g., "Team Meeting") are kept intact
   * with the surrounding quotes removed.
   *
   * @param input raw command text.
   * @return array of tokens in the original order.
   */
  public static String[] tokenize(String input) {
    List<String> parts = new ArrayList<>();
    Matcher matcher = TOKEN_PATTERN.matcher(input);

    while (matcher.find()) {
      String part = matcher.group();
      if (part.startsWith("\"") && part.endsWith("\"")) {
        part = part.substring(1, part.length() - 1);
      }
      parts.add(part);
    }

    return parts.toArray(new String[0]);
  }
}
