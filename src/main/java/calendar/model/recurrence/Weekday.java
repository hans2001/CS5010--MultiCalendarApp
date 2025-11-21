package calendar.model.recurrence;

/**
 * Weekday letters used by the assignment:
 * M=Mon, T=Tue, W=Wed, R=Thu, F=Fri, S=Sat, U=Sun.
 */
public enum Weekday {
  M, T, W, R, F, S, U;

  /**
   * Converts a {@link java.time.DayOfWeek} to the matching {@link Weekday}.
   */
  public static Weekday from(java.time.DayOfWeek dayOfWeek) {
    switch (dayOfWeek) {
      case MONDAY:
        return M;
      case TUESDAY:
        return T;
      case WEDNESDAY:
        return W;
      case THURSDAY:
        return R;
      case FRIDAY:
        return F;
      case SATURDAY:
        return S;
      case SUNDAY:
        return U;
      default:
        throw new IllegalArgumentException("Unknown DayOfWeek: " + dayOfWeek);
    }
  }
}
