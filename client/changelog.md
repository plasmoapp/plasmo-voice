### Changes in 2.1.5
- Players' icons are now rendered only after successful connect to the UDP server.
- Added minimum threshold for distance gain to fix `Set source float 4106: -7.955086E-7: Invalid parameter parameter value.`.
- Fixed an issue where the audio end packet wasn't sent after reconnecting to the UDP server.
- Fixed an audio glitch on re-activating abruptly stopped source (e.g. on dimension change while source is activated).
- Fixed broken rendering of the entire HUD when HUD icons/overlay are being rendered on <1.20.
- Fixed [crash on <1.21.5](https://github.com/plasmoapp/plasmo-voice/issues/461) and rending issues on 1.21.5 with Vulkanmod.
- Fixed a deadlock caused by language loading, which could eventually break languages and other addons that use coroutines (e.g. discs addon).
