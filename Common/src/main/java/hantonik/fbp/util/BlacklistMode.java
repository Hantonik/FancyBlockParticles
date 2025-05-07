package hantonik.fbp.util;

public enum BlacklistMode {
    FANCY,
    VANILLA,
    BLACKLISTED;

    public BlacklistMode getNext() {
        return switch (this) {
            case FANCY -> VANILLA;
            case VANILLA -> BLACKLISTED;
            case BLACKLISTED -> FANCY;
        };
    }
}
