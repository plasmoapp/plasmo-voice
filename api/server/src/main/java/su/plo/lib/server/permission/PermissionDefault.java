package su.plo.lib.server.permission;

public enum PermissionDefault {

    TRUE,
    FALSE,
    OP,
    NOT_OP;

    public boolean getValue(boolean op) {
        switch (this) {
            case TRUE:
                return true;
            case OP:
                return op;
            case NOT_OP:
                return !op;
            default:
                return false;
        }
    }
}
