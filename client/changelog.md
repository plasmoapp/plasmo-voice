### Client
- Moved "Bottom Center" hud icon higher in Creative, so it no longer overlaps item names: https://i.imgur.com/3nYWeAN.png.
  - In other game modes, it was also moved slightly lower: https://i.imgur.com/CUpcIdc.png.
- Fixed native memory leak in voice playback. Audio buffers were never freed after being uploaded to OpenAL.

### Server
- Added `BaseServerDirectSource#getListeners` and `ServerProximitySource#getListeners` to API to resolve current source's listeners.
