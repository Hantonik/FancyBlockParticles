package hantonik.fbp.platform.util;

import java.util.Locale;

public enum EnvironmentType {
    CLIENT, DEDICATED_SERVER;

    public static EnvironmentType from(Enum<?> other) {
        return EnvironmentType.values()[other.ordinal()];
    }

    public boolean isClient() {
        return this == CLIENT;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ENGLISH);
    }
}
