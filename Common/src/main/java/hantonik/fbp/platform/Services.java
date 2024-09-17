package hantonik.fbp.platform;

import hantonik.fbp.platform.services.IClientHelper;
import hantonik.fbp.platform.services.IEnvironmentHelper;
import hantonik.fbp.platform.services.IPlatformHelper;
import hantonik.fbp.platform.services.IRegistryHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import java.util.ServiceLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Services {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final IEnvironmentHelper ENVIRONMENT = load(IEnvironmentHelper.class);
    public static final IClientHelper CLIENT = load(IClientHelper.class);
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);

    public static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));

        LOGGER.debug(MarkerManager.getMarker("SERVICES"), "Loaded {} for service {}", loadedService, clazz);

        return loadedService;
    }
}
