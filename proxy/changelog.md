### Changes in 2.1.5
- Fixed an issue where the audio end packet wasn't sent after reconnecting to the UDP server.
- Fixed a deadlock caused by language loading, which could eventually break languages and other addons that use coroutines (e.g. discs addon).
