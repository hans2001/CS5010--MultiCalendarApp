package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

/**
 * Simple coverage tests for EditProperty.from to ensure all mappings are handled.
 */
public final class EditPropertyTest {

  @Test
  public void fromParsesEndProperty() {
    Optional<EditProperty> result = EditProperty.from("end");
    assertTrue(result.isPresent());
    assertEquals(EditProperty.END, result.get());
  }
}
