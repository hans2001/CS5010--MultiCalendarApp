package calendar.controller;

import static calendar.controller.CommandPatterns.EXPORT;
import static calendar.controller.CommandPatterns.SHOW_STATUS_ON;
import static calendar.controller.service.CommandTokenizer.tokenize;

import calendar.controller.commands.CommandHandler;
import calendar.controller.commands.HandleEvents;
import calendar.controller.service.CalendarFormService;
import calendar.controller.service.EventCreationRequest;
import calendar.controller.service.EventEditRequest;
import calendar.export.CalendarExporter;
import calendar.export.CsvExporter;
import calendar.export.IcalExporter;
import calendar.model.CalendarManager;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.api.CalendarApi;
import calendar.model.api.EditScope;
import calendar.model.config.CalendarSettings;
import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.model.exception.ConflictException;
import calendar.model.exception.NotFoundException;
import calendar.model.exception.ValidationException;
import calendar.view.CalendarView;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Calendar controller for processing user input and interacting with model and view.
 */
public class CalendarControllerImpl implements CalendarController {
  private final Readable input;
  private final Appendable output;
  private final CalendarExporter csvExporter;
  private final CalendarExporter icalExporter;
  private final CalendarManager calendarManager;
  private final CalendarFormService formService;
  private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
  private TimeZoneInMemoryCalendarInterface inUseCalendar = null;
  private static final String ERROR_NO_CALENDAR =
      "Error: No calendar selected.";

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
    this.formService = new CalendarFormService();
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
    commandHandlers.put("create event", (line, view) -> guardWithActiveCalendar(view,
        () -> handleCreateEvent(line, inUseCalendar, view)));

    commandHandlers.put("edit", (line, view) -> guardWithActiveCalendar(view,
        () -> handleEditEvent(line, inUseCalendar, view)));

    commandHandlers.put("edit event", (line, view) -> guardWithActiveCalendar(view,
        () -> handleEditEvent(line, inUseCalendar, view)));

    commandHandlers.put("print", (line, view) -> guardWithActiveCalendar(view,
        () -> HandleEvents.handlePrintEvent(line, view, calendarManager)));

    commandHandlers.put("show status on", (line, view) -> guardWithActiveCalendar(view,
        () -> handleShowStatus(line, inUseCalendar, view)));

    commandHandlers.put("export cal", (line, view) -> guardWithActiveCalendar(view,
        () -> handleExport(line, inUseCalendar, view)));

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

    commandHandlers.put("edit calendar", (line, view) -> guardWithActiveCalendar(view,
        () -> HandleEvents.handleEditCalendarEvent(line, calendarManager, view)));

    commandHandlers.put("copy event", (line, view) -> guardWithActiveCalendar(view,
        () -> HandleEvents.handleCopyEvent(line, calendarManager, view, inUseCalendar)));
  }

  private void guardWithActiveCalendar(CalendarView view, CheckedCalendarAction action)
      throws IOException {
    if (inUseCalendar == null) {
      view.printMessage(ERROR_NO_CALENDAR);
    } else {
      action.run();
    }
  }

  @FunctionalInterface
  private interface CheckedCalendarAction {
    void run() throws IOException;
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
   * @param input    the raw create command string (must match one of the controller's
   *                 create patterns)
   * @param calendar the calendar API to apply the created event or series to
   * @param view     the view used to print success or error messages to the user
   * @throws IOException if writing output to the provided view fails
   */
  private void handleCreateEvent(String input, CalendarApi calendar, CalendarView view)
      throws IOException {
    try {
      EventCreationRequest request = formService.parseCreateEventCommand(input.trim());
      formService.applyCreateEvent(request, calendar);
      safePrintMessage(view, "Event created successfully.");
    } catch (ValidationException e) {
      safePrintMessage(view, "Fields are invalid " + e.getMessage());
    } catch (ConflictException c) {
      safePrintMessage(view, "Event conflict: " + c.getMessage());
    } catch (IllegalArgumentException e) {
      safePrintMessage(view, "Fields are invalid " + e.getMessage());
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
    try {
      EventEditRequest request = formService.parseEditEventCommand(input.trim());
      formService.applyEditEvent(request, calendar);
      String successMessage = request.scope() == EditScope.SINGLE
          ? "Event updated successfully."
          : request.scope() == EditScope.FOLLOWING
              ? "Events updated successfully."
              : "Series updated successfully.";
      safePrintMessage(view, successMessage);
    } catch (NotFoundException e) {
      safePrintMessage(view, "No matching event found: " + e.getMessage());
    } catch (ValidationException e) {
      safePrintMessage(view, "Invalid update values: " + e.getMessage());
    } catch (ConflictException e) {
      safePrintMessage(view, "Update failed due to event conflict: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      safePrintMessage(view, e.getMessage());
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
      String[] parts = tokenize(input);
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
      String[] parts = tokenize(input);
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

}
