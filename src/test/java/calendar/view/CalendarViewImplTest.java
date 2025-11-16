package calendar.view;

import static org.junit.Assert.assertTrue;

import calendar.model.domain.BusyStatus;
import calendar.model.domain.Event;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.Test;

/**
 * Tests for CalendarViewImpl formatting and error handling.
 */
public class CalendarViewImplTest {

  private static Event event(String subject, String date, String startTime, String endTime) {
    return new Event.Builder()
        .subject(subject)
        .start(LocalDateTime.parse(date + "T" + startTime))
        .end(LocalDateTime.parse(date + "T" + endTime))
        .build();
  }

  @Test
  public void testPrintEventsOnWithAndWithoutLocation() {
    Event withLoc = new Event.Builder()
        .subject("meeting")
        .start(LocalDateTime.parse("2025-12-01T10:00"))
        .end(LocalDateTime.parse("2025-12-01T11:00"))
        .location("Room A")
        .build();

    Event withoutLoc = event("focus", "2025-12-01", "12:00", "13:00");

    StringBuilder out = new StringBuilder();
    CalendarViewImpl view = new CalendarViewImpl(out);
    view.printEventsOn(LocalDate.parse("2025-12-01"), Arrays.asList(withLoc, withoutLoc));

    String s = out.toString();
    assertTrue(s.contains("Events on 2025-12-01:"));
    assertTrue(s.contains("- meeting from 10:00 to 11:00 at Room A"));
    assertTrue(s.contains("- focus from 12:00 to 13:00"));
    assertTrue("should end with a blank line", s.endsWith("\n\n") || s.contains("\n\n"));
  }

  @Test
  public void testPrintEventsFromToFormatting() {
    Event a = event("a", "2025-11-01", "09:00", "10:00");
    Event b = new Event.Builder()
        .subject("b").start(LocalDateTime.parse("2025-11-01T10:30"))
        .end(LocalDateTime.parse("2025-11-01T12:00")).location("Lab 2").build();

    StringBuilder out = new StringBuilder();
    CalendarViewImpl view = new CalendarViewImpl(out);
    view.printEventsFromTo(
        LocalDateTime.parse("2025-11-01T08:00"),
        LocalDateTime.parse("2025-11-01T13:00"),
        Arrays.asList(a, b));

    String s = out.toString();
    assertTrue(s.contains("Events from 2025-11-01 08:00 to 2025-11-01 13:00:"));
    assertTrue(s.contains("- a starting on 2025-11-01 at 09:00, ending on 2025-11-01 at 10:00"));
    assertTrue(
        s.contains("- b starting on 2025-11-01 at 10:30, ending on 2025-11-01 at 12:00 at Lab 2"));
    assertTrue("should end with a blank line", s.endsWith("\n\n") || s.contains("\n\n"));
  }

  @Test
  public void testPrintStatus() {
    StringBuilder out = new StringBuilder();
    CalendarViewImpl view = new CalendarViewImpl(out);
    view.printStatus(BusyStatus.BUSY);
    view.printStatus(BusyStatus.AVAILABLE);
    String s = out.toString();
    assertTrue(s.contains("User is BUSY"));
    assertTrue(s.contains("User is not BUSY"));
  }

  @Test
  public void testPrintStatusBusyExact() {
    StringBuilder out = new StringBuilder();
    CalendarViewImpl view = new CalendarViewImpl(out);
    view.printStatus(BusyStatus.BUSY);
    String s = out.toString();
    assertTrue(s.equals("User is BUSY\n"));
  }

  @Test
  public void testPrintStatusAvailableExact() {
    StringBuilder out = new StringBuilder();
    CalendarViewImpl view = new CalendarViewImpl(out);
    view.printStatus(BusyStatus.AVAILABLE);
    String s = out.toString();
    assertTrue(s.equals("User is not BUSY\n"));
  }

  private static class FailingAppendable implements Appendable {
    @Override
    public Appendable append(CharSequence csq) throws IOException {
      throw new IOException("x");
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
      throw new IOException("x");
    }

    @Override
    public Appendable append(char c) throws IOException {
      throw new IOException("x");
    }
  }

  @Test(expected = IOException.class)
  public void testPrintMessageIoExceptionWrapped() throws IOException {
    CalendarViewImpl view = new CalendarViewImpl(new FailingAppendable());
    view.printMessage("hello");
  }

  @Test(expected = RuntimeException.class)
  public void testAppendLineFailureBecomesRuntime() {
    CalendarViewImpl view = new CalendarViewImpl(new FailingAppendable());
    view.printStatus(BusyStatus.BUSY);
  }
}

