### Beta
This is a beta version, and it still requires testing, especially the voice playback changes.
If you encounter any issues, please report them on [GitHub](https://github.com/plasmoapp/plasmo-voice/issues) or [Discord](https://discord.gg/uueEqzwCJJ).

Versions 2.1.x and 2.2.x are protocol-compatible,
so there's no need to worry if server or client hasn't been updated to 2.2.x.


### Client
- Microphone now works on macOS with launchers that can't ask for microphone permission (e.g. official Minecraft launcher). [#527](https://github.com/plasmoapp/plasmo-voice/pull/527)
  - With such launchers, Plasmo Voice asks for permission itself through bundled `Plasmo Voice Microphone` helper app.
  - Huge thanks to [@arnodoelinger](https://github.com/arnodoelinger) for implementing and testing it!
- Microphone and output devices are now opened on a separate thread, so game doesn't freeze while device is opening (e.g. when switching devices or joining a server).
- `Inherit` activation type now shows by which activation it activates in the dropdown tooltip: https://i.imgur.com/QOQb5PW.png.
- Settings tabs now use consistent spacing between elements.
  - If you're addon developer and use internal Plasmo Voice GUI classes, see [#546](https://github.com/plasmoapp/plasmo-voice/pull/546) and [these commits](https://github.com/plasmoapp/plasmo-voice/compare/cf6f60ab3546442d1916361e57f23a8d2f2e3b37...329247629aec2f557b34eeb298031ac6626e4c11) for changes.
- Rendering was heavily refactored internally.
  - If you notice any rendering issues with GUI or source icons, please report them on [GitHub](https://github.com/plasmoapp/plasmo-voice/issues).
  - If you're addon developer and use internal Plasmo Voice rendering classes, see [#546](https://github.com/plasmoapp/plasmo-voice/pull/546) for changes.
- Fixed startup crash on NeoForge 1.21 (1.21.1 wasn't affected). [#544](https://github.com/plasmoapp/plasmo-voice/issues/544)
- Fixed crash with `Improved Transparency` turned on when rendering a distance sphere in 26.3.
- Fixed distance sphere not rendering with Iris shaders enabled.
- Fixed GUI elements showing hover effect while dragging scrollbar.
- Fixed overlay and volume percentage above players not respecting `Text Background Opacity`.
- Fixed text losing formatting in GUI, tooltips and overlay (e.g. hex colors or combined styles like `§k§l` in source name).
- Fixed volume percentage above players having different brightness than the name tag.
- Fixed `Esc` not closing the settings screen on the first press after binding a hotkey or returning from the HUD position screen.
- Fixed `Connecting to the UDP server...` screen sometimes not switching to the settings after UDP connects.

### Server
