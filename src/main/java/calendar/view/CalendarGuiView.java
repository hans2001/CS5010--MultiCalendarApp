package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Swing GUI implementation.
 */
public class CalendarGuiView extends JFrame implements CalendarGuiViewInterface {
  private final JButton prevMonthBtn = new JButton("<");
  private final JButton nextMonthBtn = new JButton(">");
  private final JButton createEventBtn = new JButton("New Event");
  private final JButton createCalendarBtn = new JButton("New Calendar");
  private final JButton editCalendarBtn = new JButton("Edit Calendar");
  private final JComboBox<String> calendarSelector = new JComboBox<>();
  private final JLabel monthYearTitle = new JLabel("", JLabel.CENTER);
  private final JLabel activeCalendarLabel = new JLabel("Current Calendar: ");
  private final JLabel activeCalendarTzLabel = new JLabel("Timezone: ");
  private final JPanel monthGrid = new JPanel(new GridLayout(7, 7));
  private final JLabel selectedDateLabel = new JLabel("Select a date to view events");
  private final JTextArea eventsArea = new JTextArea(15, 25);
  private ActionListener commandListener;
  private YearMonth currentMonth;
  private LocalDate selectedDate;

  /**
   * Constructs the GUI.
   */
  public CalendarGuiView() {
    super("Calendar GUI");
    setSize(1000, 700);
    setLayout(new BorderLayout());

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calendarSelector.setPrototypeDisplayValue("Longest Calendar Name Here");
    top.add(new JLabel("Calendar:"));
    top.add(calendarSelector);
    top.add(prevMonthBtn);
    top.add(nextMonthBtn);
    top.add(createEventBtn);
    top.add(createCalendarBtn);
    top.add(editCalendarBtn);

    JPanel calendarInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
    calendarInfo.add(activeCalendarLabel);
    calendarInfo.add(Box.createRigidArea(new Dimension(20, 0)));
    calendarInfo.add(activeCalendarTzLabel);

    monthYearTitle.setFont(monthYearTitle.getFont().deriveFont(Font.BOLD, 20f));

    JPanel north = new JPanel(new BorderLayout());
    north.add(top, BorderLayout.NORTH);
    north.add(calendarInfo, BorderLayout.CENTER);
    north.add(monthYearTitle, BorderLayout.SOUTH);
    add(north, BorderLayout.NORTH);

    add(monthGrid, BorderLayout.CENTER);

    eventsArea.setEditable(false);
    eventsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JPanel east = new JPanel(new BorderLayout());
    east.add(selectedDateLabel, BorderLayout.NORTH);
    east.add(new JScrollPane(eventsArea), BorderLayout.CENTER);
    add(east, BorderLayout.EAST);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  @Override
  public void makeVisible() {
    setVisible(true);
  }

  @Override
  public void drawMonth(YearMonth month) {
    this.currentMonth = month;
    if (selectedDate == null
        || selectedDate.getMonthValue() != month.getMonthValue()
        || selectedDate.getYear() != month.getYear()) {
      selectedDate = month.atDay(1);
    }

    monthGrid.removeAll();
    String title = month.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
        + " " + month.getYear();
    monthYearTitle.setText(title);

    String[] headers = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (String header : headers) {
      JLabel label = new JLabel(header, JLabel.CENTER);
      label.setFont(label.getFont().deriveFont(Font.BOLD));
      monthGrid.add(label);
    }

    int startDay = month.atDay(1).getDayOfWeek().getValue();
    int daysInMonth = month.lengthOfMonth();
    int day = 1;
    for (int i = 1; i <= 42; i++) {
      if (i >= startDay && day <= daysInMonth) {
        LocalDate date = month.atDay(day);
        JButton button = new JButton(String.valueOf(day));
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusPainted(false);
        button.setActionCommand("select-day-" + date);
        if (commandListener != null) {
          button.addActionListener(commandListener);
        }
        if (date.equals(selectedDate)) {
          button.setBackground(new Color(0x4C8BF5));
          button.setForeground(Color.WHITE);
          button.setOpaque(true);
        }
        monthGrid.add(button);
        day++;
      } else {
        monthGrid.add(new JLabel(""));
      }
    }

    monthGrid.revalidate();
    monthGrid.repaint();
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
    eventsArea.setText(events.isEmpty() ? "No events." : String.join("\n", events));
  }

  @Override
  public Optional<String> promptForCreateEvent(LocalDate date) {
    final JTextField subjectField = new JTextField(20);
    JTextField startField = new JTextField("09:00");
    JTextField endField = new JTextField("10:00");
    JCheckBox allDayBox = new JCheckBox("All day");
    allDayBox.addActionListener(e -> {
      boolean enabled = !allDayBox.isSelected();
      startField.setEnabled(enabled);
      endField.setEnabled(enabled);
    });

    JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.add(new JLabel("Creating event on " + date));
    panel.add(new JLabel("Subject:"));
    panel.add(subjectField);
    panel.add(allDayBox);
    panel.add(new JLabel("Start time (HH:mm):"));
    panel.add(startField);
    panel.add(new JLabel("End time (HH:mm):"));
    panel.add(endField);

    int result = JOptionPane.showConfirmDialog(
        this, panel, "New Event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return Optional.empty();
    }

    String subject = subjectField.getText().trim();
    if (subject.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Subject cannot be blank.",
          "Invalid Input", JOptionPane.ERROR_MESSAGE);
      return Optional.empty();
    }

    if (allDayBox.isSelected()) {
      return Optional.of(String.format("create event \"%s\" on %s",
          escapeSubject(subject), date));
    }

    try {
      LocalTime.parse(startField.getText().trim());
      LocalTime.parse(endField.getText().trim());
    } catch (DateTimeParseException ex) {
      JOptionPane.showMessageDialog(this, "Time must be in HH:mm format.",
          "Invalid Input", JOptionPane.ERROR_MESSAGE);
      return Optional.empty();
    }

    return Optional.of(String.format("create event \"%s\" from %sT%s to %sT%s",
        escapeSubject(subject), date, startField.getText().trim(),
        date, endField.getText().trim()));
  }

