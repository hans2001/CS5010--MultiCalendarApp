import calendar.controller.CalendarController;
import calendar.controller.CalendarControllerImpl;
import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendar;
import calendar.model.TimeZoneInMemoryCalendarInterface;
import calendar.model.api.CalendarApi;
import calendar.model.config.CalendarSettings;
import calendar.model.internal.InMemoryCalendar;
import calendar.view.CalendarGuiView;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * Main entry point for the calendar app.
   *
   * @param args command line arguments:
   *             (--mode interactive)
   *             (--mode headless commands.txt).
   */
  public static void main(String[] args) {
    CalendarSettings settings = CalendarSettings.defaults();
    CalendarApi model = new InMemoryCalendar(settings);

    try {
      if (args == null || args.length == 0) {
        runGui(settings);
        return;
      }

      if (args.length >= 2 && "--mode".equalsIgnoreCase(args[0])) {
        String mode = args[1].toLowerCase(Locale.ROOT);
        if (mode.equals("interactive")) {
          runInteractive(model, settings);
          return;
        } else if (mode.equals("headless")) {
          if (args.length < 3) {
            System.err.println("Missing file path for headless mode.");
            return;
          }
          runHeadless(model, settings, args[2]);
          return;
        } else {
          System.err.println("Invalid mode: " + args[1]);
          System.err.println("Valid options are: interactive, headless");
          return;
        }
      }

      runInteractive(model, settings);
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

  /**
   * Start an interactive session that reads commands from standard input and
   * writes output to standard output.
   *
   * @param model    the calendar model used by the interactive session
   * @param settings configuration for the session's behavior and display
   * @throws IOException if an I/O error occurs while reading from standard input
   *                     or writing to standard output
   */
  private static void runInteractive(CalendarApi model, CalendarSettings settings)
      throws IOException {
    CalendarView view = new CalendarViewImpl(System.out);
    CalendarController controller =
        new CalendarControllerImpl(
            new InputStreamReader(System.in),
            System.out,
            settings);
    controller.go(view);
  }

  /**
   * Runs the calendar app with a GUI.
   *
   * @param settings configuration for the session's behavior and display
   * @throws IOException error.
   */
  private static void runGui(CalendarSettings settings)
      throws IOException {
    CalendarGuiView view = new CalendarGuiView();

    CalendarManager calendarManager = new CalendarManager();
    ZoneId systemZone = ZoneId.systemDefault();
    TimeZoneInMemoryCalendarInterface inUseCalendar = calendarManager.createCalendar(
        "Default Calendar", systemZone.toString());
    GuiCalendar guiCalendar = new GuiCalendar(
        inUseCalendar.getZoneId().getId(),
        inUseCalendar.getName());

    CalendarGuiController controller = new CalendarGuiController(settings,
        view, guiCalendar, calendarManager);

    view.makeVisible();
  }

  /**
   * Execute the calendar in headless mode by reading commands from the given file.
   * Attempts to open and process the file at {@code filePath} with a controller connected
   * to {@code settings} and standard output; prints an error and returns if the file
   * does not exist or if the file does not end with an "exit" command.
   *
   * @param model the calendar model (unused by this method but provided for symmetry)
   * @param settings runtime settings used to construct the controller
   * @param filePath path to the commands file to execute
   * @throws IOException if an I/O error occurs while reading the commands file or writing output
   */
  private static void runHeadless(CalendarApi model, CalendarSettings settings, String filePath)
      throws IOException {
    File file = new File(filePath);
    if (!file.exists()) {
      System.err.println("Commands file not found: " + filePath);
      return;
    }

    try (Reader reader = new FileReader(file)) {
      CalendarView view = new CalendarViewImpl(System.out);
      CalendarController controller =
          new CalendarControllerImpl(
              reader,
              System.out,
              settings);

      controller.go(view);

      if (!fileContainsExit(file)) {
        System.err.println("Error: Command file ended without an 'exit' command.");
      }
    }
  }

  /**
   * Checks if the file ends with an exit command.
   *
   * @param file file.
   *
   * @return if we have exit as last command.
   * @throws IOException invalid output.
   */
  private static boolean fileContainsExit(File file) throws IOException {
    String lastLine = null;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          lastLine = line.trim();
        }
      }
    }

    return lastLine != null && lastLine.equalsIgnoreCase("exit");
  }
}