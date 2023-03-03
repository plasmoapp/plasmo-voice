package su.plo.voice.api.addon;

public enum AddonScope {

    CLIENT,

    SERVER,
    PROXY,
    LIB_SERVER, // SERVER OR PROXY

    LIB;

    public boolean isCompatible(AddonScope scope) {
        return scope == LIB ||
                this == scope ||
                ((this == SERVER || this == PROXY) && scope == LIB_SERVER);
    }
}
