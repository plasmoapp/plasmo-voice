package su.plo.lib.server.permission;

public enum PermissionTristate {

    TRUE,
    FALSE,
    UNDEFINED;

    public boolean booleanValue(boolean defaultValue) {
        return this == UNDEFINED
                ? defaultValue
                : this == TRUE;
    }
}
