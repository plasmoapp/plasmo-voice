### Alpha Notice
This version still requires testing, especially for backports.
If you encounter any issues, please report them on Discord: https://discord.gg/uueEqzwCJJ.

Versions 2.0.x and 2.1.x are protocol-compatible,
so there’s no need to worry if the server hasn't been updated to 2.1.x.

### Main Changes
- Backports to 1.18.2, 1.17.1, and 1.16.5.
- New open source [Opus](https://github.com/plasmoapp/opus-jni-rust) and [RNNoise](https://github.com/plasmoapp/rnnoise-jni-rust) binaries, related to [#319](https://github.com/plasmoapp/plasmo-voice/issues/319) [#375](https://github.com/plasmoapp/plasmo-voice/issues/375) [#382](https://github.com/plasmoapp/plasmo-voice/issues/382).
- Languages now support [MiniMessage format](https://docs.advntr.dev/minimessage/index.html).
- Fixed [#376](https://github.com/plasmoapp/plasmo-voice/issues/376) "IP address not updates after recreate minecraft's server docker container"
- ...and many more internal changes and fixes.

### Breaking API Changes
There have been breaking changes to the API, meaning you'll need to update your addons:
- [pv-addon-groups](https://modrinth.com/plugin/pv-addon-groups/version/1.1.0)
- [pv-addon-broadcast](https://modrinth.com/plugin/pv-addon-broadcast/version/1.1.0)

The API documentation is now available: https://plasmovoice.com/docs/api.
It's still a work in progress, so feedback is appreciated.
You can provide feedback on our Discord: https://discord.gg/uueEqzwCJJ.
