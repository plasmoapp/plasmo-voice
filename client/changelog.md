### Beta
This is a beta version, and it still requires testing, especially the voice playback changes.
If you encounter any issues, please report them on [GitHub](https://github.com/plasmoapp/plasmo-voice/issues) or [Discord](https://discord.gg/uueEqzwCJJ).

Versions 2.1.x and 2.2.x are protocol-compatible,
so there's no need to worry if server or client hasn't been updated to 2.2.x.

---

Dropped support for Forge and Minecraft versions below 1.21. Plasmo Voice now only supports Fabric and NeoForge on 1.21 and newer.

### Client
- `Volume` tab now shows only source lines used in the last 5 minutes when there are more than 3 of them (`Proximity` always shown). Can be toggled to show all via button next to `Sources Volume`.
- Entity icons are now rendered on any entity, not only on living entities.
  - Display entities now show the icon even when they're empty, and their transformation is applied to the icon and audio position, so they can be used as an anchor for audio and icon.
- Improved voice playback:
  - Fixed the start of the audio stream sometimes being cut off when you hear audio source for the first time.
  - Fixed the last moment of audio stream sometimes being cut off.
  - Fixed the end of audio stream sometimes being lost, or played late at the start of the next stream.
  - Fixed a short piece of already played audio sometimes being repeated.
  - Fixed a short silence in the middle of the audio stream with proximity sources when changing worlds.
  - Removed `Adaptive Jitter Buffer`, because it didn't work properly.
- Activation settings in the client config are now stored by activation name instead of UUID.
- Fixed Opus `mode` and `mtu_size` from the server config being ignored by the native encoder (it always used `VOIP`).
- Fixed HUD icons and overlay not rendering when there are a lot of HUD layers on NeoForge. [#542](https://github.com/plasmoapp/plasmo-voice/issues/542)
- Fixed settings screen resetting its scroll position and search input when window is resized.
- Fixed overlay settings for source lines sometimes being reset on join.
- Fixed `ClientSourceLine#getVolume` returning the max volume instead of the actual volume.

### Server
- Commands now use Brigadier:
  - `/vmute` now requires duration before reason: `/vmute <player> permanent <reason>` instead of `/vmute <player> <reason>`.
  - `/vmute` now rejects invalid durations (`0`, `10x`, overflowing values, `u` timestamps in the past) instead of treating them as part of the reason and muting permanently.
  - `/vmute` and `/vunmute` now accept target selectors, e.g. `/vmute @a[distance=..10] 5m`.
  - `/vunmute` now accepts the UUID of a muted player even when their name can't be resolved.
  - `/vrc` now uses actual command executor as reconnect target, so `/execute as <player> run vrc` will reconnect `<player>`.
- Updated slib to 2.1.0, which breaks addons that use:
  - `McCommandManager#register` with Brigadier commands. Register them with `McBrigadierRegistry` from `McBrigadierCommandsRegisterEvent` listener instead.
  - `MessageTextConverter`. Use `McTextMessage.of(component)` instead.
- Added vanish support, supported mods: [Melius Vanish](https://www.curseforge.com/minecraft/mc-mods/meliusvanish) on Fabric and [Vanishmod](https://www.curseforge.com/minecraft/mc-mods/vanishmod) on NeoForge.
- Added optional SO_REUSEPORT support (`[voice.reuse_port]` in `config.toml`, Linux only). When enabled, multiple sockets on separate threads share the voice port instead of one.
- Number of UDP threads can now be set with `-Dplasmovoice.udp_threads` (default: CPU cores * 2).
- UDP server now copies fewer buffers, batches socket writes and doesn't create packet events when nothing listens to them.
  - In our benchmark, one socket now sends ~1.2x more packets than 2.1.x before it starts dropping, and allocates ~10x less memory per sent packet.
  - With `reuse_port`, 100 players speaking at the same spot (~500k outgoing packets/s) are handled without loss on 4 cores.
- Fixed Plasmo Voice permissions not being visible to permission mods (e.g. LuckPerms) on NeoForge.
- Fixed Opus `mode` and `mtu_size` from the config being ignored by the native encoder (it always used `VOIP`).
