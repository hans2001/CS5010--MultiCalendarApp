package calendar.view.dialog;

import calendar.controller.EditProperty;
import calendar.controller.service.EventEditRequest;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for editing an existing event.
 */
public class EventEditDialog {
  private final Component parent;

  /**
   * Creates a dialog for editing events using the provided parent component.
   *
   * @param parent owner used for positioning modal dialogs.
   */
  public EventEditDialog(Component parent) {
    this.parent = parent;
  }

  /**
   * Prompts the user for edit details.
   *
   * @return populated request if confirmed; empty otherwise.
   */
  public Optional<EventEditRequest> show(String subject,
                                         java.time.LocalDateTime start,
                                         java.time.LocalDateTime end) {
    JComboBox<EditProperty> propertyBox = new JComboBox<>(EditProperty.values());
    JTextField valueField = new JTextField(20);
    EditScopeChoice[] scopes = EditScopeChoice.values();
    JComboBox<EditScopeChoice> scopeBox = new JComboBox<>(scopes);

    JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.add(new JLabel("Editing event: " + subject));
    panel.add(new JLabel("Property:"));
    panel.add(propertyBox);
    panel.add(new JLabel("New value:"));
    panel.add(valueField);
    panel.add(new JLabel("Scope:"));
    panel.add(scopeBox);

    int result = JOptionPane.showConfirmDialog(parent, panel, "Edit Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return Optional.empty();
    }

    String newValue = valueField.getText().trim();
    if (newValue.isEmpty()) {
      JOptionPane.showMessageDialog(parent, "Value cannot be blank.",
          "Invalid Input", JOptionPane.ERROR_MESSAGE);
      return Optional.empty();
    }

    EditProperty property = (EditProperty) propertyBox.getSelectedItem();
    EditScopeChoice scopeChoice = (EditScopeChoice) scopeBox.getSelectedItem();

    EventEditRequest.Builder builder = new EventEditRequest.Builder()
        .property(property)
        .subject(subject)
        .start(start)
        .newValue(newValue)
        .scope(scopeChoice.scope);

    if (scopeChoice.requiresEnd) {
      builder.end(end);
    }

    return Optional.of(builder.build());
  }

  /**
   * Helper to map UI scope labels to EditScope.
   */
  private enum EditScopeChoice {
    SINGLE("Single instance", calendar.model.api.EditScope.SINGLE, true),
    FOLLOWING("Following", calendar.model.api.EditScope.FOLLOWING, false),
    ENTIRE("Entire series", calendar.model.api.EditScope.ENTIRE_SERIES, false);

    final String label;
    final calendar.model.api.EditScope scope;
    final boolean requiresEnd;

    EditScopeChoice(String label,
                    calendar.model.api.EditScope scope,
                    boolean requiresEnd) {
      this.label = label;
      this.scope = scope;
      this.requiresEnd = requiresEnd;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
