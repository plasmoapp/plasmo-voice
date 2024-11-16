### Alpha Notice
This version still requires testing, especially for backports.
If you encounter any issues, please report them on Discord: https://discord.gg/uueEqzwCJJ.

Versions 2.0.x and 2.1.x are protocol-compatible,
so there’s no need to worry if the server hasn't been updated to 2.1.x.

### Changes in 2.1.2
- Fixed buffer overflow when using AudioSender with delayed first frame.
- Fixed EncoderException on server switch on servers with proxy and proxy-side addons (e.g, groups addon).
- Fixed `pv.activation.*` permission is not being updated on the client without reconnect.
- Fixed textfield input not being handled.
- Player sources are now automatically muted if the direct source from the same player becomes active. Can be disabled in menu `Advanced`/`Mute Player On Direct`.