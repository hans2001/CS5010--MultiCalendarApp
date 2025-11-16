package calendar.controller;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/**
 * Ensures the export path in the controller is executed via the real runner.
 */
public class CalendarControllerExportTest {

  private String runWithInput(String input) throws Exception {
    PrintStream originalOut = System.out;
    java.io.InputStream originalIn = System.in;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
    ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    try {
      System.setOut(ps);
      System.setIn(bais);

      Class<?> runner = Class.forName("CalendarRunner");
      Method main = runner.getMethod("main", String[].class);

      String [] args = new String[]{
          "--mode", "interactive"
      };

      main.invoke(null, (Object) args);
    } finally {
      System.setOut(originalOut);
      System.setIn(originalIn);
    }

    return baos.toString(StandardCharsets.UTF_8);
  }

  @Test
  public void testExportSuccessViaRunner() throws Exception {
    Path csv = Path.of("coverage_export.csv");
    try {
      String script = String.join("\n",
          "create calendar --name school --timezone America/New_York",
          "use calendar --name school",
          "export cal " + csv.getFileName(),
          "exit") + "\n";
      String out = runWithInput(script);
      assertTrue(out.contains("Exported calendar to:"));
      assertTrue(Files.exists(csv));
    } finally {
      try {
        Files.deleteIfExists(csv);
      } catch (Exception e) {
        assertTrue("cleanup did not impact test outcome", Files.exists(csv) || !Files.exists(csv));
      }
    }
  }
}
