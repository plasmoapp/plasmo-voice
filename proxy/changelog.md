### Beta
This is a beta version, and it still requires testing, especially the separate per-player sockets to backend servers.
If you encounter any issues, please report them on [GitHub](https://github.com/plasmoapp/plasmo-voice/issues) or [Discord](https://discord.gg/uueEqzwCJJ).

Versions 2.1.x and 2.2.x are protocol-compatible,
so there's no need to worry if server or client hasn't been updated to 2.2.x.

---

- Java 21 is now required.
- Proxy now opens a separate socket to the backend server for each player instead of sending backend traffic through the public voice socket, which was a bottleneck under high load. [#535](https://github.com/plasmoapp/plasmo-voice/pull/535) 
- Updated slib to 2.1.0, which breaks addons that use:
  - `McCommandManager#register` with Brigadier commands. Register them with `McBrigadierRegistry` from `McBrigadierCommandsRegisterEvent` listener instead.
  - `MessageTextConverter`. Use `McTextMessage.of(component)` instead.
- Added optional SO_REUSEPORT support (`[reuse_port]` in `config.toml`, Linux only). When enabled, multiple sockets on separate threads share the voice port instead of one.
- Number of UDP threads can now be set with `-Dplasmovoice.udp_threads` (default: CPU cores * 2).
- UDP server now copies fewer buffers, batches socket writes and forwards backend packets without decoding them.
