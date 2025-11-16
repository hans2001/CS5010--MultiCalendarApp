package calendar.controller;

import static calendar.controller.CommandPatterns.CREATE_ALLDAY;
import static calendar.controller.CommandPatterns.CREATE_ALLDAY_REPEAT_N;
import static calendar.controller.CommandPatterns.CREATE_ALLDAY_REPEAT_UNTIL;
import static calendar.controller.CommandPatterns.CREATE_REPEAT_N;
import static calendar.controller.CommandPatterns.CREATE_REPEAT_UNTIL;
import static calendar.controller.CommandPatterns.CREATE_SINGLE;
import static calendar.controller.CommandPatterns.EDIT_EVENTS;
import static calendar.controller.CommandPatterns.EDIT_SERIES;
import static calendar.controller.CommandPatterns.EDIT_SINGLE;
import static calendar.controller.CommandPatterns.EXPORT;
import static calendar.controller.CommandPatterns.SHOW_STATUS_ON;

import calendar.controller.commands.CommandHandler;
import calendar.controller.commands.HandleEvents;
import calendar.export.CalendarExporter;
import calendar.export.CsvExporter;
import calendar.export.IcalExporter;
import calendar.model.CalendarManager;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.api.EventDraft;
import calendar.model.api.EventPatch;
import calendar.model.api.EventSelector;
import calendar.model.api.SeriesDraft;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.domain.Status;
import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import calendar.model.recurrence.RecurrenceRule;
import calendar.model.recurrence.Weekday;
import calendar.view.CalendarView;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calendar controller for processing user input and interacting with model and view.
 */
public class CalendarControllerImpl implements CalendarController {
  private final Readable input;
  private final Appendable output;
  private final CalendarExporter csvExporter;
  private final CalendarExporter icalExporter;
  private final CalendarManager calendarManager;
  private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
  private TimeZoneInMemoryCalendarInterface inUseCalendar = null;

  /**
   * Creates a new calendar controller.
   *
   * @param input  input from user.
   * @param output output to user.
   */
  public CalendarControllerImpl(Readable input, Appendable output) {
    this(input, output, CalendarSettings.defaults());
  }

  /**
   * Creates a controller that shares {@link CalendarSettings} with the model/exporters.
   */
  public CalendarControllerImpl(Readable input, Appendable output, CalendarSettings settings) {
    this.input = Objects.requireNonNull(input, "input");
    this.output = Objects.requireNonNull(output, "output");
    CalendarSettings sharedSettings = Objects.requireNonNull(settings, "settings");
    this.csvExporter = new CsvExporter(sharedSettings);
    this.icalExporter = new IcalExporter(sharedSettings);
    this.calendarManager = new CalendarManager();
  }

  /**
   * Splits the user command by "" or spaces to try and get keywords and params.
   *
   * @param input user input.
   * @return array of parts for each word in the input.
   */
  private static String[] splitCommands(String input) {
    List<String> parts = new ArrayList<>();
    Matcher matcher = Pattern.compile("\"[^\"]+\"|\\S+").matcher(input);

    while (matcher.find()) {
      String part = matcher.group();
      if (part.startsWith("\"") && part.endsWith("\"")) {
        part = part.substring(1, part.length() - 1);
      }
      parts.add(part);
    }

    return parts.toArray(new String[0]);
  }

  /**
   * Parses weekday letters (e.g., MTWRFSU) into an EnumSet of Weekday.
   *
   * @param weekdays concatenated weekday letters
   * @return corresponding set of weekdays
   */
  private static EnumSet<Weekday> weekdaysSetFromString(String weekdays) {
    EnumSet<Weekday> set = EnumSet.noneOf(Weekday.class);
    for (char c : weekdays.toCharArray()) {
      set.add(Weekday.valueOf(String.valueOf(c).toUpperCase()));
    }
    return set;
  }

