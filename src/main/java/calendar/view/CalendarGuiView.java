package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Class for the GUI.
 */
public class CalendarGuiView extends JFrame implements CalendarGuiViewInterface {
  private final JButton nextMonthBtn;
  private final JButton prevMonthBtn;
  private final JButton createEventBtn;
  private final JButton createCalendarBtn;
  private final JLabel monthYearTitle;

  private final JPanel monthGrid;
  private final JLabel activeCalendarLabel;
  private final JLabel selectedDateLabel;
  private final JTextArea eventsArea;
  private ActionListener commandListener;
  private YearMonth currentMonth;
  private LocalDate selectedDate;

  /**
   * Initilizes the GUI for all the buttons and titles.
   */
  public CalendarGuiView() {
    super("Calendar GUI");
    this.setSize(900, 700);
    this.setLayout(new BorderLayout());

    prevMonthBtn = new JButton("<");
    nextMonthBtn = new JButton(">");
    createEventBtn = new JButton("New Event");
    createCalendarBtn = new JButton("New Calendar");
    activeCalendarLabel = new JLabel("Current Calendar: default");
    JPanel top = new JPanel();

    top.add(prevMonthBtn);
    top.add(nextMonthBtn);
    top.add(createEventBtn);
    top.add(createCalendarBtn);
    top.add(activeCalendarLabel);

    monthYearTitle = new JLabel("", JLabel.CENTER);
    monthYearTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.add(top, BorderLayout.NORTH);
    northPanel.add(monthYearTitle, BorderLayout.SOUTH);

    this.add(northPanel, BorderLayout.NORTH);

    monthGrid = new JPanel(new GridLayout(6, 7));
    this.add(monthGrid, BorderLayout.CENTER);

    selectedDateLabel = new JLabel("Select a date to view events");
    eventsArea = new JTextArea(15, 25);
    eventsArea.setEditable(false);
    eventsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JPanel east = new JPanel(new BorderLayout());
    east.add(selectedDateLabel, BorderLayout.NORTH);
    east.add(new JScrollPane(eventsArea), BorderLayout.CENTER);
    this.add(east, BorderLayout.EAST);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  @Override
  public void makeVisible() {
    this.setVisible(true);
  }

  @Override
  public void drawMonth(YearMonth month) {
    this.currentMonth = month;
    if (selectedDate == null || !selectedDate.getMonth().equals(month.getMonth())
        || selectedDate.getYear() != month.getYear()) {
      selectedDate = month.atDay(1);
    }
    monthGrid.removeAll();

    String title = month.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
        + " " + month.getYear();
    monthYearTitle.setText(title);

    monthGrid.setLayout(new GridLayout(7, 7));
    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (String dayName : days) {
      JLabel label = new JLabel(dayName, JLabel.CENTER);
      label.setFont(label.getFont().deriveFont(Font.BOLD));
      monthGrid.add(label);
    }

    int startDay = month.atDay(1).getDayOfWeek().getValue();
    int length = month.lengthOfMonth();

    int day = 1;
    for (int i = 1; i <= 42; i++) {
      if (i >= startDay && day <= length) {
        LocalDate date = month.atDay(day);
        JButton dayButton = new JButton(String.valueOf(day));
        dayButton.setActionCommand("select-day-" + date);
        if (commandListener != null) {
          dayButton.addActionListener(commandListener);
        }
        if (date.equals(selectedDate)) {
          dayButton.setBackground(new Color(0x4C8BF5));
          dayButton.setForeground(Color.WHITE);
          dayButton.setOpaque(true);
        }
        monthGrid.add(dayButton);
        day++;
      } else {
        monthGrid.add(new JLabel(""));
      }
    }

    monthGrid.revalidate();
    monthGrid.repaint();
  }

  @Override
  public void setCommandButtonListener(ActionListener listener) {
    this.commandListener = listener;
    prevMonthBtn.setActionCommand("prev-month");
    nextMonthBtn.setActionCommand("next-month");
    createEventBtn.setActionCommand("create-event");
    createCalendarBtn.setActionCommand("create-calendar");

    prevMonthBtn.addActionListener(listener);
    nextMonthBtn.addActionListener(listener);
    createEventBtn.addActionListener(listener);
    createCalendarBtn.addActionListener(listener);
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this,
        message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
    if (currentMonth != null) {
      drawMonth(currentMonth);
    }
  }

  @Override
  public void displayEvents(LocalDate date, List<String> events) {
    selectedDateLabel.setText("Events on " + date);
    if (events.isEmpty()) {
      eventsArea.setText("No events.");
    } else {
      eventsArea.setText(String.join("\n", events));
    }
  }

  @Override
  public Optional<String> promptForCreateEvent(LocalDate date) {
    JTextField subjectField = new JTextField(20);
    JTextField startTimeField = new JTextField("09:00");
    JTextField endTimeField = new JTextField("10:00");
    JCheckBox allDayCheck = new JCheckBox("All day");
    startTimeField.setEnabled(true);
    endTimeField.setEnabled(true);

    allDayCheck.addActionListener(e -> {
      boolean enabled = !allDayCheck.isSelected();
      startTimeField.setEnabled(enabled);
      endTimeField.setEnabled(enabled);
    });

    JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.add(new JLabel("Creating event on " + date));
    panel.add(new JLabel("Subject:"));
    panel.add(subjectField);
    panel.add(allDayCheck);
    panel.add(new JLabel("Start time (HH:mm):"));
    panel.add(startTimeField);
    panel.add(new JLabel("End time (HH:mm):"));
    panel.add(endTimeField);

    int result = JOptionPane.showConfirmDialog(this, panel, "New Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return Optional.empty();
    }

    String subject = subjectField.getText().trim();
    if (subject.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Subject cannot be blank.",
          "Invalid Input", JOptionPane.ERROR_MESSAGE);
      return Optional.empty();
    }

    if (allDayCheck.isSelected()) {
      return Optional.of(String.format("create event \"%s\" on %s",
          escapeSubject(subject), date));
    }

    try {
      LocalTime.parse(startTimeField.getText().trim());
      LocalTime.parse(endTimeField.getText().trim());
    } catch (DateTimeParseException e) {
      JOptionPane.showMessageDialog(this, "Time must be in HH:mm format.",
          "Invalid Input", JOptionPane.ERROR_MESSAGE);
      return Optional.empty();
    }

    return Optional.of(String.format("create event \"%s\" from %sT%s to %sT%s",
        escapeSubject(subject),
        date,
        startTimeField.getText().trim(),
        date,
        endTimeField.getText().trim()));
  }

  private String escapeSubject(String subject) {
    return subject.replace("\"", "'");
  }

  /**
   * Change the active calendar title.
   *
   * @param name of new active calendar.
   */
  public void setActiveCalendarName(String name) {
    activeCalendarLabel.setText("Current Calendar: " + name);
  }
}
