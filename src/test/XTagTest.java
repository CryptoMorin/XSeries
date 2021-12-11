import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import org.junit.jupiter.api.Test;

/**
 * Simple integration tests to ensure the XTag wrapper properly works.
 */
class XTagTest {

  @Test
  void checkNonInheritTag() {
    final XMaterial target = XMaterial.TUBE_CORAL;

    assertTrue(XTag.CORAL_PLANTS.isTagged(target));
  }

  @Test
  void checkInheritTag() {
    final XMaterial targetInheritOne = XMaterial.STRIPPED_ACACIA_LOG;
    final XMaterial targetInheritTwo = XMaterial.STRIPPED_OAK_LOG;

    assertTrue(XTag.LOGS_THAT_BURN.isTagged(targetInheritOne));
    assertTrue(XTag.LOGS_THAT_BURN.isTagged(targetInheritTwo));
  }
}
