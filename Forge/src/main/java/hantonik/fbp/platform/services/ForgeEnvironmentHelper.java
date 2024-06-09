package hantonik.fbp.platform.services;

import hantonik.fbp.platform.util.EnvironmentType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.function.Supplier;

public final class ForgeEnvironmentHelper implements IEnvironmentHelper {
    @Override
    public EnvironmentType getEnvironmentType() {
        return EnvironmentType.from(FMLLoader.getDist());
    }

    @Override
    public void runOn(EnvironmentType env, Supplier<Runnable> runnable) {
        DistExecutor.unsafeRunWhenOn(Dist.values()[env.ordinal()], runnable);
    }
}
