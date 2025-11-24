package calendar.controller.guicommands;

import calendar.controller.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.GuiCalendarInterface;
import calendar.view.CalendarGuiViewInterface;

/**
 * Bundles the GUI controller, view, manager, and current calendar for command implementations.
 */
public final class CalendarGuiCommandContext {
  private final CalendarManager manager;
  private final GuiCalendarInterface currentCalendar;
  private final CalendarGuiController controller;
  private final CalendarGuiViewInterface view;

  /**
   * Creates a context with the shared GUI components.
   */
  public CalendarGuiCommandContext(CalendarManager manager,
                                   GuiCalendarInterface currentCalendar,
                                   CalendarGuiController controller,
                                   CalendarGuiViewInterface view) {
    this.manager = manager;
    this.currentCalendar = currentCalendar;
    this.controller = controller;
    this.view = view;
  }

  /**
   * Returns the underlying calendar manager.
   */
  public CalendarManager manager() {
    return manager;
  }

  /**
   * Returns the calendar currently shown in the GUI.
   */
  public GuiCalendarInterface currentCalendar() {
    return currentCalendar;
  }

  /**
   * Returns the owning controller.
   */
  public CalendarGuiController controller() {
    return controller;
  }

  /**
   * Returns the GUI view.
   */
  public CalendarGuiViewInterface view() {
    return view;
  }
}
