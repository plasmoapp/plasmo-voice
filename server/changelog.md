### Changes in 2.1.7
- Fixed `/vreload` not reloading language changes.
- Fixed `Player is not connected to UDP server` exception on PlayerShowEntityEvent.
- Added more descriptive comments in the config.
- Added configurable player icon visibility and offset. Check `[voice.player_icon]` config block for more info.
- Reworked keep alive to handle more players.
- Improved performance using epoll when available.
- Fixed leaving in vanish breaks Plasmo Voice sometimes. ([#493](https://github.com/plasmoapp/plasmo-voice/issues/493))
