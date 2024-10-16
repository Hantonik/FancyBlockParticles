package hantonik.fbp;

import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

@Mod(FancyBlockParticles.MOD_ID)
public final class FBPForge {
    public FBPForge() {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing...");

        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(this::onRegisterClientReloadListeners));

        ModList.get().getModContainerById(FancyBlockParticles.MOD_ID).ifPresent(mc -> mc.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new FBPOptionsScreen(screen))));
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Starting client setup...");

        FBPKeyMappings.MAPPINGS.forEach(ClientRegistry::registerKeyBinding);

        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(this::postScreenInit);
        MinecraftForge.EVENT_BUS.addListener(this::postRenderGuiOverlay);
        MinecraftForge.EVENT_BUS.addListener(this::onClientLoggingIn);

        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Finished client setup!");
    }

    private void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> FancyBlockParticles.CONFIG.load());
    }

    private void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            FancyBlockParticles.postClientTick(Minecraft.getInstance());
    }

    private void postScreenInit(final GuiScreenEvent.InitGuiEvent.Post event) {
        FancyBlockParticles.postScreenInit(event.getGui());
    }

    private void postRenderGuiOverlay(final RenderGameOverlayEvent.Post event) {
        FancyBlockParticles.onRenderHud(event.getMatrixStack(), event.getWindow().getGuiScaledWidth());
    }

    private void onClientLoggingIn(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        FancyBlockParticles.onLevelLoad();
    }
}
