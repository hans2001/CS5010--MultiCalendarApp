package calendar.controller.commands;

import static calendar.controller.service.CommandTokenizer.tokenize;

import calendar.controller.CommandPatterns;
import calendar.controller.CommandPatternsExtended;
import calendar.model.CalendarManager;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.domain.Event;
import calendar.model.exception.NotFoundException;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Handles events for controller.
 */
public class HandleEvents {
  /**
   * Creates a new calendar.
   *
   * @param input user input.
   * @param manager for calendars.
   * @param view view.
   *
   * @throws IOException unhandled error.
   */
  public static void handleCreateCalendarEvent(
      String input, CalendarManager manager, CalendarView view
  ) throws IOException {
    if (input.matches(CommandPatternsExtended.CREATE_CALENDAR)) {
      String[] parts = tokenize(input);
      String name = parts[3];
      String timeZone = parts[5];

      try {
        manager.createCalendar(name, timeZone);
        view.printMessage("Successfully created calendar: " + name);
      } catch (Exception e) {
        view.printMessage(e.getMessage());
      }
    } else {
      view.printMessage("Invalid create calendar format");
    }
  }

  /**
   * Edits a calendar.
   *
   * @param input user input.
   * @param manager for calendars.
   * @param view view.
   *
   * @throws IOException unhandled error.
   * @throws NotFoundException calendar not found.
   */
  public static void handleEditCalendarEvent(
      String input, CalendarManager manager, CalendarView view
  ) throws IOException, NotFoundException {
    if (input.matches(CommandPatternsExtended.EDIT_CALENDAR_NAME)) {
      String[] parts = tokenize(input);
      String calendarName = parts[3];
      String newProperty = parts[6];

      try {
        manager.editCalendarName(calendarName, newProperty);
        view.printMessage("Successfully edited calendar name.");
      } catch (Exception e) {
        view.printMessage(e.getMessage());
      }
    } else if (input.matches(CommandPatternsExtended.EDIT_CALENDAR_TIMEZONE)) {
      String[] parts = tokenize(input);
      String calendarName = parts[3];
      String newProperty = parts[6];

      try {
        ZoneId newZone = ZoneId.of(newProperty);
        manager.editCalendarTimezone(calendarName, newZone);
        view.printMessage("Successfully edited calendar timezone.");
      } catch (DateTimeException e) {
        view.printMessage("Invalid timezone: " + newProperty);
      } catch (Exception e) {
        view.printMessage(e.getMessage());
      }
    } else {
      view.printMessage("Invalid edit calendar command format.");
    }
  }

  /**
   * Uses a calendar.
   *
   * @param input user input.
   * @param manager for calendars.
   * @param view view.
   *
   * @throws IOException unhandled error.
   * @throws NotFoundException calendar not found.
   */
  public static TimeZoneInMemoryCalendarInterface handleUseCalendarEvent(
      String input, CalendarManager manager, CalendarView view
  ) throws IOException, NotFoundException {
    if (input.matches(CommandPatternsExtended.USE_CALENDAR)) {
      String[] parts = tokenize(input);
      String name = parts[3];

      try {
        TimeZoneInMemoryCalendarInterface cal = manager.getCalendar(name);
        view.printMessage("Successfully switched calendar to " + cal.getName());

        return cal;
      } catch (NotFoundException e) {
        view.printMessage("Calendar not found.");
        return null;
      }
    } else {
      view.printMessage("Invalid use calendar command format.");
      return null;
    }
  }

