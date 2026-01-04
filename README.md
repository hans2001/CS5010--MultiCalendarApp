# CS5010 MultiCalendarApp

A desktop calendar application that helps users create, edit, and view events across multiple calendars and time zones. Built in Java with a Swing-based UI and an MVC architecture, the project combines an intuitive GUI with scriptable and interactive text modes.

## Why this project

This project demonstrates how to design a user-friendly scheduling tool while keeping the codebase modular and testable. It highlights UI design in Swing, controller-driven workflows, and robust model logic for recurring events and time zones.

## What I built

- Multi-calendar support with clear calendar identification
- Month view navigation with day-level event lists
- Single and recurring event creation and editing
- Time zone awareness for accurate scheduling
- Three usage modes: GUI, interactive CLI, and headless script execution
- Graceful validation and error messages for invalid inputs

## Technical skills demonstrated

- Java and object-oriented design
- Swing UI development and event handling
- MVC separation of concerns
- Input validation and user-friendly error handling
- Command-line parsing and script-driven workflows
- Gradle build tooling and JAR packaging

## Architecture (high level)

- Model: calendars, events, recurrence rules, and time zone logic
- View: Swing-based GUI with month navigation and event details
- Controller: routes user actions in GUI and CLI modes to the model

## Run it

1. Build a JAR:
   `./gradlew jar`
2. Launch the GUI:
   `java -jar build/libs/<JAR_NAME>.jar`
3. Interactive CLI mode:
   `java -jar build/libs/<JAR_NAME>.jar --mode interactive`
4. Headless script mode:
   `java -jar build/libs/<JAR_NAME>.jar --mode headless <script-file>`

## What you can do in the app

- Create new calendars in different time zones
- Select a day to view all events scheduled for that date
- Add single or recurring events (weekly patterns and end conditions)
- Edit one event or a matching group of events