  private String escapeSubject(String subject) {
    return subject.replace("\"", "'");
  }

  @Override
  public void setCommandButtonListener(ActionListener listener) {
    this.commandListener = listener;
    prevMonthBtn.setActionCommand("prev-month");
    nextMonthBtn.setActionCommand("next-month");
    createEventBtn.setActionCommand("create-event");
    createCalendarBtn.setActionCommand("create-calendar");
    editCalendarBtn.setActionCommand("edit-calendar");
    calendarSelector.setActionCommand("select-calendar");

    prevMonthBtn.addActionListener(listener);
    nextMonthBtn.addActionListener(listener);
    createEventBtn.addActionListener(listener);
    createCalendarBtn.addActionListener(listener);
    editCalendarBtn.addActionListener(listener);
    calendarSelector.addActionListener(listener);
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Message",
        JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public String[] promptNewCalendar() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
    JTextField nameField = new JTextField();
    panel.add(new JLabel("Calendar Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    String[] zones = ZoneId.getAvailableZoneIds().stream()
        .sorted().toArray(String[]::new);
    JComboBox<String> tzBox = new JComboBox<>(zones);
    panel.add(tzBox);

    int result = JOptionPane.showConfirmDialog(
        this, panel, "Create New Calendar",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return null;
    }
    String name = nameField.getText().trim();
    String tz = Objects.requireNonNull((String) tzBox.getSelectedItem());
    if (name.isEmpty() || tz.isEmpty()) {
      showError("Both fields must be filled.");
      return null;
    }
    return new String[]{name, tz};
  }

  @Override
  public void setActiveCalendarName(String name) {
    activeCalendarLabel.setText("Current Calendar: " + name);
  }

  @Override
  public void setActiveCalendarTimezone(String tz) {
    activeCalendarTzLabel.setText("Timezone: " + tz);
  }

  @Override
  public void addCalendarToSelector(String name) {
    calendarSelector.addItem(name);
  }

  @Override
  public void editCalendarInSelector(String ogName, String newName) {
    int count = calendarSelector.getItemCount();
    for (int i = 0; i < count; i++) {
      if (calendarSelector.getItemAt(i).equals(ogName)) {
        calendarSelector.insertItemAt(newName, i);
        calendarSelector.removeItemAt(i + 1);
        calendarSelector.setSelectedItem(newName);
        return;
      }
    }
  }

  @Override
  public void selectCalendarOnCalendarSelector(String name) {
    calendarSelector.setSelectedItem(name);
  }

  @Override
  public String getSelectedCalendarName() {
    return (String) calendarSelector.getSelectedItem();
  }

  @Override
  public String[] displayEditCalendar(String calendarName, String calendarTz) {
    JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
    panel.add(new JLabel("Calendar Name:"));
    JTextField nameField = new JTextField(calendarName);
    panel.add(nameField);

    panel.add(new JLabel("Timezone:"));
    String[] zones = ZoneId.getAvailableZoneIds().stream()
        .sorted().toArray(String[]::new);
    JComboBox<String> tzBox = new JComboBox<>(zones);
    tzBox.setSelectedItem(calendarTz);
    panel.add(tzBox);

    int result = JOptionPane.showConfirmDialog(
        this, panel, "Edit Calendar",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return new String[]{calendarName, calendarName, calendarTz};
    }
    String newName = nameField.getText().trim();
    String newTz = Objects.requireNonNull((String) tzBox.getSelectedItem());
    return new String[]{calendarName, newName, newTz};
  }
}
