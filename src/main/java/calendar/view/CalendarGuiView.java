package calendar.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Class for the GUI.
 */
public class CalendarGuiView extends JFrame implements CalendarGuiViewInterface {
  private JButton nextMonthBtn;
  private JButton prevMonthBtn;
  private JButton createEventBtn;
  private JButton createCalendarBtn;
  private JLabel monthYearTitle;
  private ActionListener commandListener;

  private JLabel activeCalendarTzLabel;
  private JPanel monthGrid;
  private JLabel activeCalendarLabel;
  private JComboBox<String> calendarSelector;

  /**
   * Initializes the GUI for all the buttons and titles.
   */
  public CalendarGuiView() {
    super("Calendar GUI");
    this.setSize(900, 700);
    this.setLayout(new BorderLayout());

    prevMonthBtn = new JButton("<");
    nextMonthBtn = new JButton(">");
    createEventBtn = new JButton("New Event");
    createCalendarBtn = new JButton("New Calendar");

    activeCalendarLabel = new JLabel("Current Calendar: ");
    activeCalendarTzLabel = new JLabel("Timezone: ");

    // Controls bar on top
    JPanel top = new JPanel();
    calendarSelector = new JComboBox<>();
    calendarSelector.setPrototypeDisplayValue("Longest Calendar Name Here");
    top.add(calendarSelector);

    top.add(prevMonthBtn);
    top.add(nextMonthBtn);
    top.add(createEventBtn);
    top.add(createCalendarBtn);
    top.add(activeCalendarLabel);

    // Calendar name + timezone below buttons
    JPanel calendarInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    calendarInfoPanel.add(activeCalendarLabel);
    calendarInfoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
    calendarInfoPanel.add(activeCalendarTzLabel);

    // Calendar's Current Month and Year
    monthYearTitle = new JLabel("", JLabel.CENTER);
    monthYearTitle.setFont(new Font("SansSerif", Font.BOLD, 20));

    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.add(top, BorderLayout.NORTH);
    northPanel.add(calendarInfoPanel, BorderLayout.CENTER);
    northPanel.add(monthYearTitle, BorderLayout.SOUTH);

    this.add(northPanel, BorderLayout.NORTH);

    monthGrid = new JPanel(new GridLayout(6, 7));
    this.add(monthGrid, BorderLayout.CENTER);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  @Override
  public void makeVisible() {
    this.setVisible(true);
  }

  @Override
  public void drawMonth(YearMonth month) {
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
        dayButton.setMargin(new Insets(1, 1, 1, 1));
        dayButton.setFocusPainted(false);

        monthGrid.add(new JLabel(String.valueOf(date.getDayOfMonth()), JLabel.CENTER));
        //dayButton.setActionCommand("cal-events-on-day-" + date.toString());
        //dayButton.addActionListener(commandListener);

        //monthGrid.add(dayButton);

        day++;
      } else {
        monthGrid.add(new JLabel(""));
      }
    }

    monthGrid.revalidate();
    monthGrid.repaint();
  }

  @Override
  public String[] promptNewCalendar() {
    JPanel panel = new JPanel(new GridLayout(2, 2));

    panel.add(new JLabel("Calendar Name:"));
    JTextField nameField = new JTextField();
    panel.add(nameField);

    panel.add(new JLabel("Timezone:"));
    String[] zones = ZoneId.getAvailableZoneIds()
        .stream()
        .sorted()
        .toArray(String[]::new);
    JComboBox<String> timezoneBox = new JComboBox<>(zones);
    panel.add(timezoneBox);

    int result = JOptionPane.showConfirmDialog(
        this, panel, "Create New Calendar",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    //user cancel
    if (result != JOptionPane.OK_OPTION) {
      return null;
    }

    String name = nameField.getText().trim();
    String tz = (String) timezoneBox.getSelectedItem();

    if (name.isEmpty() || Objects.requireNonNull(tz).isEmpty()) {
      showError("Both fields must be filled.");
      return null;
    }

    return new String[]{name, tz};
  }

  @Override
  public void addCalendarToCalendarList(String name) {
    calendarSelector.addItem(name);
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
  public void setCommandButtonListener(ActionListener listener) {
    this.commandListener = listener;

    prevMonthBtn.setActionCommand("prev-month");
    nextMonthBtn.setActionCommand("next-month");
    createEventBtn.setActionCommand("create-event");
    createCalendarBtn.setActionCommand("create-calendar");
    calendarSelector.setActionCommand("select-calendar");

    prevMonthBtn.addActionListener(listener);
    nextMonthBtn.addActionListener(listener);
    createEventBtn.addActionListener(listener);
    createCalendarBtn.addActionListener(listener);
    calendarSelector.addActionListener(listener);
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this,
        message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(
        this, message,
        "Message",
        JOptionPane.INFORMATION_MESSAGE
    );
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
  public String[] displayEditCalendar(String calendarName, String calendarTz) {
    JPanel panel = new JPanel(new GridLayout(2, 2));

    panel.add(new JLabel("Calendar Name:"));
    JTextField nameField = new JTextField(calendarName);
    panel.add(nameField);

    panel.add(new JLabel("Timezone:"));
    String[] zones = ZoneId.getAvailableZoneIds()
        .stream()
        .sorted()
        .toArray(String[]::new);
    JComboBox<String> timezoneBox = new JComboBox<>(zones);
    timezoneBox.setSelectedItem(calendarTz);
    panel.add(timezoneBox);

    int result = JOptionPane.showConfirmDialog(
        this,
        panel,
        "Edit Calendar",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (result != JOptionPane.OK_OPTION) {
      return new String[]{calendarName, calendarName, calendarTz};
    }
    String newName = nameField.getText().trim();
    String newTz = (String) timezoneBox.getSelectedItem();

    return new String[]{calendarName, newName, newTz};
  }
}
