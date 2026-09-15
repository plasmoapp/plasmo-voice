package su.plo.lib.mod.client;

//#if MC>=26.3
//$$ import com.mojang.blaze3d.platform.InputConstants;
//$$
//$$ /** Converts SDL input to the codes used by saved hotkeys and mouse widgets. */
//$$ public final class SdlInput {
//$$     // SDL-only keys use a separate range so they cannot collide with saved GLFW key codes.
//$$     private static final int SDL_KEY_OFFSET = 1024;
//$$     private static final int[][] KEYS = {
//$$             {32, InputConstants.KEY_SPACE},
//$$             {39, InputConstants.KEY_APOSTROPHE},
//$$             {44, InputConstants.KEY_COMMA},
//$$             {45, InputConstants.KEY_MINUS},
//$$             {46, InputConstants.KEY_PERIOD},
//$$             {47, InputConstants.KEY_SLASH},
//$$             {48, InputConstants.KEY_0},
//$$             {59, InputConstants.KEY_SEMICOLON},
//$$             {61, InputConstants.KEY_EQUALS},
//$$             {91, InputConstants.KEY_LBRACKET},
//$$             {92, InputConstants.KEY_BACKSLASH},
//$$             {93, InputConstants.KEY_RBRACKET},
//$$             {96, InputConstants.KEY_GRAVE},
//$$             {161, 100}, // GLFW world 1 / SDL non-US backslash.
//$$             {162, 50}, // GLFW world 2 / SDL non-US hash.
//$$             {256, InputConstants.KEY_ESCAPE},
//$$             {257, InputConstants.KEY_RETURN},
//$$             {258, InputConstants.KEY_TAB},
//$$             {259, InputConstants.KEY_BACKSPACE},
//$$             {260, InputConstants.KEY_INSERT},
//$$             {261, InputConstants.KEY_DELETE},
//$$             {262, InputConstants.KEY_RIGHT},
//$$             {263, InputConstants.KEY_LEFT},
//$$             {264, InputConstants.KEY_DOWN},
//$$             {265, InputConstants.KEY_UP},
//$$             {266, InputConstants.KEY_PAGEUP},
//$$             {267, InputConstants.KEY_PAGEDOWN},
//$$             {268, InputConstants.KEY_HOME},
//$$             {269, InputConstants.KEY_END},
//$$             {280, InputConstants.KEY_CAPSLOCK},
//$$             {281, InputConstants.KEY_SCROLLLOCK},
//$$             {282, InputConstants.KEY_NUMLOCK},
//$$             {283, InputConstants.KEY_PRINTSCREEN},
//$$             {284, InputConstants.KEY_PAUSE},
//$$             {320, InputConstants.KEY_NUMPAD0},
//$$             {330, 99}, // Keypad decimal.
//$$             {331, 84}, // Keypad divide.
//$$             {332, InputConstants.KEY_MULTIPLY},
//$$             {333, 86}, // Keypad subtract.
//$$             {334, InputConstants.KEY_ADD},
//$$             {335, InputConstants.KEY_NUMPADENTER},
//$$             {336, InputConstants.KEY_NUMPADEQUALS},
//$$             {340, InputConstants.KEY_LSHIFT},
//$$             {341, InputConstants.KEY_LCONTROL},
//$$             {342, InputConstants.KEY_LALT},
//$$             {343, InputConstants.KEY_LGUI},
//$$             {344, InputConstants.KEY_RSHIFT},
//$$             {345, InputConstants.KEY_RCONTROL},
//$$             {346, InputConstants.KEY_RALT},
//$$             {347, InputConstants.KEY_RGUI},
//$$             {348, 101} // Application/menu key.
//$$     };
//$$
//$$     public static int toLegacyKey(int key) {
//$$         // GLFW key tokens and SDL scancodes both identify physical keys using the US layout.
//$$         // KeyEvent.keycode() is layout-dependent and must not be used for saved hotkeys.
//$$         if (key >= InputConstants.KEY_A && key <= InputConstants.KEY_Z) return key - InputConstants.KEY_A + 65;
//$$         if (key >= InputConstants.KEY_1 && key <= InputConstants.KEY_9) return key - InputConstants.KEY_1 + 49;
//$$         if (key >= InputConstants.KEY_F1 && key <= InputConstants.KEY_F12) return key - InputConstants.KEY_F1 + 290;
//$$         if (key >= InputConstants.KEY_F13 && key <= InputConstants.KEY_F24) return key - InputConstants.KEY_F13 + 302;
//$$         if (key >= InputConstants.KEY_NUMPAD1 && key <= InputConstants.KEY_NUMPAD9) return key - InputConstants.KEY_NUMPAD1 + 321;
//$$         for (int[] pair : KEYS) {
//$$             if (pair[1] == key) return pair[0];
//$$         }
//$$         return key == 0 ? -1 : SDL_KEY_OFFSET + key;
//$$     }
//$$
//$$     public static int toMinecraftKey(int key) {
//$$         if (key >= 65 && key <= 90) return key - 65 + InputConstants.KEY_A;
//$$         if (key >= 49 && key <= 57) return key - 49 + InputConstants.KEY_1;
//$$         if (key >= 290 && key <= 301) return key - 290 + InputConstants.KEY_F1;
//$$         if (key >= 302 && key <= 313) return key - 302 + InputConstants.KEY_F13;
//$$         if (key >= 321 && key <= 329) return key - 321 + InputConstants.KEY_NUMPAD1;
//$$         for (int[] pair : KEYS) {
//$$             if (pair[0] == key) return pair[1];
//$$         }
//$$         return key >= SDL_KEY_OFFSET ? key - SDL_KEY_OFFSET : 0;
//$$     }
//$$
//$$     public static int toLegacyMouseButton(int button) {
//$$         if (button == InputConstants.MOUSE_BUTTON_RIGHT) return 1;
//$$         if (button == InputConstants.MOUSE_BUTTON_MIDDLE) return 2;
//$$         return button - 1;
//$$     }
//$$
//$$     public static int toMinecraftMouseButton(int button) {
//$$         if (button == 1) return InputConstants.MOUSE_BUTTON_RIGHT;
//$$         if (button == 2) return InputConstants.MOUSE_BUTTON_MIDDLE;
//$$         return button + 1;
//$$     }
//$$
//$$     private SdlInput() {
//$$     }
//$$ }
//#endif
