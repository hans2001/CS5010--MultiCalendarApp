package calendar.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Class for the GUI.
 */
public class CalendarGuiView extends JFrame implements CalendarGuiViewInterface {
  private JButton nextMonthBtn;
  private JButton prevMonthBtn;
  private JButton createEventBtn;
  private JButton createCalendarBtn;
  private JLabel monthYearTitle;

  private JPanel monthGrid;
  private JLabel activeCalendarLabel;

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
        monthGrid.add(new JLabel(String.valueOf(day), JLabel.CENTER));
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

  /**
   * Change the active calendar title.
   *
   * @param name of new active calendar.
   */
  public void setActiveCalendarName(String name) {
    activeCalendarLabel.setText("Current Calendar: " + name);
  }
}
