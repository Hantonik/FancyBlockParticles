package hantonik.fbp;

import com.mojang.logging.LogUtils;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.util.handler.FBPClientHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Mod(FancyBlockParticles.MOD_ID)
public final class FancyBlockParticles {
    public static final String MOD_ID = "fbp";
    public static final String MOD_NAME = "FancyBlockParticles";
    public static final String MOD_VERSION = ModList.get().getModContainerById(MOD_ID).orElseThrow().getModInfo().getVersion().getQualifier();

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker SETUP_MARKER = MarkerFactory.getMarker("SETUP");

    public static final FBPConfig CONFIG = FBPConfig.load();

    public FancyBlockParticles() {
        LOGGER.info(SETUP_MARKER, "Initializing...");

        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.register(new FBPKeyMappings());
            bus.register(FancyBlockParticles.CONFIG);
        });
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info(SETUP_MARKER, "Starting client setup...");

        MinecraftForge.EVENT_BUS.register(new FBPClientHandler());

        LOGGER.info(SETUP_MARKER, "Finished client setup!");
    }
}
