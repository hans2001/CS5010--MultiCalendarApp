package calendar.view;

import calendar.controller.service.EventCreationRequest;
import calendar.controller.service.EventEditRequest;
import calendar.view.dialog.EventCreationDialog;
import calendar.view.dialog.EventEditDialog;
import calendar.view.model.GuiEventSummary;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

/**
 * Swing GUI implementation.
 */
public class CalendarGuiView extends JFrame implements CalendarGuiViewInterface {
  private final JButton prevMonthBtn = new JButton("<");
  private final JButton nextMonthBtn = new JButton(">");
  private final JButton createEventBtn = new JButton("New Event");
  private final JButton editEventBtn = new JButton("Edit Event");
  private final JButton createCalendarBtn = new JButton("New Calendar");
  private final JButton editCalendarBtn = new JButton("Edit Calendar");
  private final JComboBox<String> calendarSelector = new JComboBox<>();
  private final JLabel monthYearTitle = new JLabel("", JLabel.CENTER);
  private final JLabel activeCalendarLabel = new JLabel("Current Calendar: ");
  private final JLabel activeCalendarTzLabel = new JLabel("Timezone: ");
  private final JPanel monthGrid = new JPanel(new GridLayout(7, 7));
  private final JLabel selectedDateLabel = new JLabel("Select a date to view events");
  private final DefaultListModel<GuiEventSummary> eventsModel = new DefaultListModel<>();
  private final JList<GuiEventSummary> eventsList = new JList<>(eventsModel);
  private boolean suppressCalendarSelection = false;
  private CalendarGuiFeatures features;
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
    top.add(editEventBtn);
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

    eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JPanel east = new JPanel(new BorderLayout());
    east.add(selectedDateLabel, BorderLayout.NORTH);
    east.add(new JScrollPane(eventsList), BorderLayout.CENTER);
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
        if (features != null) {
          button.addActionListener(evt -> features.daySelected(date));
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
  public void displayEvents(LocalDate date, List<GuiEventSummary> events) {
    selectedDateLabel.setText("Events on " + date);
    eventsModel.clear();
    for (GuiEventSummary summary : events) {
      eventsModel.addElement(summary);
    }
    eventsList.setEnabled(!events.isEmpty());
  }

  @Override
  public Optional<EventCreationRequest> promptForCreateEvent(LocalDate date) {
    return new EventCreationDialog(this, date).show();
  }

  @Override
  public Optional<EventEditRequest> promptForEditEvent(GuiEventSummary summary) {
    return new EventEditDialog(this).show(summary.subject(), summary.start(), summary.end());
  }

  @Override
  public void setFeatures(CalendarGuiFeatures features) {
    if (this.features != null) {
      throw new IllegalStateException("Features already set.");
    }
    this.features = Objects.requireNonNull(features, "features cannot be null");

    prevMonthBtn.addActionListener(e -> this.features.goToPreviousMonth());
    nextMonthBtn.addActionListener(e -> this.features.goToNextMonth());
    createEventBtn.addActionListener(e -> this.features.requestEventCreation());
    editEventBtn.addActionListener(e -> {
      GuiEventSummary selected = eventsList.getSelectedValue();
      if (selected == null) {
        showError("Select an event to edit.");
      } else {
        this.features.requestEventEdit(selected);
      }
    });
    createCalendarBtn.addActionListener(e -> this.features.requestCalendarCreation());
    editCalendarBtn.addActionListener(e -> this.features.requestCalendarEdit());
    calendarSelector.addActionListener(e -> {
      if (suppressCalendarSelection) {
        return;
      }
      Object selected = calendarSelector.getSelectedItem();
      if (selected != null) {
        this.features.calendarSelected(selected.toString());
      }
    });

    registerKeyBindings();
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
    return new String[] {name, tz};
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
    suppressCalendarSelection = true;
    try {
      calendarSelector.setSelectedItem(name);
    } finally {
      suppressCalendarSelection = false;
    }
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
      return new String[] {calendarName, calendarName, calendarTz};
    }
    String newName = nameField.getText().trim();
    String newTz = Objects.requireNonNull((String) tzBox.getSelectedItem());
    return new String[] {calendarName, newName, newTz};
  }

  private void registerKeyBindings() {
    JComponent root = (JComponent) getContentPane();
    bindKeyStroke(root, "prev-month", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
        () -> features.goToPreviousMonth());
    bindKeyStroke(root, "next-month", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
        () -> features.goToNextMonth());
    bindKeyStroke(root, "create-event", KeyStroke.getKeyStroke(KeyEvent.VK_N, 0),
        () -> features.requestEventCreation());
    bindKeyStroke(root, "edit-event", KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), () -> {
      GuiEventSummary selected = eventsList.getSelectedValue();
      if (selected != null) {
        features.requestEventEdit(selected);
      }
    });
    bindKeyStroke(root, "create-calendar",
        KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK),
        () -> features.requestCalendarCreation());
    bindKeyStroke(root, "edit-calendar",
        KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),
        () -> features.requestCalendarEdit());
  }

  private void bindKeyStroke(JComponent root, String key, KeyStroke stroke, Runnable action) {
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, key);
    root.getActionMap().put(key, new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        if (features != null) {
          action.run();
        }
      }
    });
  }
}
