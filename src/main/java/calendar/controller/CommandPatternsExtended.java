package calendar.controller;

import static calendar.controller.CommandPatterns.DATE_ONLY;
import static calendar.controller.CommandPatterns.DATE_TIME;
import static calendar.controller.CommandPatterns.SUBJECT;

/**
 * Regex Patterns for new commands.
 */
public class CommandPatternsExtended {
  private static final String TIMEZONE_SEGMENT = "[A-Za-z0-9_+\\-]+";
  private static final String TIMEZONE =
      "(" + TIMEZONE_SEGMENT + "(?:/" + TIMEZONE_SEGMENT + ")*)";

  public static final String CREATE_CALENDAR =
      "^create calendar --name (\\S+) --timezone " + TIMEZONE + "$";

  public static final String EDIT_CALENDAR_NAME =
      "^edit calendar --name (\\S+) --property name (\\S+)$";

  public static final String EDIT_CALENDAR_TIMEZONE =
      "^edit calendar --name (\\S+) --property timezone " + TIMEZONE + "$";

  public static final String USE_CALENDAR =
      "^use calendar --name (\\S+)$";

  public static final String COPY_EVENT =
      "^copy event " + SUBJECT + " on " + DATE_TIME + " --target (\\S+) to " + DATE_TIME + "$";

  public static final String COPY_EVENTS_ON =
      "^copy events on " + DATE_ONLY + " --target (\\S+) to " + DATE_ONLY + "$";

  public static final String COPY_EVENTS_BETWEEN =
      "^copy events between " + DATE_ONLY + " and " + DATE_ONLY + " --target (\\S+) to "
          + DATE_ONLY + "$";
}
