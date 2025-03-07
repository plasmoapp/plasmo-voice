### Alpha Notice
This version still requires testing, especially for backports.
If you encounter any issues, please report them on Discord: https://discord.gg/uueEqzwCJJ.

Versions 2.0.x and 2.1.x are protocol-compatible,
so there’s no need to worry if the server hasn't been updated to 2.1.x.

### Changes in 2.1.3
- Fixed an issue where proxy identified a backend connection as a player connection, causing a ClassCastException. 
- Added option to set AES key manually using `plasmovoice/aes-key` (path can be changed by `PLASMO_VOICE_AES_KEY_FILE` env) file or `PLASMO_VOICE_AES_KEY` env.