  /**
   * Registers the controller's text command handlers into the commandHandlers map.
   *
   * <p>The registered handlers parse and dispatch user command lines for creating, editing,
   * printing, exporting, and managing calendars; handlers that require an active calendar will
   * print an error if no calendar is selected. The "use calendar" handler updates the
   * controller's active calendar when a calendar is successfully selected.
   */
  public void registerCommands() {
    commandHandlers.put("create event", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        handleCreateEvent(line, inUseCalendar, view);
      }
    });

    commandHandlers.put("edit", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        handleEditEvent(line, inUseCalendar, view);
      }
    });

    commandHandlers.put("edit event", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        handleEditEvent(line, inUseCalendar, view);
      }
    });

    commandHandlers.put("print", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        HandleEvents.handlePrintEvent(line, view, calendarManager);
      }
    });

    commandHandlers.put("show status on", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        handleShowStatus(line, inUseCalendar, view);
      }
    });

    commandHandlers.put("export cal", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        handleExport(line, inUseCalendar, view);
      }
    });

    commandHandlers.put("create calendar", (line, view) ->
        HandleEvents.handleCreateCalendarEvent(line, calendarManager, view)
    );

    commandHandlers.put("use calendar", (line, view) -> {
      TimeZoneInMemoryCalendarInterface newCal =
          HandleEvents.handleUseCalendarEvent(line, calendarManager, view);
      if (newCal != null) {
        inUseCalendar = newCal;
      }
    });

    commandHandlers.put("edit calendar", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        HandleEvents.handleEditCalendarEvent(line, calendarManager, view);
      }
    });

    commandHandlers.put("copy event", (line, view) -> {
      if (inUseCalendar == null) {
        view.printMessage("Error: No calendar selected.");
      } else {
        HandleEvents.handleCopyEvent(line, calendarManager, view, inUseCalendar);
      }
    });
  }

  /**
   * Run the controller's interactive command loop, reading lines from the configured input,
   * matching them against registered command handlers, and dispatching matched handlers until
   * the user exits or input is exhausted.
   * The loop prompts for commands, accepts "exit" to terminate, and uses the provided view
   * to render command results.
   *
   * @param view the CalendarView used to display command output and status
   * @throws IOException if writing prompts or messages to the configured output fails
   */
  @Override
  public void go(CalendarView view) throws IOException {
    Objects.requireNonNull(view, "view");

    output.append("Welcome to Calendar. Type 'exit' to quit.");
    output.append(System.lineSeparator());

    registerCommands();

    Scanner scan = new Scanner(this.input);
    while (true) {
      output.append("Enter a command: ");
      if (!scan.hasNextLine()) {
        return;
      }

      String line = scan.nextLine().trim();

      if (line.equals("exit")) {
        return;
      }

      Map.Entry<String, CommandHandler> bestMatch = null;
      for (Map.Entry<String, CommandHandler> entry : commandHandlers.entrySet()) {
        String key = entry.getKey();
        if (line.startsWith(key)) {
          if (bestMatch == null || key.length() > bestMatch.getKey().length()) {
            bestMatch = entry;
          }
        }
      }

      if (bestMatch != null) {
        try {
          bestMatch.getValue().handle(line, view);
        } catch (IOException e) {
          safePrintMessage(view, "Error: " + e.getMessage());
        }
      } else {
        safePrintMessage(view, "Error: Invalid command");
      }
    }
  }

  /**
   * Attempts to print a message via the view; if that fails, falls back to controller output.
   */
  private void safePrintMessage(CalendarView view, String message) {
    try {
      view.printMessage(message);
    } catch (IOException io) {
      try {
        output.append(message).append(System.lineSeparator());
      } catch (IOException secondary) {
        // Swallow secondary failure to avoid side effects during tests
      }
    }
  }

  /**
   * Parse a create-event command and perform the corresponding creation on the provided calendar.
   *
   * <p>Supports creating a single timed event, repeating series by count, repeating series until a
   * date, all-day events, and all-day repeating series (both by count and until date). On success
   * prints a confirmation message to the view; on failure prints a descriptive error message.
   *
   * @param input    the raw create command string (must match one of the controller's
   *                 create patterns)
   * @param calendar the calendar API to apply the created event or series to
   * @param view     the view used to print success or error messages to the user
   * @throws IOException if writing output to the provided view fails
   */
  private void handleCreateEvent(String input, CalendarApi calendar, CalendarView view)
      throws IOException {
    input = input.trim();

    if (input.matches(CREATE_SINGLE)) {
      String[] parts = splitCommands(input);
      String subject = parts[2];
      String from = parts[4];
      String to = parts[6];

      EventDraft draft = new EventDraft();
      draft.subject = subject;
      draft.start = Optional.of(LocalDateTime.parse(from));
      draft.end = Optional.of(LocalDateTime.parse(to));

      try {
        calendar.create(draft);
        safePrintMessage(view, "Event created successfully.");
      } catch (ValidationException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (ConflictException c) {
        safePrintMessage(view, "Event conflict: " + c.getMessage());
      }

    } else if (input.matches(CREATE_REPEAT_N)) {
      String[] parts = splitCommands(input);
      String subject = parts[2];
      LocalDateTime startDate = LocalDateTime.parse(parts[4]);

      SeriesDraft draft = new SeriesDraft();
      draft.subject = subject;
      draft.startDate = startDate.toLocalDate();
      draft.startTime = Optional.of(startDate.toLocalTime());

      LocalDateTime to = LocalDateTime.parse((parts[6]));
      draft.endTime = Optional.of(to.toLocalTime());

      String weekdays = parts[8];
      EnumSet<Weekday> weekdaySet = weekdaysSetFromString(weekdays);

      try {
        int n = Integer.parseInt(parts[10]);
        draft.rule = new RecurrenceRule(weekdaySet, Optional.of(n), Optional.empty());

        calendar.createSeries(draft);
        safePrintMessage(view, "Event created successfully.");
      } catch (ValidationException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (IllegalArgumentException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (ConflictException c) {
        safePrintMessage(view, "Event conflict: " + c.getMessage());
      }
    } else if (input.matches(CREATE_REPEAT_UNTIL)) {
      String[] parts = splitCommands(input);
      String subject = parts[2];
      String from = parts[4];

      SeriesDraft draft = new SeriesDraft();
      draft.subject = subject;
      draft.startDate = LocalDateTime.parse(from).toLocalDate();
      draft.startTime = Optional.of(LocalDateTime.parse(from).toLocalTime());
      LocalDateTime to = LocalDateTime.parse((parts[6]));
      draft.endTime = Optional.of(to.toLocalTime());

      String weekdays = parts[8];
      EnumSet<Weekday> weekdaySet = weekdaysSetFromString(weekdays);

      try {
        LocalDate untilDate = LocalDate.parse(parts[10]);
        LocalDate startDate = LocalDateTime.parse(from).toLocalDate();
        if (!untilDate.isAfter(startDate) && !untilDate.isEqual(startDate)) {
          throw new IllegalArgumentException("Until date must be after or equal to start date");
        }
        draft.rule = new RecurrenceRule(weekdaySet, Optional.empty(), Optional.of(untilDate));

        calendar.createSeries(draft);
        safePrintMessage(view, "Event created successfully.");
      } catch (ValidationException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (IllegalArgumentException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (ConflictException c) {
        safePrintMessage(view, "Event conflict: " + c.getMessage());
      }
    } else if (input.matches(CREATE_ALLDAY)) {
      String[] parts = splitCommands(input);

      String subject = parts[2];
      String onDate = parts[4];

      EventDraft draft = new EventDraft();
      draft.subject = subject;
      draft.allDayDate = Optional.of(LocalDate.parse(onDate));

      try {
        calendar.create(draft);
        safePrintMessage(view, "Event created successfully.");
      } catch (ValidationException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (ConflictException c) {
        safePrintMessage(view, "Event conflict: " + c.getMessage());
      }
    } else if (input.matches(CREATE_ALLDAY_REPEAT_N)) {
      String[] parts = splitCommands(input);
      String subject = parts[2];
      LocalDate onDate = LocalDate.parse(parts[4]);

      SeriesDraft draft = new SeriesDraft();
      draft.subject = subject;
      draft.startDate = onDate;
      draft.allDay = true;

      try {
        int n = Integer.parseInt(parts[8]);
        String weekdays = parts[6];
        EnumSet<Weekday> weekdaySet = weekdaysSetFromString(weekdays);
        draft.rule = new RecurrenceRule(weekdaySet, Optional.of(n), Optional.empty());

        calendar.createSeries(draft);
        safePrintMessage(view, "Event created successfully.");
      } catch (ValidationException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (IllegalArgumentException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (ConflictException c) {
        safePrintMessage(view, "Event conflict: " + c.getMessage());
      }
    } else if (input.matches(CREATE_ALLDAY_REPEAT_UNTIL)) {
      String[] parts = splitCommands(input);
      String subject = parts[2];
      LocalDate onDate = LocalDate.parse(parts[4]);

      SeriesDraft draft = new SeriesDraft();
      draft.subject = subject;
      draft.startDate = onDate;
      draft.allDay = true;

      try {
        String weekdays = parts[6];
        EnumSet<Weekday> weekdaySet = weekdaysSetFromString(weekdays);

        LocalDate untilDate = LocalDate.parse(parts[8]);
        if (!untilDate.isAfter(onDate) && !untilDate.isEqual(onDate)) {
          throw new IllegalArgumentException("Until date must be after or equal to start date");
        }
        draft.rule = new RecurrenceRule(weekdaySet, Optional.empty(), Optional.of(untilDate));

        calendar.createSeries(draft);
        safePrintMessage(view, "Event created successfully.");
      } catch (ValidationException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (IllegalArgumentException e) {
        safePrintMessage(view, "Fields are invalid " + e.getMessage());
      } catch (ConflictException c) {
        safePrintMessage(view, "Event conflict: " + c.getMessage());
      }
    } else {
      safePrintMessage(view, "Error: Invalid create event command format.");
    }
  }

  /**
   * Parse and execute an edit command to update events in the given calendar
   * and report results to the view.
   *
   * @param input    the raw user command specifying which events to edit and the new value
   * @param calendar the calendar model on which the update operations will be performed
   * @param view     the view used to print success or error messages to the user
   * @throws IOException if writing messages to the view or fallback output fails
   */
  private void handleEditEvent(String input, CalendarApi calendar, CalendarView view)
      throws IOException {
    input = input.trim();

    if (input.matches(EDIT_SINGLE)) {
      String[] parts = splitCommands(input);
      String subject = parts[3];
      LocalDateTime from = LocalDateTime.parse(parts[5]);
      LocalDateTime to = LocalDateTime.parse(parts[7]);

      EventSelector selector = new EventSelector();
      selector.subject = subject;
      selector.start = from;
      selector.end = Optional.of(to);

      String newPropertyValue = parts[9];
      String property = parts[2];

      try {
        EventPatch patch = createPatch(property, newPropertyValue);
        calendar.updateBySelector(selector, patch, EditScope.SINGLE);
        safePrintMessage(view, "Event updated successfully.");
      } catch (NotFoundException e) {
        safePrintMessage(view, "No matching event found: " + e.getMessage());
      } catch (ValidationException e) {
        safePrintMessage(view, "Invalid update values: " + e.getMessage());
      } catch (ConflictException e) {
        safePrintMessage(view, "Update failed due to event conflict: " + e.getMessage());
      } catch (IllegalArgumentException e) {
        // e.g., invalid status value
        safePrintMessage(view, "Invalid property value: " + e.getMessage());
      }
    } else if (input.matches(EDIT_EVENTS)) {
      String[] parts = splitCommands(input);
      String property = parts[2];
      String subject = parts[3];
      LocalDateTime from = LocalDateTime.parse(parts[5]);
      String newPropertyValue = parts[7];

      EventSelector selector = new EventSelector();
      selector.subject = subject;
      selector.start = from;

      try {
        EventPatch patch = createPatch(property, newPropertyValue);
        calendar.updateBySelector(selector, patch, EditScope.FOLLOWING);
        safePrintMessage(view, "Events updated successfully.");
      } catch (NotFoundException e) {
        safePrintMessage(view, "No matching event found: " + e.getMessage());
      } catch (ValidationException e) {
        safePrintMessage(view, "Invalid update values: " + e.getMessage());
      } catch (ConflictException e) {
        safePrintMessage(view, "Update failed due to event conflict: " + e.getMessage());
      }

    } else if (input.matches(EDIT_SERIES)) {
      String[] parts = splitCommands(input);
      String property = parts[2];
      String subject = parts[3];
      LocalDateTime from = LocalDateTime.parse(parts[5]);
      String newPropertyValue = parts[7];

      EventSelector selector = new EventSelector();
      selector.subject = subject;
      selector.start = from;

      try {
        EventPatch patch = createPatch(property, newPropertyValue);
        calendar.updateBySelector(selector, patch, EditScope.ENTIRE_SERIES);
        safePrintMessage(view, "Series updated successfully.");
      } catch (NotFoundException e) {
        safePrintMessage(view, "No matching event found: " + e.getMessage());
      } catch (ValidationException e) {
        safePrintMessage(view, "Invalid update values: " + e.getMessage());
      } catch (ConflictException e) {
        safePrintMessage(view, "Update failed due to event conflict: " + e.getMessage());
      }
    } else {
      safePrintMessage(view, "Error: Invalid edit command format.");
    }
  }

  /**
   * Prints busy status if the user has events scheduled on a given day and time,
   * otherwise, available.
   *
   * @param input    user input.
   * @param calendar calendar model.
   * @throws IOException if output is invalid.
   */
  private void handleShowStatus(String input, CalendarApi calendar, CalendarView view)
      throws IOException {
    if (input.trim().matches(SHOW_STATUS_ON)) {
      String[] parts = splitCommands(input);
      LocalDateTime dateString = LocalDateTime.parse(parts[3]);

      BusyStatus status = calendar.statusAt(dateString);
      view.printStatus(status);
    } else {
      safePrintMessage(view, "Error: Invalid print command format.");
    }
  }

  /**
   * Handles the export command by exporting all events from the provided calendar to a file.
   * Parses the filename from the command input, exports events using the appropriate exporter
   * based on the file extension, and reports success or failure to the supplied view. If the
   * view cannot be written to, writes an error fallback to the controller's output.
   *
   * @param input   the raw command input expected to match the EXPORT pattern
   *                and contain the target filename
   * @param calendar the calendar API to read events from
   * @param view    the view used to display success or error messages to the user
   * @throws IOException if writing the fallback error message to the controller output fails
   */
  private void handleExport(String input, CalendarApi calendar, CalendarView view)
      throws IOException {
    if (input.trim().matches(EXPORT)) {
      String[] parts = splitCommands(input);
      String fileName = parts[2];

      try {
        Path targetPath = Path.of(fileName);
        List<Event> events = calendar.allEvents();
        Path exportedFile = exportByExtension(targetPath, events);

        try {
          view.printMessage("Exported calendar to: " + exportedFile.toAbsolutePath());
        } catch (IOException io) {
          // View failed, append fallback message directly to output
          try {
            output.append("Error: Failed to export calendar: ")
                .append(io.getMessage())
                .append(System.lineSeparator());
          } catch (IOException ignored) {
            // Swallow secondary failure
          }
        }
      } catch (Exception e) {
        String errorMessage = e.getMessage();
        try {
          view.printMessage("Error: Failed to export calendar: " + errorMessage);
        } catch (IOException io2) {
          // View failed, append fallback message directly to output
          try {
            output.append("Error: Failed to export calendar: ")
                .append(errorMessage)
                .append(System.lineSeparator());
          } catch (IOException ignored) {
            // Swallow secondary failure
          }
        }
      }

    } else {
      safePrintMessage(view, "Error: Invalid export command format.");
    }
  }

  private Path exportByExtension(Path targetPath, List<Event> events) {
    String lowerName = targetPath.getFileName().toString().toLowerCase(Locale.ROOT);
    if (lowerName.endsWith(".csv")) {
      return csvExporter.export(targetPath, events);
    } else if (lowerName.endsWith(".ics") || lowerName.endsWith(".ical")) {
      return icalExporter.export(targetPath, events);
    }
    throw new IllegalArgumentException("Unsupported export format: " + targetPath);
  }

  /**
   * Creates an EventPatch with a new field based on the given property and value.
   *
   * @param property the property name to update (ex subject, start, end, etc.).
   * @param newValue the new value as a string
   * @return an EventPatch with the appropriate field set
   */
  private EventPatch createPatch(String property, String newValue) {
    EventPatch patch = new EventPatch();

    EditProperty.from(property).ifPresent(p -> {
      switch (p) {
        case SUBJECT:
          patch.subject = Optional.of(newValue);
          break;
        case START:
          patch.start = Optional.of(LocalDateTime.parse(newValue));
          break;
        case END:
          patch.end = Optional.of(LocalDateTime.parse(newValue));
          break;
        case DESCRIPTION:
          patch.description = Optional.of(newValue);
          break;
        case LOCATION:
          patch.location = Optional.of(newValue);
          break;
        case STATUS:
          patch.status = Optional.of(Status.valueOf(newValue.toUpperCase()));
          break;
        default:
          break;
      }
    });

    return patch;
  }

}