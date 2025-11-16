package calendar.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * Abstract class to make inputting and outputting easier.
 */
public abstract class CalendarRunnerTester {
  /**
   * Helper function for running tests.
   *
   * @param input from test.
   *
   * @return valid string.
   *
   * @throws Exception general error.
   */
  protected String runWithInput(String input) throws Exception {
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
}
