package calendar.model.api;

/**
 * Defines the scope for editing events in a recurring series:
 * - SINGLE: Only the selected instance.
 * - FOLLOWING: The selected instance and all following instances in its series.
 * - ENTIRE_SERIES: All instances in the series.
 */
public enum EditScope {
  SINGLE,
  FOLLOWING,
  ENTIRE_SERIES
}
