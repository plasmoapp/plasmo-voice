### Features
- Backport to 1.18.2, 1.17.1, 1.16.5
- New [Opus](https://github.com/plasmoapp/opus-jni-rust) and [RNNoise](https://github.com/plasmoapp/rnnoise-jni-rust) binaries
- The change in static icons position should be smoother now

### Bug fixes
- Fixed a rendering bug with some activation distances (https://i.imgur.com/RYc8YnU.gif)
- Fixed an error causing "Delete buffers: Invalid operation" in logs
- Fixed a rendering bug when microphone test is active, but the icon is disabled

### Addons update
Due to major API changes, you should update your addons to version 1.1.x:
- [pv-addon-soundphysics]()
- [pv-addon-replaymod]()

### Major API changes
There have been breaking changes to the API, so unfortunately, you will have to update your addons.

But here's some good news — the API documentation is finally available: https://plasmovoice.com/docs/api
