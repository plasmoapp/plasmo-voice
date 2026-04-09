### Client
- Added support for navigation using "Tab".
- Added auto-focus on text field in "Volume" menu tab.
- Fixed crash when switching servers on a proxy. ([#509](https://github.com/plasmoapp/plasmo-voice/issues/509))
- Fixed percent rendering above player head breaks rendering on >1.21.1.
- UDP connection is now considered established only after response from server is received.

### Server
- Fixed "Incorrect behavior on resuming AudioSender". ([#501](https://github.com/plasmoapp/plasmo-voice/issues/501))
- Added sanity bounds checks for collections decoding. ([#505](https://github.com/plasmoapp/plasmo-voice/issues/505))
- Embedded player info request retry logic to improve connection reliability. ([#466](https://github.com/plasmoapp/plasmo-voice/issues/466))
