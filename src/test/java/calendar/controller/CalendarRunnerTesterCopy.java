package calendar.controller;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the copy of calendars.
 */
public class CalendarRunnerTesterCopy extends CalendarRunnerTester {
  /**
   * Tests valid event copy.
   * Also tests that days in different timezones that are ahead/behind a day works.
   */
  @Test
  public void copyCalendarTests() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
        + "use calendar --name school\n"
        + "create event \"Team Meeting\" from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "create calendar --name hobbies --timezone Europe/Paris\n"
        + "copy event \"Team Meeting\" on 2025-05-05T10:00 --target hobbies to 2025-05-05T11:00\n"
        + "print events on 2025-05-05\n"
        + "use calendar --name hobbies\n"
        + "create event \"French Prev Game\" from 2025-05-05T00:00 to 2025-05-05T01:00\n"
        +
        "copy event \"French Prev Game\" on 2025-05-05T00:00 --target school to 2025-05-05T01:00\n"
        + "use calendar --name school\n"
        + "create event \"American Next Game\" from 2025-05-05T22:00 to 2025-05-05T23:00\n"
        +
        "copy event \"American Next Game\" on 2025-05-05T22:00 --target "
        + "hobbies to 2025-05-05T23:00\n"
        + "print events on 2025-05-05\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains(
        "Welcome to Calendar. Type 'exit' to quit.\n"
            + "Enter a command: Successfully created calendar: school\n"
            + "Enter a command: Successfully switched calendar to school\n"
            + "Enter a command: Event created successfully.\n"
            + "Enter a command: Successfully created calendar: hobbies\n"
            + "Enter a command: Event Team Meeting successfully copied from school to hobbies\n"
            + "Enter a command: Events for calendar: school\n"
            + "Events on 2025-05-05:\n"
            + "- Team Meeting from 10:00 to 11:00\n"
            + "\n"
            + "Events for calendar: hobbies\n"
            + "Events on 2025-05-05:\n"
            + "- Team Meeting from 11:00 to 12:00\n"
            + "\n"
            + "Enter a command: Successfully switched calendar to hobbies\n"
            + "Enter a command: Event created successfully.\n"
            + "Enter a command: Event French Prev Game successfully copied from hobbies to school\n"
            + "Enter a command: Successfully switched calendar to school\n"
            + "Enter a command: Event created successfully.\n"
            + "Enter a command: Event American Next "
            + "Game successfully copied from school to hobbies\n"
            + "Enter a command: Events for calendar: school\n"
            + "Events on 2025-05-05:\n"
            + "- French Prev Game from 01:00 to 02:00\n"
            + "- Team Meeting from 10:00 to 11:00\n"
            + "- American Next Game from 22:00 to 23:00\n"
            + "\n"
            + "Events for calendar: hobbies\n"
            + "Events on 2025-05-05:\n"
            + "- French Prev Game from 00:00 to 01:00\n"
            + "- Team Meeting from 11:00 to 12:00\n"
            + "- American Next Game from 23:00 to 00:00"
    ));

  }

  /**
   * Checks next/prev day copy works.
   */
  @Test
  public void copyNextPrevTest() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
        + "use calendar --name school\n"
        + "create event \"Early EST\" from 2025-05-05T00:30 to 2025-05-05T01:30\n"
        + "create calendar --name hobbies --timezone America/Los_Angeles\n"
        + "copy events on 2025-05-05 --target hobbies to 2025-05-05\n"
        + "use calendar --name hobbies\n"
        + "print events on 2025-05-04\n"
        + "create event \"Late LA\" from 2025-05-05T23:30 to 2025-05-05T23:45\n"
        + "copy events on 2025-05-05 --target school to 2025-05-05\n"
        + "use calendar --name school\n"
        + "print events on 2025-05-06\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Events on 2025-05-04:\n"
        + "- Early EST from 21:30 to 22:30"));

    assertTrue(result.contains("Events on 2025-05-06:\n"
        +
        "- Late LA from 02:30 to 02:45"));
  }

  /**
   * Testing coping intervals.
   */
  @Test
  public void copyEventsBetweenEdgeAndOverlapTest() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
            + "use calendar --name school\n"
            + "create event \"Start Edge\" from 2025-05-05T00:00 to 2025-05-05T01:00\n"
            + "create event Overlap from 2025-05-04T23:30 to 2025-05-05T00:30\n"
            + "create event \"End Edge\" from 2025-05-05T23:00 to 2025-05-05T23:59\n"
            + "create calendar --name hobbies --timezone America/Los_Angeles\n"
            + "copy events between 2025-05-05 and 2025-05-05 --target hobbies to 2025-05-05\n"
            + "use calendar --name hobbies\n"
            + "print events on 2025-05-05\n"
            + "print events on 2025-05-04\n"
            + "exit\n";

    String result = runWithInput(input);

    assertTrue(result.contains("Events for calendar: hobbies\n"
        +
        "Events on 2025-05-04:\n"
        +
        "- Overlap from 20:30 to 21:30\n"
        +
        "- Start Edge from 21:00 to 22:00"));
    assertTrue(result.contains("Events for calendar: hobbies\n"
        +
        "Events on 2025-05-05:\n"
        +
        "- End Edge from 20:00 to 20:59"));
  }

  /**
   * Tests invalid event copy. (Invalid name or time).
   */
  @Test
  public void copyCalendarInvalidTests() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
        + "use calendar --name school\n"
        + "create event \"Team Meeting\" from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "create calendar --name hobbies --timezone Europe/Paris\n"
        +
        "copy event \"Sprint Planning\" on 2025-05-05T10:00 --target hobbies to 2025-05-05T11:00\n"
        + "copy event \"Team Meeting\" on 2025-05-06T10:00 --target hobbies to 2025-05-06T11:00\n"
        + "print events on 2025-05-05\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Welcome to Calendar. Type 'exit' to quit.\n"
        + "Enter a command: Successfully created calendar: school\n"
        + "Enter a command: Successfully switched calendar to school\n"
        + "Enter a command: Event created successfully.\n"
        + "Enter a command: Successfully created calendar: hobbies\n"
        + "Enter a command: Event 'Sprint Planning' not found at 2025-05-05T10:00\n"
        + "Enter a command: Event 'Team Meeting' not found at 2025-05-06T10:00\n"
        + "Enter a command: Events for calendar: school\n"
        + "Events on 2025-05-05:\n"
        + "- Team Meeting from 10:00 to 11:00\n"
        + "\n"
        + "Events for calendar: hobbies\n"
        + "Events on 2025-05-05:"));
  }

  /**
   * Verifies copying an event inside the same calendar places it on the new date as expected.
   */
  @Test
  public void copyEventWithinSameCalendarCreatesNewEntry() throws Exception {
    String input = "create calendar --name cal --timezone America/New_York\n"
        + "use calendar --name cal\n"
        + "create event \"Event 1\" from 2025-05-05T00:00 to 2025-05-05T01:00\n"
        + "create event \"Event 2\" from 2025-06-06T02:00 to 2025-06-06T03:00\n"
        + "copy event \"Event 1\" on 2025-05-05T00:00 --target cal to 2025-06-06T00:00\n"
        + "print events on 2025-06-06\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Events for calendar: cal\n"
        + "Events on 2025-06-06:\n"
        + "- Event 1 from 00:00 to 01:00\n"
        + "- Event 2 from 02:00 to 03:00"));
  }

  /**
   * Ensures a copied series retains its identity so an ENTIRE_SERIES edit affects every copy.
   */
  @Test
  public void copySeriesRetainsIdentityAcrossCalendars() throws Exception {
    String input = "create calendar --name source --timezone America/New_York\n"
        + "create calendar --name dest --timezone America/New_York\n"
        + "use calendar --name source\n"
        + "create event Standup from 2025-05-05T09:00 to 2025-05-05T10:00 repeats MW for 3 times\n"
        + "copy events between 2025-05-05 and 2025-05-12 --target dest to 2025-06-02\n"
        + "use calendar --name dest\n"
        + "print events on 2025-06-02\n"
        + "print events on 2025-06-04\n"
        + "edit series subject Standup from 2025-06-02T09:00 with \"Standup Copied\"\n"
        + "print events on 2025-06-02\n"
        + "print events on 2025-06-04\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Events for calendar: dest\n"
        + "Events on 2025-06-02:\n"
        + "- Standup from 09:00 to 10:00"));
    assertTrue(result.contains("Events for calendar: dest\n"
        + "Events on 2025-06-04:\n"
        + "- Standup from 09:00 to 10:00"));
    assertTrue(result.contains("Events for calendar: dest\n"
        + "Events on 2025-06-02:\n"
        + "- Standup Copied from 09:00 to 10:00"));
    assertTrue(result.contains("Events for calendar: dest\n"
        + "Events on 2025-06-04:\n"
        + "- Standup Copied from 09:00 to 10:00"));
  }

  /**
   * Tests on event copy.
   */
  @Test
  public void copyCalendarOnTests() throws Exception {
    String input = "create calendar --name est_school --timezone America/New_York\n"
        + "create calendar --name est_school2 --timezone America/New_York\n"
        + "create calendar --name pst_school --timezone America/Los_Angeles\n"
        + "use calendar --name est_school\n"
        + "create event \"est meeting\" from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "create event \"est meeting2\" from 2025-05-05T12:00 to 2025-05-05T13:00\n"
        + "copy events on 2025-05-05 --target est_school2 to 2025-05-05\n"
        + "copy events on 2025-05-05 --target pst_school to 2025-05-05\n"
        + "print events on 2025-05-05\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Welcome to Calendar. Type 'exit' to quit.\n"
        + "Enter a command: Successfully created calendar: est_school\n"
        + "Enter a command: Successfully created calendar: est_school2\n"
        + "Enter a command: Successfully created calendar: pst_school\n"
        + "Enter a command: Successfully switched calendar to est_school\n"
        + "Enter a command: Event created successfully.\n"
        + "Enter a command: Event created successfully.\n"
        + "Enter a command: Event  successfully copied from est_school to est_school2\n"
        + "Enter a command: Event  successfully copied from est_school to pst_school\n"
        + "Enter a command: Events for calendar: est_school2\n"
        + "Events on 2025-05-05:\n"
        + "- est meeting from 10:00 to 11:00\n"
        + "- est meeting2 from 12:00 to 13:00\n"
        + "\n"
        + "Events for calendar: pst_school\n"
        + "Events on 2025-05-05:\n"
        + "- est meeting from 07:00 to 08:00\n"
        + "- est meeting2 from 09:00 to 10:00\n"
        + "\n"
        + "Events for calendar: est_school\n"
        + "Events on 2025-05-05:\n"
        + "- est meeting from 10:00 to 11:00\n"
        + "- est meeting2 from 12:00 to 13:00"));
  }

  /**
   * Tests on event copy with different days.
   */
  @Test
  public void copyCalendarOnTests2() throws Exception {
    String input = "create calendar --name est_school --timezone America/New_York\n"
        + "create calendar --name pst_school --timezone America/Los_Angeles\n"
        + "use calendar --name est_school\n"
        + "create event \"est meeting\" from 2025-05-05T00:00 to 2025-05-05T01:00\n"
        + "copy events on 2025-05-05 --target pst_school to 2025-05-05\n"
        + "use calendar --name pst_school\n"
        + "create event \"pst meeting\" from 2025-05-04T22:00 to 2025-05-04T23:30\n"
        + "copy event \"pst meeting\" on 2025-05-04T22:00 --target est_school to 2025-05-04T23:30\n"
        + "print events on 2025-05-04\n"
        + "print events on 2025-05-05\n"
        + "print events on 2025-05-06\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains(
        "Welcome to Calendar. Type 'exit' to quit.\n"
            + "Enter a command: Successfully created calendar: est_school\n"
            + "Enter a command: Successfully created calendar: pst_school\n"
            + "Enter a command: Successfully switched calendar to est_school\n"
            + "Enter a command: Event created successfully.\n"
            + "Enter a command: Event  successfully copied from est_school to pst_school\n"
            + "Enter a command: Successfully switched calendar to pst_school\n"
            + "Enter a command: Event created successfully.\n"
            + "Enter a command: Event pst meeting successfully"
            + " copied from pst_school to est_school\n"
            + "Enter a command: Events for calendar: pst_school\n"
            + "Events on 2025-05-04:\n"
            + "- est meeting from 21:00 to 22:00\n"
            + "- pst meeting from 22:00 to 23:30\n"
            + "\n"
            + "Events for calendar: est_school\n"
            + "Events on 2025-05-04:\n"
            + "- pst meeting from 23:30 to 01:00\n"
            + "\n"
            + "Enter a command: Events for calendar: pst_school\n"
            + "Events on 2025-05-05:\n"
            + "\n"
            + "Events for calendar: est_school\n"
            + "Events on 2025-05-05:\n"
            + "- pst meeting from 23:30 to 01:00\n"
            + "- est meeting from 00:00 to 01:00\n"
            + "\n"
            + "Enter a command: Events for calendar: pst_school\n"
            + "Events on 2025-05-06:\n"
            + "\n"
            + "Events for calendar: est_school\n"
            + "Events on 2025-05-06:\n"
            + "\n"
            + "Enter a command: "
    ));

  }

  /**
   * Tests on copy interval.
   */
  @Test
  public void copyCalendarIntervalTests() throws Exception {
    String input = "create calendar --name est_school --timezone America/New_York\n"
        + "create calendar --name pst_school --timezone America/Los_Angeles\n"
        + "use calendar --name est_school\n"
        + "create event \"est meeting\" from 2025-05-05T00:00 to 2025-05-05T01:00\n"
        + "create event \"est meeting2\" from 2025-05-04T10:00 to 2025-05-04T11:00\n"
        + "copy events between 2025-05-05 and 2025-05-06 --target pst_school to 2025-05-05\n"
        + "print events on 2025-05-05\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Event  successfully copied from est_school to pst_school"));
  }

  /**
   * Invalid copy tests for dupe events.
   */
  @Test
  public void copyCalendarEventDupeTests() throws Exception {
    String input = "create calendar --name est_school --timezone America/New_York\n"
        + "create calendar --name pst_school --timezone America/Los_Angeles\n"
        + "use calendar --name est_school\n"
        + "create event \"est meeting\" from 2025-05-05T00:00 to 2025-05-05T01:00\n"
        + "use calendar --name pst_school\n"
        + "create event \"est meeting\" from 2025-05-05T00:00 to 2025-05-05T01:00\n"
        + "copy events between 2025-05-05 and 2025-05-06 --target pst_school to 2025-05-05\n"
        + "copy event \"est meeting\" on 2025-05-05T00:00 --target pst_school to 2025-05-05T00:00\n"
        + "copy events on 2025-05-05 --target pst_school to 2025-05-05\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains(
        "Welcome to Calendar. Type 'exit' to quit.\n"
            + "Enter a command: Successfully created calendar: est_school\n"
            + "Enter a command: Successfully created calendar: pst_school\n"
            + "Enter a command: Successfully switched calendar to est_school\n"
            + "Enter a command: Event created successfully.\n"
            + "Enter a command: Successfully switched calendar to pst_school\n"
            + "Enter a command: Event created successfully.\n"
            + "Enter a command: Cannot copy event 'est meeting': "
            +
            "Duplicate event (subject/start/end) exists\n"
            + "Enter a command: Cannot copy event 'est meeting': "
            +
            "Duplicate event (subject/start/end) exists\n"
            + "Enter a command: Cannot copy event 'est meeting':"
            +
            " Duplicate event (subject/start/end) exists"
    ));

  }
}
