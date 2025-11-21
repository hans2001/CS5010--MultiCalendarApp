package calendar.controller;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the editing of calendars.
 */
public class CalendarRunnerTesterEdit extends CalendarRunnerTester {
  /**
   * Tests we can edit a calendar.
   */
  @Test
  public void editCalendarTests() throws Exception {
    String input = "use calendar --name school\n"
        + "create calendar --name school --timezone America/New_York\n"
        + "use calendar --name school\n"
        + "edit calendar --name school --property name work\n"
        + "create event \"Team Meeting\" from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "print events on 2025-05-05\n"
        + "edit calendar --name work --property timezone Europe/Paris\n"
        + "print events on 2025-05-05\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Welcome to Calendar. Type 'exit' to quit.\n"
        +
        "Enter a command: Calendar not found.\n"
        +
        "Enter a command: Successfully created calendar: school\n"
        +
        "Enter a command: Successfully switched calendar to school\n"
        +
        "Enter a command: Successfully edited calendar name.\n"
        +
        "Enter a command: Event created successfully.\n"
        +
        "Enter a command: Events for calendar: work\n"
        +
        "Events on 2025-05-05:\n"
        +
        "- Team Meeting from 10:00 to 11:00\n"
        +
        "\n"
        +
        "Enter a command: Successfully edited calendar timezone.\n"
        +
        "Enter a command: Events for calendar: work\n"
        +
        "Events on 2025-05-05:\n"
        +
        "- Team Meeting from 10:00 to 11:00"));
  }

  /**
   * Tests invalid edits. (same name and wrong timezone).
   */
  @Test
  public void editCalendarInvalidTests() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
        + "create calendar --name work --timezone America/New_York\n"
        + "use calendar --name school\n"
        + "edit calendar --name school --property name work\n"
        + "edit calendar --name school --property timezone fake/zone\n"
        + "edit calendar --name school --property fake_property fake/zone\n"
        + "edit calendar --name school --property America/New_York\n"
        + "edit calendar --name school --property name\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Enter a command: Successfully created calendar: school\n"
        + "Enter a command: Successfully created calendar: work\n"
        + "Enter a command: Successfully switched calendar to school\n"
        + "Enter a command: Calendar with name 'work' already exists\n"
        + "Enter a command: Invalid timezone: fake/zone\n"
        + "Enter a command: Invalid edit calendar command format.\n"
        + "Enter a command: Invalid edit calendar command format.\n"
        + "Enter a command: Invalid edit calendar command format.\n"
    ));
  }

}
