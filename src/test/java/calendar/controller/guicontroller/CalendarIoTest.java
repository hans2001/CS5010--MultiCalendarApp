package calendar.controller.guicontroller;

import calendar.controller.CalendarController;
import calendar.controller.CalendarControllerImpl;
import calendar.model.config.CalendarSettings;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;

/**
 * Tests IO.
 */
public class CalendarIoTest {
  @Test
  public void testSafePrintMessageIoException() {
    CalendarView view = new CalendarViewImpl(System.out) {
      @Override
      public void printMessage(String message) throws IOException {
        throw new IOException("Simulated failure");
      }
    };

    String simulatedInput = "create calendar --name school --timezone America/New_York\nexit\n";
    ByteArrayInputStream inStream = new ByteArrayInputStream(simulatedInput.getBytes());

    CalendarSettings settings = CalendarSettings.defaults();

    CalendarController controller = new CalendarControllerImpl(
        new InputStreamReader(inStream),
        System.out,
        settings
    );

    try {
      controller.go(view);
    } catch (Exception e) {
      return;
    }
  }
}
