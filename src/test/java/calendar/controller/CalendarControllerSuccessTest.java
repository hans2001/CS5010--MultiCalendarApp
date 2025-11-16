package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Verifies that successful controller commands emit confirmation messages via safePrint.
 */
public final class CalendarControllerSuccessTest {
  private static final String DEFAULT_CALENDAR_SETUP = String.join("\n",
      "create calendar --name school --timezone America/New_York",
      "use calendar --name school");

  @Test
  public void createVariantsPrintSuccessMessages() throws Exception {
    String commands = String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "create event single from 2025-11-01T10:00 to 2025-11-01T11:00",
        "create event seriesN from 2025-11-02T09:00 to 2025-11-02T10:00 repeats MW for 2 times",
        "create event seriesUntil from 2025-11-03T09:00 to 2025-11-03T10:00 repeats MW until"
            + " 2025-11-10",
        "create event allday on 2025-12-01",
        "create event alldayN on 2025-12-02 repeats MW for 2 times",
        "create event alldayUntil on 2025-12-03 repeats MT until 2025-12-10",
        "exit");

    RecordingView view = new RecordingView();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(commands), new StringBuilder());
    controller.go(view);

    long confirmations = view.getMessages().stream()
        .filter(m -> m.contains("Event created successfully."))
        .count();
    assertEquals(6, confirmations);
  }

  @Test
  public void invalidCommandUsesSafePrint() throws Exception {
    String commands = String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "invalid command that should trigger error",
        "exit");

    RecordingView view = new RecordingView();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(commands), new StringBuilder());
    controller.go(view);

    assertTrue(view.getMessages().stream().anyMatch(m -> m.contains("Error: Invalid command")));
  }

  @Test
  public void editCommandsSuccessMessages() throws Exception {
    String commands = String.join("\n",
        DEFAULT_CALENDAR_SETUP,
        "create event single from 2025-11-01T10:00 to 2025-11-01T11:00",
        "edit event subject single from 2025-11-01T10:00 to 2025-11-01T11:00 with updated",
        "edit events subject updated from 2025-11-01T10:00 with updatedAgain",
        "edit series subject updatedAgain from 2025-11-01T10:00 with finalName",
        "exit");

    RecordingView view = new RecordingView();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(commands), new StringBuilder());
    controller.go(view);

    assertTrue(view.getMessages().contains("Event created successfully."));
    assertTrue(view.getMessages().stream().noneMatch(m -> m.startsWith("Error")));
  }

  @Test
  public void acceptsMultiSegmentTimezoneNames() throws Exception {
    String commands = String.join("\n",
        "create calendar --name multi --timezone America/Argentina/Buenos_Aires",
        "use calendar --name multi",
        "edit calendar --name multi --property timezone America/North_Dakota/New_Salem",
        "exit");

    StringBuilder output = new StringBuilder();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(commands), output);
    CalendarView view = new CalendarViewImpl(output);
    controller.go(view);

    String transcript = output.toString();
    assertTrue(transcript.contains("Successfully created calendar: multi"));
    assertTrue(transcript.contains("Successfully edited calendar timezone."));
  }

  @Test
  public void acceptsSingleSegmentTimezoneNames() throws Exception {
    String commands = String.join("\n",
        "create calendar --name utcOnly --timezone UTC",
        "use calendar --name utcOnly",
        "exit");

    RecordingView view = new RecordingView();
    CalendarController controller =
        new CalendarControllerImpl(new StringReader(commands), new StringBuilder());
    controller.go(view);

    assertTrue(view.getMessages().contains("Successfully created calendar: utcOnly"));
    assertTrue(view.getMessages().contains("Successfully switched calendar to utcOnly"));
  }

  private static final class RecordingView implements CalendarView {
    private final List<String> messages = new ArrayList<>();

    List<String> getMessages() {
      return messages;
    }

    @Override
    public void printMessage(String message) {
      messages.add(message);
    }

    @Override
    public void printEventsOn(LocalDate date, List<Event> events) {
      // not needed for these tests
    }

    @Override
    public void printEventsFromTo(LocalDateTime from, LocalDateTime to,
                                  List<Event> events) {
      // not needed
    }

    @Override
    public void printStatus(BusyStatus status) {
      // not needed
    }
  }
}
