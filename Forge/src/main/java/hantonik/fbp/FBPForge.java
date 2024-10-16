package hantonik.fbp;

import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FancyBlockParticles.MOD_ID)
public final class FBPForge {
    public FBPForge() {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing...");

        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(this::onRegisterKeyMappings);
            bus.addListener(this::onRegisterClientReloadListeners);
        });

        ModList.get().getModContainerById(FancyBlockParticles.MOD_ID).ifPresent(mc -> mc.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(FBPOptionsScreen::new)));
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Starting client setup...");

        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(this::postClientPauseChange);
        MinecraftForge.EVENT_BUS.addListener(this::postScreenInit);
        MinecraftForge.EVENT_BUS.addListener(this::onClientLoggingIn);

        Minecraft.getInstance().gui.layers.add(((graphics, partialTick) -> FancyBlockParticles.onRenderHud(graphics)));

        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Finished client setup!");
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        FBPKeyMappings.MAPPINGS.forEach(event::register);
    }

    private void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> FancyBlockParticles.CONFIG.load());
    }

    private void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            FancyBlockParticles.postClientTick(Minecraft.getInstance());
    }

    private void postClientPauseChange(final ClientPauseChangeEvent.Post event) {
        if (event.isPaused())
            FancyBlockParticles.onClientPause(Minecraft.getInstance().screen);
    }

    private void postScreenInit(final ScreenEvent.Init.Post event) {
        FancyBlockParticles.postScreenInit(event.getScreen());
    }

    private void onClientLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        FancyBlockParticles.onLevelLoad();
    }
}
