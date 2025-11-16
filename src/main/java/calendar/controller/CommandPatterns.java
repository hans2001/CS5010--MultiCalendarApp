package calendar.controller;

/**
 * The regex patterns for matching with user input for validation.
 */
public class CommandPatterns {
  public static final String DATE_TIME = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}";
  public static final String DATE_ONLY = "\\d{4}-\\d{2}-\\d{2}";
  public static final String SUBJECT = "(\"[^\"]+\"|\\S+)";
  public static final String VALUE = "(\"[^\"]+\"|\\S+)";
  public static final String WEEKDAYS = "[MTWRFSU]+";
  public static final String PROPERTY = "(subject|start|end|location|description|status)";

  // Create commands
  public static final String CREATE_SINGLE =
      "^create event " + SUBJECT + " from " + DATE_TIME + " to " + DATE_TIME + "$";

  public static final String CREATE_REPEAT_N =
      "create event " + SUBJECT + " from " + DATE_TIME + " to " + DATE_TIME
          + " repeats " + WEEKDAYS + " for \\d+ times";

  public static final String CREATE_REPEAT_UNTIL =
      "create event " + SUBJECT + " from " + DATE_TIME + " to " + DATE_TIME
          + " repeats " + WEEKDAYS + " until " + DATE_ONLY;

  public static final String CREATE_ALLDAY =
      "create event " + SUBJECT + " on " + DATE_ONLY;

  public static final String CREATE_ALLDAY_REPEAT_N =
      "create event " + SUBJECT + " on " + DATE_ONLY
          + " repeats " + WEEKDAYS + " for \\d+ times";

  public static final String CREATE_ALLDAY_REPEAT_UNTIL =
      "create event " + SUBJECT + " on " + DATE_ONLY
          + " repeats " + WEEKDAYS + " until " + DATE_ONLY;

  // Edit commands
  public static final String EDIT_SINGLE =
      "^edit event " + PROPERTY + " " + SUBJECT + " from " + DATE_TIME + " to " + DATE_TIME
          + " with " + VALUE + "$";

  public static final String EDIT_EVENTS =
      "^edit events " + PROPERTY + " " + SUBJECT + " from " + DATE_TIME
          + " with " + VALUE + "$";

  public static final String EDIT_SERIES =
      "^edit series " + PROPERTY + " " + SUBJECT + " from " + DATE_TIME
          + " with " + VALUE + "$";

  // Print commands
  public static final String PRINT_ON =
      "^print events on " + DATE_ONLY + "$";

  public static final String PRINT_FROM_TO =
      "^print events from " + DATE_TIME + " to " + DATE_TIME + "$";

  public static final String EXPORT =
      "^export cal \\S+$";

  public static final String SHOW_STATUS_ON =
      "^show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$";

  /**
   * Prevents instantiation.
   */
  private CommandPatterns() {
  }
}
