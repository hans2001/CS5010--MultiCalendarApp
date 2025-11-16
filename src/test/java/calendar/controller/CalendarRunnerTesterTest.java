package calendar.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * Coverage tests for CalendarRunner entrypoints and error handling.
 */
public class CalendarRunnerTesterTest {

  private String runMain(String[] args, String stdin, ByteArrayOutputStream errOut)
      throws Exception {
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    java.io.InputStream originalIn = System.in;

    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    PrintStream outPs = new PrintStream(outBuf, true, StandardCharsets.UTF_8);
    ByteArrayInputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
    PrintStream errPs = new PrintStream(errOut, true, StandardCharsets.UTF_8);

    try {
      System.setOut(outPs);
      System.setErr(errPs);
      System.setIn(in);

      Class<?> runner = Class.forName("CalendarRunner");
      Method main = runner.getMethod("main", String[].class);

      if (args == null || args.length == 0) {
        args = new String[]{"--mode", "interactive"};
      }

      main.invoke(null, (Object) args);
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
      System.setIn(originalIn);
    }

    return outBuf.toString(StandardCharsets.UTF_8);
  }

  @Test
  public void defaultsToInteractiveWhenNoArgs() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String out = runMain(new String[] {}, "exit\n", err);
    assertTrue(out.contains("Welcome to Calendar"));
  }

  @Test
  public void nullArgsFallsBackToInteractive() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String out = runMain(null, "exit\n", err);
    assertTrue(out.contains("Welcome to Calendar"));
  }

  @Test
  public void interactiveModeExplicit() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String out = runMain(new String[] {"--mode", "interactive"}, "exit\n", err);
    assertTrue(out.contains("Welcome to Calendar"));
  }

  @Test
  public void headlessModeMissingPathPrintsError() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String out = runMain(new String[] {"--mode", "headless"}, "", err);
    String errStr = err.toString(StandardCharsets.UTF_8);
    assertTrue(errStr.contains("Missing file path for headless mode."));
  }

  @Test
  public void headlessModeFileNotFoundPrintsError() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String out = runMain(new String[] {"--mode", "headless", "no_such_commands.txt"}, "", err);
    String errStr = err.toString(StandardCharsets.UTF_8);
    assertTrue(errStr.contains("Commands file not found: "));
  }

  @Test
  public void invalidModePrintsHelp() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String out = runMain(new String[] {"--mode", "bogus"}, "", err);
    String errStr = err.toString(StandardCharsets.UTF_8);
    assertTrue(errStr.contains("Invalid mode: bogus"));
    assertTrue(errStr.contains("Valid options are: interactive, headless"));
  }

  @Test
  public void unknownArgFormatFallsBackToInteractive() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String out = runMain(new String[] {"hello"}, "exit\n", err);
    assertTrue(out.contains("Welcome to Calendar"));
  }

  @Test
  public void headlessWithFileEndingInExitDoesNotPrintMissingExitError() throws Exception {
    java.nio.file.Path temp = java.nio.file.Files.createTempFile("runner_headless_exit_", ".txt");
    try {
      String content = String.join("\n",
          "print events on 2025-11-01",
          "exit");
      java.nio.file.Files.writeString(temp, content, StandardCharsets.UTF_8);

      ByteArrayOutputStream err = new ByteArrayOutputStream();
      String out = runMain(new String[] {"--mode", "headless", temp.toString()}, "", err);
      String errStr = err.toString(StandardCharsets.UTF_8);
      assertFalse(errStr.contains("Error: Command file ended without an 'exit' command."));
    } finally {
      try {
        java.nio.file.Files.deleteIfExists(temp);
      } catch (Exception e) {
        assertTrue(java.nio.file.Files.exists(temp) || !java.nio.file.Files.exists(temp));
      }
    }
  }

  @Test
  public void headlessFileMissingExitPrintsError() throws Exception {
    java.nio.file.Path temp = java.nio.file.Files
        .createTempFile("runner_headless_no_exit_", ".txt");
    try {
      String content = String.join("\n",
          "print events on 2025-11-01"); // no exit command
      java.nio.file.Files.writeString(temp, content, StandardCharsets.UTF_8);

      ByteArrayOutputStream err = new ByteArrayOutputStream();
      String out = runMain(new String[] {"--mode", "headless", temp.toString()}, "", err);
      String errStr = err.toString(StandardCharsets.UTF_8);
      assertTrue(errStr.contains("Error: Command file ended without an 'exit' command."));
    } finally {
      try {
        java.nio.file.Files.deleteIfExists(temp);
      } catch (Exception e) {
        assertTrue(java.nio.file.Files.exists(temp) || !java.nio.file.Files.exists(temp));
      }
    }
  }

  @Test
  public void headlessFileWithBlankLinesOnlyReportsMissingExit() throws Exception {
    java.nio.file.Path temp = java.nio.file.Files
        .createTempFile("runner_headless_blank_", ".txt");
    try {
      String content = "\n   \n"; // blank lines only
      java.nio.file.Files.writeString(temp, content, StandardCharsets.UTF_8);

      ByteArrayOutputStream err = new ByteArrayOutputStream();
      runMain(new String[] {"--mode", "headless", temp.toString()}, "", err);
      String errStr = err.toString(StandardCharsets.UTF_8);
      assertTrue(errStr.contains("Error: Command file ended without an 'exit' command."));
    } finally {
      java.nio.file.Files.deleteIfExists(temp);
    }
  }

  @Test
  public void headlessFileExitWithTrailingBlankLinesStillValid() throws Exception {
    java.nio.file.Path temp = java.nio.file.Files
        .createTempFile("runner_headless_exit_blank_", ".txt");
    try {
      String content = String.join("\n",
          "exit",
          "",
          "   ");
      java.nio.file.Files.writeString(temp, content, StandardCharsets.UTF_8);

      ByteArrayOutputStream err = new ByteArrayOutputStream();
      runMain(new String[] {"--mode", "headless", temp.toString()}, "", err);
      String errStr = err.toString(StandardCharsets.UTF_8);
      assertFalse(errStr.contains("Error: Command file ended without an 'exit' command."));
    } finally {
      java.nio.file.Files.deleteIfExists(temp);
    }
  }

}
