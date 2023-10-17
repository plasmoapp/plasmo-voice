package su.plo.voice.api.addon;

public enum AddonLoaderScope {

    /**
     * Indicates that the addon should be loaded in the client scope.
     * <br>
     * fabric.mod.json, mods.toml
     */
    CLIENT,

    /**
     * Indicates that the addon should be loaded in the server scope.
     * <br>
     * fabric.mod.json, mods.toml, or plugin.yml
     */
    SERVER,

    /**
     * Indicates that the addon should be loaded in the proxy scope.
     * <br>
     * bungee.yml, velocity-plugin.json
     */
    PROXY,

    /**
     * Indicates that the addon should be loaded in either the server or proxy scope.
     */
    ANY_SERVER,

    /**
     * Indicates that the addon should be loaded in either the client, server, or proxy scope.
     */
    ANY;

    /**
     * Checks if the addon should be loaded in a server environment.
     *
     * @return {@code true} if the addon is intended for the server scope.
     */
    public boolean isServer() {
        return this == ANY || this == ANY_SERVER || this == SERVER;
    }

    /**
     * Checks if the addon should be loaded in a client environment.
     *
     * @return {@code true} if the addon is intended for the client scope.
     */
    public boolean isClient() {
        return this == ANY || this == CLIENT;
    }

    /**
     * Checks if the addon should be loaded in a proxy environment.
     *
     * @return {@code true} if the addon is intended for the proxy scope.
     */
    public boolean isProxy() {
        return this == ANY || this == ANY_SERVER || this == PROXY;
    }
}
