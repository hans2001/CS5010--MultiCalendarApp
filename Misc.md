List of Changes
- Addition of GuiCalendar.
  - Our original calendar model did not support month traversal,
    which is required for the GUI (such as moving forward/backward through months).

  - To extend functionality without modifying the existing calendar model, 
    we introduced a new GuiCalendar class.

  - GuiCalendar uses composition by internally holding a reference to the existing calendar model. 
    This lets it reuse all existing logic but add GUI functionality on top.

  - This design follows the Open/Closed Principle.
    We extended the behavior of the system without modifying the underlying model.
- Command Pattern for handling Calendar Functionality 
  - Implemented the Command Pattern to handle calendar actions in a uniform way.
  - Encapsulated each user action into a separate command object, improving 
    organization and separating the UI logic from controller logic.
  - Enables easier addition of new actions and supports the Open/Closed Principle, 
    since new commands can be added without modifying existing logic.