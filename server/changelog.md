### Beta
This is a beta version, and it still requires testing.
If you encounter any issues, please report them on [GitHub](https://github.com/plasmoapp/plasmo-voice/issues) or [Discord](https://discord.gg/uueEqzwCJJ).

Versions 2.1.x and 2.2.x are protocol-compatible,
so there's no need to worry if server or client hasn't been updated to 2.2.x.

---

- Plasmo Voice is now a Paper plugin. Spigot is no longer supported, use Paper or Folia 1.21+.
- Commands now use Brigadier:
  - `/vmute` now requires duration before reason: `/vmute <player> permanent <reason>` instead of `/vmute <player> <reason>`.
  - `/vmute` now rejects invalid durations (`0`, `10x`, overflowing values, `u` timestamps in the past) instead of treating them as part of the reason and muting permanently.
  - `/vmute` and `/vunmute` now accept target selectors, e.g. `/vmute @a[distance=..10] 5m`.
  - `/vunmute` now accepts the UUID of a muted player even when their name can't be resolved.
  - `/vrc` now uses actual command executor as reconnect target, so `/execute as <player> run vrc` will reconnect `<player>`.
  - Commands can now be used in datapack functions without macros.
- Updated slib to 2.1.0, which breaks addons that use:
  - `McCommandManager#register` with Brigadier commands. Register them with `McBrigadierRegistry` from `McBrigadierCommandsRegisterEvent` listener instead.
  - `MessageTextConverter`. Use `McTextMessage.of(component)` instead.
- Vanish is now alwayts handled through Paper's player visibility API, so direct SuperVanish/PremiumVanish integration was removed.
- Added optional SO_REUSEPORT support (`[voice.reuse_port]` in `config.toml`, Linux only). When enabled, multiple sockets on separate threads share the voice port instead of one.
- Number of UDP threads can now be set with `-Dplasmovoice.udp_threads` (default: CPU cores * 2).
- Number of UDP threads no longer follows `netty-threads` from `spigot.yml`.
- UDP server now copies fewer buffers, batches socket writes and doesn't create packet events when nothing listens to them.
  - In our benchmark, one socket now sends ~1.2x more packets than 2.1.x before it starts dropping, and allocates ~10x less memory per sent packet.
  - With `reuse_port`, 100 players speaking at the same spot (~500k outgoing packets/s) are handled without loss on 4 cores.
- Fixed Opus `mode` and `mtu_size` from the config being ignored by the native encoder (it always used `VOIP`).
