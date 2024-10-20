package hantonik.fbp.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {
    String getModVersion(String modId);

    boolean isModLoaded(String modId);

    Path getConfigDir();
}
