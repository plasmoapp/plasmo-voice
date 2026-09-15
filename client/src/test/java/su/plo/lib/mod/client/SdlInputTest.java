package su.plo.lib.mod.client;

//#if MC>=26.3
//$$ import com.mojang.blaze3d.platform.InputConstants;
//$$ import net.minecraft.client.KeyMapping;
//$$ import net.minecraft.client.input.InputQuirks;
//$$ import net.minecraft.client.input.KeyEvent;
//$$ import org.junit.jupiter.api.Test;
//$$
//$$ import static org.junit.jupiter.api.Assertions.assertEquals;
//$$ import static org.junit.jupiter.api.Assertions.assertFalse;
//$$ import static org.junit.jupiter.api.Assertions.assertTrue;
//$$
//$$ class SdlInputTest {
//$$     @Test
//$$     void editingShortcutsUseNativeGuiKeys() {
//$$         int modifiers = InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER;
//$$         assertTrue(Inputs.isSelectAll(InputConstants.KEY_A, modifiers));
//$$         assertTrue(Inputs.isCopy(InputConstants.KEY_C, modifiers));
//$$         assertTrue(Inputs.isPaste(InputConstants.KEY_V, modifiers));
//$$         assertTrue(Inputs.isCut(InputConstants.KEY_X, modifiers));
//$$
//$$         // Persisted GLFW values identify different physical keys in SDL GUI events.
//$$         assertFalse(Inputs.isSelectAll(65, modifiers));
//$$         assertFalse(Inputs.isCopy(67, modifiers));
//$$         assertFalse(Inputs.isPaste(86, modifiers));
//$$         assertFalse(Inputs.isCut(88, modifiers));
//$$         assertFalse(Inputs.isPaste(InputConstants.KEY_V, 0));
//$$         assertFalse(Inputs.isPaste(InputConstants.KEY_V, modifiers | InputConstants.MOD_SHIFT));
//$$         assertFalse(Inputs.isPaste(InputConstants.KEY_V, modifiers | InputConstants.MOD_ALT));
//$$     }
//$$
//$$     @Test
//$$     void nativeMenuBindingAndSavedHotkeysUseDifferentKeyCodes() {
//$$         KeyMapping menuKey = new KeyMapping(
//$$                 "key.plasmovoice.test.settings", InputConstants.KEY_V, KeyMapping.Category.MISC
//$$         );
//$$         KeyEvent event = new KeyEvent(InputConstants.KEY_V, InputConstants.KEYCODE_V, 0);
//$$         assertTrue(menuKey.matches(event));
//$$
//$$         int savedKeyCode = SdlInput.toLegacyKey(event.key());
//$$         assertEquals(86, savedKeyCode);
//$$         assertFalse(menuKey.matches(new KeyEvent(savedKeyCode, event.keycode(), event.modifiers())));
//$$         assertTrue(menuKey.matches(new KeyEvent(SdlInput.toMinecraftKey(savedKeyCode), 0, 0)));
//$$     }
//$$
//$$     @Test
//$$     void preservesSavedHotkeysAndMenuKey() {
//$$         assertEquals(86, SdlInput.toLegacyKey(new KeyEvent(25, 118, 0).key())); // V.
//$$         assertEquals(342, SdlInput.toLegacyKey(226)); // Left Alt / default push to talk.
//$$         assertEquals(77, SdlInput.toLegacyKey(16)); // M / microphone mute.
//$$         assertEquals(25, SdlInput.toMinecraftKey(86));
//$$         assertEquals(226, SdlInput.toMinecraftKey(342));
//$$         assertEquals(16, SdlInput.toMinecraftKey(77));
//$$     }
//$$
//$$     @Test
//$$     void preservesPhysicalKeysOnOtherKeyboardLayouts() {
//$$         KeyEvent germanZ = new KeyEvent(28, 122, 0);
//$$         assertEquals(89, SdlInput.toLegacyKey(germanZ.key()));
//$$         assertEquals(256, SdlInput.toLegacyKey(41)); // Escape.
//$$         assertEquals(258, SdlInput.toLegacyKey(43)); // Tab.
//$$         assertEquals(335, SdlInput.toLegacyKey(88)); // Keypad Enter.
//$$     }
//$$
//$$     @Test
//$$     void preservesMouseBindingsAndButtonOrder() {
//$$         assertEquals(0, SdlInput.toLegacyMouseButton(1)); // Left.
//$$         assertEquals(1, SdlInput.toLegacyMouseButton(3)); // Right.
//$$         assertEquals(2, SdlInput.toLegacyMouseButton(2)); // Middle.
//$$         assertEquals(3, SdlInput.toLegacyMouseButton(4)); // Back.
//$$         assertEquals(4, SdlInput.toLegacyMouseButton(5)); // Forward.
//$$         for (int button = 1; button <= 8; button++) {
//$$             assertEquals(button, SdlInput.toMinecraftMouseButton(SdlInput.toLegacyMouseButton(button)));
//$$         }
//$$     }
//$$
//$$     @Test
//$$     void recognizesBothSidesOfSdlModifiers() {
//$$         assertTrue(Inputs.hasShiftDown(1));
//$$         assertTrue(Inputs.hasShiftDown(2));
//$$         assertFalse(Inputs.hasShiftDown(64));
//$$         assertTrue(Inputs.hasAltDown(256));
//$$         assertTrue(Inputs.hasAltDown(512));
//$$         assertFalse(Inputs.hasAltDown(2));
//$$     }
//$$
//$$     @Test
//$$     void supportsSdlOnlyKeysWithoutCollidingWithSavedKeys() {
//$$         assertTrue(SdlInput.toLegacyKey(263) > 348); // Media Play.
//$$         assertEquals(263, SdlInput.toMinecraftKey(SdlInput.toLegacyKey(263)));
//$$         assertEquals(-1, SdlInput.toLegacyKey(0));
//$$         assertEquals(0, SdlInput.toMinecraftKey(-1));
//$$         for (int key = 1; key < 512; key++) {
//$$             assertEquals(key, SdlInput.toMinecraftKey(SdlInput.toLegacyKey(key)));
//$$         }
//$$     }
//$$ }
//#endif
