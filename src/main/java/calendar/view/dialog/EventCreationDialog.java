package calendar.view.dialog;

import calendar.controller.service.EventCreationRequest;
import calendar.model.recurrence.Weekday;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Optional;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for creating single or recurring events. Returns a populated
 * {@link EventCreationRequest} or empty if cancelled/invalid.
 */
public class EventCreationDialog {
  private final Component parent;
  private final LocalDate defaultDate;

  /**
   * Creates a dialog for crafting new events.
   *
   * @param parent owner component for modal display.
   * @param defaultDate date selected in the GUI.
   */
  public EventCreationDialog(Component parent, LocalDate defaultDate) {
    this.parent = parent;
    this.defaultDate = defaultDate;
  }

  /**
   * Displays the dialog and returns the user's request, if any.
   */
  public Optional<EventCreationRequest> show() {
    final JTextField subjectField = new JTextField(20);
    final JTextField startField = new JTextField("09:00");
    final JTextField endField = new JTextField("10:00");

    final JComboBox<EventCreationRequest.Pattern> patternBox =
        new JComboBox<>(EventCreationRequest.Pattern.values());

    DayOfWeek[] weekdays = DayOfWeek.values();
    JCheckBox[] weekdayBoxes = new JCheckBox[weekdays.length];
    JPanel weekdaysPanel = new JPanel(new GridLayout(1, 7));
    for (int i = 0; i < weekdays.length; i++) {
      weekdayBoxes[i] = new JCheckBox(weekdays[i].name().substring(0, 3));
      weekdaysPanel.add(weekdayBoxes[i]);
    }

    final JSpinner occurrencesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 500, 1));
    final JTextField untilDateField = new JTextField(defaultDate.plusWeeks(1).toString());

    JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.add(new JLabel("Creating event on " + defaultDate));
    panel.add(new JLabel("Pattern:"));
    panel.add(patternBox);
    panel.add(new JLabel("Subject:"));
    panel.add(subjectField);
    panel.add(new JLabel("Start time (HH:mm):"));
    panel.add(startField);
    panel.add(new JLabel("End time (HH:mm):"));
    panel.add(endField);
    panel.add(new JLabel("Weekdays:"));
    panel.add(weekdaysPanel);
    panel.add(new JLabel("Occurrences:"));
    panel.add(occurrencesSpinner);
    panel.add(new JLabel("Until date (yyyy-MM-dd):"));
    panel.add(untilDateField);

    patternBox.addActionListener(e -> toggleInputs(patternBox, startField, endField,
        weekdayBoxes, occurrencesSpinner, untilDateField));
    toggleInputs(patternBox, startField, endField, weekdayBoxes,
        occurrencesSpinner, untilDateField);

    int result = JOptionPane.showConfirmDialog(
        parent, panel, "New Event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return Optional.empty();
    }

    String subject = subjectField.getText().trim();
    if (subject.isEmpty()) {
      showError("Subject cannot be blank.");
      return Optional.empty();
    }

    try {
      EventCreationRequest.Pattern choice =
          (EventCreationRequest.Pattern) patternBox.getSelectedItem();
      return Optional.of(buildRequest(
          choice, subject, startField.getText().trim(), endField.getText().trim(),
          weekdayBoxes, weekdays, occurrencesSpinner, untilDateField));
    } catch (IllegalArgumentException | DateTimeParseException ex) {
      showError(ex.getMessage());
      return Optional.empty();
    }
  }

  private void toggleInputs(JComboBox<EventCreationRequest.Pattern> patternBox,
                            JTextField startField,
                            JTextField endField,
                            JCheckBox[] weekdayBoxes,
                            JSpinner occurrencesSpinner,
                            JTextField untilDateField) {
    EventCreationRequest.Pattern choice =
        (EventCreationRequest.Pattern) patternBox.getSelectedItem();
    final boolean timed = choice == EventCreationRequest.Pattern.SINGLE_TIMED
        || choice == EventCreationRequest.Pattern.RECURRING_TIMED_COUNT
        || choice == EventCreationRequest.Pattern.RECURRING_TIMED_UNTIL;

    final boolean recurring = choice == EventCreationRequest.Pattern.RECURRING_TIMED_COUNT
        || choice == EventCreationRequest.Pattern.RECURRING_TIMED_UNTIL
        || choice == EventCreationRequest.Pattern.RECURRING_ALL_DAY_COUNT
        || choice == EventCreationRequest.Pattern.RECURRING_ALL_DAY_UNTIL;

    final boolean requiresCount = choice == EventCreationRequest.Pattern.RECURRING_TIMED_COUNT
        || choice == EventCreationRequest.Pattern.RECURRING_ALL_DAY_COUNT;
    final boolean requiresUntil = choice == EventCreationRequest.Pattern.RECURRING_TIMED_UNTIL
        || choice == EventCreationRequest.Pattern.RECURRING_ALL_DAY_UNTIL;

    startField.setEnabled(timed);
    endField.setEnabled(timed);
    for (JCheckBox box : weekdayBoxes) {
      box.setEnabled(recurring);
      if (!recurring) {
        box.setSelected(false);
      }
    }
    occurrencesSpinner.setEnabled(requiresCount);
    untilDateField.setEnabled(requiresUntil);
  }

  private EventCreationRequest buildRequest(EventCreationRequest.Pattern pattern,
                                            String subject,
                                            String startText,
                                            String endText,
                                            JCheckBox[] weekdayBoxes,
                                            DayOfWeek[] weekdays,
                                            JSpinner occurrencesSpinner,
                                            JTextField untilDateField) {
    EventCreationRequest.Builder builder = new EventCreationRequest.Builder()
        .pattern(pattern)
        .subject(subject);

    switch (pattern) {
      case SINGLE_TIMED:
        LocalTime startTime = LocalTime.parse(startText);
        LocalTime endTime = LocalTime.parse(endText);
        builder.startDateTime(LocalDateTime.of(defaultDate, startTime));
        builder.endDateTime(LocalDateTime.of(defaultDate, endTime));
        break;
      case SINGLE_ALL_DAY:
        builder.allDayDate(defaultDate);
        break;
      case RECURRING_TIMED_COUNT:
        builder.startDateTime(LocalDateTime.of(defaultDate, LocalTime.parse(startText)));
        builder.endDateTime(LocalDateTime.of(defaultDate, LocalTime.parse(endText)));
        builder.weekdays(selectedWeekdays(weekdayBoxes, weekdays));
        builder.occurrences((Integer) occurrencesSpinner.getValue());
        break;
      case RECURRING_TIMED_UNTIL:
        builder.startDateTime(LocalDateTime.of(defaultDate, LocalTime.parse(startText)));
        builder.endDateTime(LocalDateTime.of(defaultDate, LocalTime.parse(endText)));
        builder.weekdays(selectedWeekdays(weekdayBoxes, weekdays));
        LocalDate untilTimed = LocalDate.parse(untilDateField.getText().trim());
        if (untilTimed.isBefore(defaultDate)) {
          throw new IllegalArgumentException("Until date must be on or after " + defaultDate);
        }
        builder.untilDate(untilTimed);
        break;
      case RECURRING_ALL_DAY_COUNT:
        builder.allDayDate(defaultDate);
        builder.weekdays(selectedWeekdays(weekdayBoxes, weekdays));
        builder.occurrences((Integer) occurrencesSpinner.getValue());
        break;
      case RECURRING_ALL_DAY_UNTIL:
        builder.allDayDate(defaultDate);
        builder.weekdays(selectedWeekdays(weekdayBoxes, weekdays));
        LocalDate until = LocalDate.parse(untilDateField.getText().trim());
        if (until.isBefore(defaultDate)) {
          throw new IllegalArgumentException("Until date must be on or after " + defaultDate);
        }
        builder.untilDate(until);
        break;
      default:
        break;
    }
    return builder.build();
  }

  private EnumSet<Weekday> selectedWeekdays(JCheckBox[] boxes, DayOfWeek[] days) {
    EnumSet<Weekday> set = EnumSet.noneOf(Weekday.class);
    for (int i = 0; i < boxes.length; i++) {
      if (boxes[i].isSelected()) {
        set.add(Weekday.from(days[i]));
      }
    }
    if (set.isEmpty()) {
      throw new IllegalArgumentException("Select at least one weekday for recurring events.");
    }
    return set;
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(parent, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
  }
}
