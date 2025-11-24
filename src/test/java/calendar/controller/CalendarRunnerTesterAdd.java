package calendar.controller;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the printing of calendars.
 */
public class CalendarRunnerTesterAdd extends CalendarRunnerTester {
  /**
   * Tests we can create multiple calendars.
   */
  @Test
  public void multipleCalendarTests() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
        + "create calendar --name work --timezone Asia/Kolkata\n"
        + "exit\n";
    String result = runWithInput(input);

    assertTrue(result.contains("Welcome to Calendar. Type 'exit' to quit."));
    assertTrue(result.contains("Enter a command: Successfully created calendar: school"));
    assertTrue(result.contains("Enter a command: Successfully created calendar: work"));
  }

  /**
   * Tests we can't have invalid names or timezones.
   */
  @Test
  public void invalidCalendarAddTest() throws Exception {
    String input = "create calendar --name school --timezone America/New_York\n"
        + "create calendar --name school --timezone Asia/Kolkata\n"
        + "create calendar --name school2 --timezone Fake/Kolkata\n"
        + "exit\n";
    String result = runWithInput(input);
    assertTrue(result.contains("Welcome to Calendar. Type 'exit' to quit."));
    assertTrue(result.contains("Enter a command: Successfully created calendar: school"));
    assertTrue(result.contains("Enter a command: Calendar with name 'school' already exists"));
    assertTrue(result.contains("Enter a command: Unsupported timezone: Fake/Kolkata"));
  }
}
