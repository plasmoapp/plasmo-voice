package su.plo.voice.api.addon;

public enum AddonLoaderScope {

    // fabric.mod.json, mods.toml
    CLIENT,

    // fabric.mod.json, mods.toml, plugin.yml
    SERVER,
    // bungee.yml, velocity-plugin.json
    PROXY,
    // SERVER + PROXY
    ANY_SERVER,

    // CLIENT + ANY_SERVER
    ANY;

    public boolean isServer() {
        return this == ANY || this == ANY_SERVER || this == SERVER;
    }

    public boolean isClient() {
        return this == ANY || this == CLIENT;
    }

    public boolean isProxy() {
        return this == ANY || this == ANY_SERVER || this == PROXY;
    }
}
