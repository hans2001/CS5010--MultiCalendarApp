# Calendar Application - User Guide

## Building the Application

To build the JAR file:

```bash
./gradlew jar
```

This creates `build/libs/calendar-1.0.jar`

## Running the Application

The application supports two modes: **interactive** and **headless**.

### Interactive Mode

Run the calendar with an interactive command prompt:

```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```

You'll see a prompt where you can enter commands one at a time. Type `exit` to quit.

**Example session:**
```text
Welcome to Calendar. Type 'exit' to quit.
Enter a command: create event "Team Meeting" from 2025-05-05T10:00 to 2025-05-05T11:00
Event created successfully.
Enter a command: print events on 2025-05-05
Events on 2025-05-05:
- Team Meeting from 10:00 to 11:00

Enter a command: exit
```

### Headless Mode

Execute commands from a file:

```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/commands.txt
```

The file must end with an `exit` command. Each command executes in order, and results are printed to stdout.

**Note:** Mode names are case-insensitive (`--mode INTERACTIVE` works too).

## Command Reference

### Creating Events

**Single event with times:**
```text
create event "Team Meeting" from 2025-05-05T10:00 to 2025-05-05T11:00
```

**All-day event:**
```text
create event "Holiday" on 2025-12-25
```

**Recurring event (count-based):**
```text
create event "Daily Standup" from 2025-05-05T09:00 to 2025-05-05T09:15 repeats MTWRF for 10 times
```

**Recurring event (date-based):**
```text
create event "Weekly Review" from 2025-05-05T16:00 to 2025-05-05T17:00 repeats F until 2025-06-30
```

**Weekday codes:** M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday

### Editing Events

**Edit single instance:**
```text
edit event subject "Team Meeting" from 2025-05-05T10:00 to 2025-05-05T11:00 with "Sprint Planning"
edit event start "Sprint Planning" from 2025-05-05T10:00 to 2025-05-05T11:00 with 2025-05-05T10:30
```

**Edit this and following instances:**
```text
edit events subject "Daily Standup" from 2025-05-08T09:00 with "Morning Sync"
```

**Edit entire series:**
```text
edit series start "Weekly Review" from 2025-05-09T16:00 with 2025-05-09T15:00
```
Note: Use the date of an actual event instance. If the series repeats on Fridays starting 2025-05-05, the first event is on 2025-05-09 (the first Friday).

**Editable properties:** subject, start, end, description, location, status

### Querying the Calendar

**Events on a specific date:**
```text
print events on 2025-05-05
```

**Events in a time range:**
```text
print events from 2025-05-05T00:00 to 2025-05-10T23:59
```

**Check availability:**
```text
show status on 2025-05-05T10:30
```
Output: `busy` or `available`

### Exporting

**Export to CSV (importable to Google Calendar):**
```text
export cal my_calendar.csv
```

The absolute path to the created file will be printed.

## Example Workflows

### Example 1: Creating a Series and Modifying It

```bash
# Create 6 events on Mondays and Wednesdays
create event "First" from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times

# Change subject of May 12 and following events
edit events subject "First" from 2025-05-12T10:00 with "Second"

# Change start time of May 12 and following (splits the series)
edit events start "Second" from 2025-05-12T10:00 with 2025-05-12T10:30

# Now events on May 5 and 7 still have subject "First" at 10:00
# Events on May 12, 14, 19, 21 have subject "Second" at 10:30
```

### Example 2: All-Day Events

```bash
# Create all-day event (8am-5pm by default)
create event "Team Offsite" on 2025-06-15

# Create recurring all-day events
create event "Weekend Cleanup" on 2025-05-03 repeats S for 4 times
```

### Example 3: Checking for Conflicts

```bash
create event "Meeting A" from 2025-05-05T10:00 to 2025-05-05T11:00
# Output: Event created successfully.

show status on 2025-05-05T10:30
# Output: busy

show status on 2025-05-05T11:30
# Output: available
```

## Notes

- All times are in EST timezone
- Event subjects with spaces must be in double quotes
- Two events cannot have the same (subject, start, end) combination
- Series instances must start and end on the same day
- When editing series start times, affected events detach from the original series
- File paths are platform-independent

## Error Messages

- **"Subject is required"** - Event needs a non-empty subject
- **"Duplicate event (subject/start/end) exists"** - Uniqueness violation
- **"No matching event found"** - Edit command couldn't find target event
- **"Ambiguous selector: multiple events share subject/start; specify end"** - Multiple events match selector (add end time to disambiguate)
- **"Invalid command"** / **"Invalid create event command format"** - Syntax error in command
- **"Fields are invalid"** - Validation error (e.g., end before start)
- **"Event conflict"** / **"Update failed due to event conflict"** - Would create duplicate event

## Testing

Sample command files are provided in `res/`:
- `res/commands.txt` - Valid commands for testing
- `res/invalid.txt` - Invalid commands to test error handling

Run tests:
```bash
./gradlew test
```

View test coverage report:
```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

