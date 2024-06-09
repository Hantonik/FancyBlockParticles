package hantonik.fbp;

import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(FancyBlockParticles.MOD_ID)
public final class FBPNeoForge {
    public FBPNeoForge(IEventBus bus) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing...");

        bus.register(this);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            bus.addListener(this::onRegisterKeyMappings);
            bus.addListener(this::onRegisterClientReloadListeners);
        }

        ModList.get().getModContainerById(FancyBlockParticles.MOD_ID).ifPresent(mc -> mc.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, modsScreen) -> new FBPOptionsScreen(modsScreen)));
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Starting client setup...");

        NeoForge.EVENT_BUS.addListener(this::postClientTick);
        NeoForge.EVENT_BUS.addListener(this::postRenderGuiLayer);
        NeoForge.EVENT_BUS.addListener(this::postClientPauseChange);

        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Finished client setup!");
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        FBPKeyMappings.MAPPINGS.forEach(event::register);
    }

    private void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> FancyBlockParticles.CONFIG.reload());
    }

    private void postClientTick(final ClientTickEvent.Post event) {
        FancyBlockParticles.postClientTick(Minecraft.getInstance());
    }

    private void postRenderGuiLayer(final RenderGuiLayerEvent.Post event) {
        FancyBlockParticles.onRenderHud(event.getGuiGraphics());
    }

    private void postClientPauseChange(final ClientPauseChangeEvent.Post event) {
        if (event.isPaused())
            FancyBlockParticles.onClientPause(Minecraft.getInstance().screen);
    }
}
