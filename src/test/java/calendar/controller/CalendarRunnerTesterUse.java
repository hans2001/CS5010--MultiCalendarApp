package calendar.controller;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the printing of calendars.
 */
public class CalendarRunnerTesterUse extends CalendarRunnerTester {
  /**
   * Tests commands don't work without selected calendar.
   */
  @Test
  public void noUseTest() throws Exception {
    String input = "create event \"Team Meeting\" from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "edit event subject \"Team Meeting\" from 2025-05-05T10:00 to "
        + "2025-05-05T11:00 with \"Sprint Planning\"\n"
        + "print events on 2025-05-05\n"
        + "show status on 2025-05-05T10:30\n"
        + "use calendar --name school\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Enter a command: Error: No calendar selected.\n"
        + "Enter a command: Error: No calendar selected.\n"
        + "Enter a command: Error: No calendar selected.\n"
        + "Enter a command: Error: No calendar selected.\n"
        + "Enter a command: Calendar not found.\n"
    ));
  }

  /**
   * Verifies that CLI commands operate correctly after selecting a calendar.
   * Executes a sequence of commands that create and select a calendar, create and edit an event,
   * print events for a date, and check status; asserts that the output contains the expected
   * welcome message, creation/selection confirmations, event creation/update confirmations,
   * the event listing for the selected calendar, and a BUSY status at the queried time.
   */
  @Test
  public void useTest() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
        + "use calendar --name school\n"
        + "create event \"Team Meeting\" from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "edit event subject \"Team Meeting\" from 2025-05-05T10:00 to "
        + "2025-05-05T11:00 with \"Sprint Planning\"\n"
        + "print events on 2025-05-05\n"
        + "show status on 2025-05-05T10:30\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Welcome to Calendar. Type 'exit' to quit.\n"
        + "Enter a command: Successfully created calendar: school\n"
        +  "Enter a command: Successfully switched calendar to school\n"
        + "Enter a command: Event created successfully.\n"
        +  "Enter a command: Event updated successfully.\n"
        +  "Enter a command: Events for calendar: school\n"
        +  "Events on 2025-05-05:\n"
        +  "- Sprint Planning from 10:00 to 11:00\n"
        +  "\n"
        +  "Enter a command: User is BUSY"
    ));
  }
}