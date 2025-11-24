package calendar.model;

import calendar.model.domain.Event;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/**
 * Calendar with GUI features.
 */
public class GuiCalendar implements GuiCalendarInterface {
  private YearMonth currentMonth;
  private TimeZoneInMemoryCalendarInterface inUseCalendar;

  /**
   * Creates a Calendar with gui features.
   *
   * @param inUseCalendar calendar.
   *
   * @throws IllegalArgumentException if bad timezone or a non-unique name.
   */
  public GuiCalendar(TimeZoneInMemoryCalendarInterface inUseCalendar)
      throws IllegalArgumentException {
    this.inUseCalendar = inUseCalendar;

    initializeCurrentMonth();
  }

  /**
   * Initialize currentMonth based on earliest event or system month.
   * If no events just pick the current month.
   */
  private void initializeCurrentMonth() {
    List<Event> events = inUseCalendar.allEvents();

    if (!events.isEmpty()) {
      Event earliest = events.stream()
          .min(Comparator.comparing(Event::start))
          .orElse(null);

      if (earliest != null) {
        this.currentMonth = YearMonth.from(
            earliest.start().atZone(inUseCalendar.getZoneId())
        );
        return;
      }
    }

    this.currentMonth = YearMonth.now(inUseCalendar.getZoneId());
  }

  @Override
  public YearMonth getMonth() {
    return this.currentMonth;
  }

  @Override
  public YearMonth getNextMonth() {
    this.currentMonth = this.currentMonth.plusMonths(1);
    return this.currentMonth;
  }

  @Override
  public YearMonth getPreviousMonth() {
    this.currentMonth = this.currentMonth.minusMonths(1);
    return this.currentMonth;
  }

  @Override
  public String getName() {
    return inUseCalendar.getName();
  }

  @Override
  public String getZoneId() {
    return inUseCalendar.getZoneId().toString();
  }
}
