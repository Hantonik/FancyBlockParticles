package hantonik.fbp.platform.services;

import hantonik.fbp.platform.util.EnvironmentType;

import java.util.function.Supplier;

public interface IEnvironmentHelper {
    EnvironmentType getEnvironmentType();

    default boolean isClient() {
        return this.getEnvironmentType().isClient();
    }

    default void runOn(EnvironmentType env, Supplier<Runnable> runnable) {
        if (this.getEnvironmentType() == env)
            runnable.get().run();
    }
}
