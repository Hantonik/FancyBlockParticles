package hantonik.fbp;

import com.mojang.logging.LogUtils;
import hantonik.fbp.config.FBPPhysicsConfig;
import hantonik.fbp.config.FBPRenderConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.util.handler.FBPClientHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
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

    public static final FBPPhysicsConfig PHYSICS_CONFIG = FBPPhysicsConfig.load();
    public static final FBPRenderConfig RENDER_CONFIG = FBPRenderConfig.load();

    public FancyBlockParticles(IEventBus bus) {
        LOGGER.info(SETUP_MARKER, "Initializing...");

        bus.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.register(new FBPKeyMappings());
            bus.register(FancyBlockParticles.RENDER_CONFIG);
        }
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info(SETUP_MARKER, "Starting client setup...");

        NeoForge.EVENT_BUS.register(new FBPClientHandler());
        NeoForge.EVENT_BUS.register(FancyBlockParticles.PHYSICS_CONFIG);

        LOGGER.info(SETUP_MARKER, "Finished client setup!");
    }
}
