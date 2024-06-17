package hantonik.fbp.platform.services;

import hantonik.fbp.platform.util.EnvironmentType;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricEnvironmentHelper implements IEnvironmentHelper {
    @Override
    public EnvironmentType getEnvironmentType() {
        return EnvironmentType.from(FabricLoader.getInstance().getEnvironmentType());
    }
}
