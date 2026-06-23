### Client
- Added `ClientAudioSource#effectiveVolume` to the API, the effective gain last applied to a source (master/source/line volume, occlusion, directional and distance gain), readable from any thread.
- Added `ClientAudioSource#sourceLine` to the API, exposing the `ClientSourceLine` a source is currently assigned to.
- Added `player*` shorthands (`getPlayerVolume`/`setPlayerVolume`/`getPlayerMute`/`setPlayerMute`) to the `ClientConfig.Volumes` API for per-player volume and mute.
- Fixed API dependency resolution on NeoForge by preferring instead of hard-requiring specific guava/gson/fastutil versions.
- Added `PlasmoVoice` logger prefix on Fabric.

### Server
- Fixed API dependency resolution on NeoForge by preferring instead of hard-requiring specific guava/gson/fastutil versions.
- Connection logs are now always logged instead of being gated by debug logging.
- Added `PlasmoVoice` logger prefix on Fabric.
