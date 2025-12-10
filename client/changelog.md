### Changes in 2.1.7
#### Client
- Fixed crash on latest versions of NeoForge 1.21.9/10.
- Fixed UnsupportedOperationException crash on 1.21.10 caused by player game profile skins in direct sources (cross-server groups).
- Fixed different types of activations blocking each other (e.g. when whisper was used, groups can't be used at the same time. Now you can use both at the same time).
- Added option to choose between direct and proximity mute when sources from the same player are overlapping. (Advanced -> Source Types Overlap)
- [Added warning on transitive activation conflicts.](https://i.imgur.com/e3a1WeY.png)
- [Added error button when microphone is not available.](https://i.imgur.com/CutT5Vb.png)
- Fixed `pv.allow_freecam` set to false breaks 3d audio panning ([#492](https://github.com/plasmoapp/plasmo-voice/issues/492)).
- Fixed microphone gain not properly preventing clipping in some cases.
- Added option to disable input device ([#494](https://github.com/plasmoapp/plasmo-voice/pull/494)).

#### Server
- Fixed `/vreload` not reloading language changes.
- Added more descriptive comments in the config.
- Added configurable player icon visibility and offset. Check `[voice.player_icon]` config block for more info.
- Reworked keep alive to handle more players.
- Improved performance using epoll when available.
