package hantonik.fbp.config;

import com.google.gson.JsonObject;

public interface IFBPConfig<T extends IFBPConfig<T>> {
    void setConfig(T config);

    default void applyConfig(T config) {
        this.setConfig(config);
    }

    default void load() {}

    default void load(JsonObject json) {}

    JsonObject save();

    void reset();

    T copy();
}
