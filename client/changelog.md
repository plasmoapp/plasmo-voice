### Changes in 2.1.5
- Players' icons are now rendered only after successful connect to the UDP server.
- Added minimum threshold for distance gain to fix `Set source float 4106: -7.955086E-7: Invalid parameter parameter value.`.
- Fixed an issue where the audio end packet wasn't sent after reconnecting to the UDP server.
- Fixed an audio glitch on re-activating abruptly stopped source (e.g. on dimension change while source is activated). 
