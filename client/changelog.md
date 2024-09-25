### Alpha Notice
This version still requires testing, especially for backports.
If you encounter any issues, please report them on Discord: https://discord.gg/uueEqzwCJJ.

Versions 2.0.x and 2.1.x are protocol-compatible,
so there’s no need to worry if the server hasn't been updated to 2.1.x.

### Main Changes
- Backports to 1.18.2, 1.17.1, and 1.16.5.
- New open source [Opus](https://github.com/plasmoapp/opus-jni-rust) and [RNNoise](https://github.com/plasmoapp/rnnoise-jni-rust) binaries, related to [#319](https://github.com/plasmoapp/plasmo-voice/issues/319) [#375](https://github.com/plasmoapp/plasmo-voice/issues/375) [#382](https://github.com/plasmoapp/plasmo-voice/issues/382).
- NeoForge support for 1.21+.
- Forge support for 1.20.4+.
- Client now sends server ip and server port in the first ping packet, which is used for a connection establishment. This is useful for building reverse proxies. You can find an example here: https://github.com/Apehum/pv-reverse-proxy.
- Server now sends a request info packet to any player joining the server. This was introduced due to differences in how various mod loaders handle the mod channel list, and I just can't figure out how to make it work on Forge/NeoForge 1.20.2+.
- Server now checks if a player has voice disabled or microphone muted before sending or receiving the audio.
- "Open to LAN" now restarts the UDP server with the published port if `host.port` is set to 0.
- Fixed "GUI Icon remains visible when GUI is hidden with F1" ([#407](https://github.com/plasmoapp/plasmo-voice/issues/407)).
- Fade-in/fade-out effects before and after an audio stream, fixing glitches that occurred when the audio source starts or stops playing audio.
- Languages now support [MiniMessage format](https://docs.advntr.dev/minimessage/index.html).
- Network jitter buffer.
- ...and many more internal changes and fixes.

### Breaking API Changes
There have been breaking changes to the API, meaning you'll need to update your addons:
#### Client Addons
- [pv-addon-soundphysics](https://modrinth.com/mod/pv-addon-soundphysics/version/1.1.0)
- [pv-addon-replaymod 1.16.5-1.21](https://modrinth.com/mod/pv-addon-replaymod/version/1.16.5-2.1.0)
- [pv-addon-replaymod 1.21+](https://modrinth.com/mod/pv-addon-replaymod/version/1.21-2.1.0)
### Server Addons
- [pv-addon-groups](https://modrinth.com/plugin/pv-addon-groups/version/1.1.0)
- [pv-addon-sculk](https://modrinth.com/plugin/pv-addon-sculk/version/1.1.0)
- [pv-addon-broadcast](https://modrinth.com/plugin/pv-addon-broadcast/version/1.1.0)
- [pv-addon-spectator](https://modrinth.com/plugin/pv-addon-spectator/version/1.1.0)
- [pv-addon-whisper](https://modrinth.com/plugin/pv-addon-whisper/version/1.1.0)
- [pv-addon-priority](https://modrinth.com/plugin/pv-addon-priority/version/1.1.0)

The API documentation is now available: https://plasmovoice.com/docs/api.
It's still a work in progress, so feedback is appreciated.
You can provide feedback on our Discord: https://discord.gg/uueEqzwCJJ.

