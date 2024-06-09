package hantonik.fbp.platform.services;

import hantonik.fbp.platform.util.EnvironmentType;
import net.neoforged.fml.loading.FMLLoader;

public final class NeoForgeEnvironmentHelper implements IEnvironmentHelper {
    @Override
    public EnvironmentType getEnvironmentType() {
        return EnvironmentType.from(FMLLoader.getDist());
    }
}