  /**
   * Copies events between calendars.
   *
   * @param input user input.
   * @param manager for calendars.
   * @param view view.
   * @param inUseCalendar current calendar.
   *
   * @throws IOException unhandled error.
   * @throws NotFoundException calendar not found.
   */
  public static void handleCopyEvent(
      String input, CalendarManager manager, CalendarView view,
      TimeZoneInMemoryCalendarInterface inUseCalendar
  ) throws IOException, NotFoundException {
    if (inUseCalendar == null) {
      view.printMessage("Error: No calendar selected. Use 'use calendar <name>' to select one.");
      return;
    }

    if (input.matches(CommandPatternsExtended.COPY_EVENT)) {
      String[] parts = tokenize(input);
      String eventName = parts[2];
      String eventFromTime = parts[4];
      String destCalName = parts[6];
      String eventToTime = parts[8];

      try {
        manager.copyEvent(
            inUseCalendar.getName(),
            eventName,
            LocalDateTime.parse(eventFromTime),
            destCalName,
            LocalDateTime.parse(eventToTime)
        );
        view.printMessage("Event " + eventName
            + " successfully copied from "
            + inUseCalendar.getName() + " to "
            + destCalName
        );
      } catch (Exception e) {
        view.printMessage(e.getMessage());
      }
    } else if (input.matches(CommandPatternsExtended.COPY_EVENTS_ON)) {
      String[] parts = tokenize(input);
      String onDate = parts[3];
      String destCalName = parts[5];
      String toDate = parts[7];

      try {
        manager.copyEventsOn(
            inUseCalendar.getName(),
            LocalDate.parse(onDate),
            destCalName,
            LocalDate.parse(toDate)
        );
        view.printMessage("Event "
            + " successfully copied from "
            + inUseCalendar.getName() + " to "
            + destCalName
        );
      } catch (Exception e) {
        view.printMessage(e.getMessage());
      }
    } else if (input.matches(CommandPatternsExtended.COPY_EVENTS_BETWEEN)) {
      String[] parts = tokenize(input);
      String betweenDate1 = parts[3];
      String betweenDate2 = parts[5];
      String destCalName = parts[7];
      String toDate = parts[9];

      try {
        manager.copyEventsBetween(
            inUseCalendar.getName(),
            LocalDate.parse(betweenDate1),
            LocalDate.parse(betweenDate2),
            destCalName,
            LocalDate.parse(toDate)
        );
        view.printMessage("Event "
            + " successfully copied from "
            + inUseCalendar.getName() + " to "
            + destCalName
        );
      } catch (Exception e) {
        view.printMessage(e.getMessage());
      }
    } else {
      view.printMessage("Invalid copy calendar command format.");
    }
  }

  /**
   * Prints events from all calendars either for a specific date or for a
   * date-time range based on the input command.
   * If the input matches the print-on pattern, prints each calendar's events for the
   * specified date. If the input matches the print-from-to pattern, prints events
   * overlapping the specified
   * date-time window for each calendar.
   *
   * @param input   the raw user command to parse for the print operation
   * @param view    the view used to display messages and event lists
   * @param manager the calendar manager that provides access to all calendars
   * @throws IOException if an I/O error occurs while writing output
   */
  public static void handlePrintEvent(
      String input, CalendarView view,
      CalendarManager manager
  ) throws IOException {
    input = input.trim();

    if (input.matches(CommandPatterns.PRINT_ON)) {
      String[] parts = tokenize(input);
      LocalDate onDate = LocalDate.parse(parts[3]);

      Map<String, TimeZoneInMemoryCalendarInterface> allCalendars = manager.getAllCalendars();
      for (Map.Entry<String, TimeZoneInMemoryCalendarInterface> entry : allCalendars.entrySet()) {
        String calendarName = entry.getKey();
        TimeZoneInMemoryCalendarInterface calendar = entry.getValue();

        List<Event> events = calendar.eventsOn(onDate);

        view.printMessage("Events for calendar: " + calendarName);
        view.printEventsOn(onDate, events);
      }
    } else if (input.matches(CommandPatterns.PRINT_FROM_TO)) {
      String[] parts = tokenize(input);
      LocalDateTime onDate = LocalDateTime.parse(parts[3]);
      LocalDateTime toDate = LocalDateTime.parse(parts[5]);

      Map<String, TimeZoneInMemoryCalendarInterface> allCalendars = manager.getAllCalendars();
      for (Map.Entry<String, TimeZoneInMemoryCalendarInterface> entry : allCalendars.entrySet()) {
        String calendarName = entry.getKey();
        TimeZoneInMemoryCalendarInterface calendar = entry.getValue();

        List<Event> events = calendar.eventsOverlapping(onDate, toDate);

        view.printMessage("Events for calendar: " + calendarName);
        view.printEventsFromTo(onDate, toDate, events);
      }
    } else {
      view.printMessage("Error: Invalid print command format.");
    }
  }

}
