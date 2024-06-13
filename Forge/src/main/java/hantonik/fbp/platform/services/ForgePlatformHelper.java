package hantonik.fbp.platform.services;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getModVersion(String modId) {
        return ModList.get().getModContainerById(modId).orElseThrow().getModInfo().getVersion().getQualifier();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
