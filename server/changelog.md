### Alpha Notice
This version still requires testing, especially for backports.
If you encounter any issues, please report them on Discord: https://discord.gg/uueEqzwCJJ.

Versions 2.0.x and 2.1.x are protocol-compatible,
so there’s no need to worry if the server hasn't been updated to 2.1.x.

### Changes in 2.1.2
- Fixed buffer overflow when using AudioSender with delayed first frame.
- Fixed `pv.activation.*` permission is not being updated on the client without reconnect.
- Fixed server translations missing in kick messages. [#428](https://github.com/plasmoapp/plasmo-voice/issues/428)
- `/vmute` without arguments now sends command usage instead of "no permissions" error.