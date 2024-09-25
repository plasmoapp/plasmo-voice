### Alpha Notice
This version still requires testing, especially for backports.
If you encounter any issues, please report them on Discord: https://discord.gg/uueEqzwCJJ.

Versions 2.0.x and 2.1.x are protocol-compatible,
so there’s no need to worry if the server hasn't been updated to 2.1.x.

### Main Changes
- Backports to 1.18.2, 1.17.1, and 1.16.5.
- New open source [Opus](https://github.com/plasmoapp/opus-jni-rust) and [RNNoise](https://github.com/plasmoapp/rnnoise-jni-rust) binaries, related to [#319](https://github.com/plasmoapp/plasmo-voice/issues/319) [#375](https://github.com/plasmoapp/plasmo-voice/issues/375) [#382](https://github.com/plasmoapp/plasmo-voice/issues/382).
- Server now sends a request info packet to any player joining the server. This was introduced due to differences in how various mod loaders handle the mod channel list, and I just can't figure out how to make it work on Forge/NeoForge 1.20.2+.
- Server now checks if a player has voice disabled or microphone muted before sending or receiving the audio.
- Languages now support [MiniMessage format](https://docs.advntr.dev/minimessage/index.html).
- ...and many more internal changes and fixes.

### Breaking API Changes
There have been breaking changes to the API, meaning you'll need to update your addons:
- [pv-addon-groups](https://modrinth.com/plugin/pv-addon-groups/version/1.1.0)
- [pv-addon-sculk](https://modrinth.com/plugin/pv-addon-sculk/version/1.1.0)
- [pv-addon-broadcast](https://modrinth.com/plugin/pv-addon-broadcast/version/1.1.0)
- [pv-addon-spectator](https://modrinth.com/plugin/pv-addon-spectator/version/1.1.0)
- [pv-addon-whisper](https://modrinth.com/plugin/pv-addon-whisper/version/1.1.0)
- [pv-addon-priority](https://modrinth.com/plugin/pv-addon-priority/version/1.1.0)
- [pv-addon-lavaplayer-lib](https://modrinth.com/plugin/pv-addon-lavaplayer-lib/version/1.1.0)
- [pv-addon-discs](https://modrinth.com/plugin/pv-addon-discs/version/1.1.0)

The API documentation is now available: https://plasmovoice.com/docs/api.
It's still a work in progress, so feedback is appreciated.
You can provide feedback on our Discord: https://discord.gg/uueEqzwCJJ.
