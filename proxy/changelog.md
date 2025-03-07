### Changes in 2.1.3
- Fixed an issue where proxy identified a backend connection as a player connection, causing a ClassCastException. 
- Added option to set AES key manually using `plasmovoice/aes-key` (path can be changed by `PLASMO_VOICE_AES_KEY_FILE` env) file or `PLASMO_VOICE_AES_KEY` env.