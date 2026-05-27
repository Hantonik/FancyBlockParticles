package hantonik.fbp;

import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.particle.group.FBPParticleGroup;
import hantonik.fbp.screen.FBPOptionsScreen;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FancyBlockParticles.MOD_ID, dist = Dist.CLIENT)
public final class FBPNeoForge {
    public FBPNeoForge(IEventBus bus, ModContainer container) {
        if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
            FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing...");

            bus.register(this);
            bus.addListener(this::onRegisterKeyMappings);
            bus.addListener(this::onRegisterParticleGroups);
            bus.addListener(this::onRegisterClientReloadListeners);

            container.registerExtensionPoint(IConfigScreenFactory.class, (_, modsScreen) -> new FBPOptionsScreen(modsScreen));
        }
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Starting client setup...");

        NeoForge.EVENT_BUS.addListener(this::postClientTick);
        NeoForge.EVENT_BUS.addListener(this::postRenderGuiLayer);
        NeoForge.EVENT_BUS.addListener(this::postClientPauseChange);
        NeoForge.EVENT_BUS.addListener(this::postScreenInit);
        NeoForge.EVENT_BUS.addListener(this::onClientLoggingIn);

        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Finished client setup!");
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        FBPKeyMappings.MAPPINGS.forEach(event::register);
    }

    private void onRegisterParticleGroups(final RegisterParticleGroupsEvent event) {
        event.register(FBPConstants.FBP_PARTICLE_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_PARTICLE_RENDER));
        event.register(FBPConstants.FBP_TERRAIN_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_TERRAIN_RENDER));
        event.register(FBPConstants.FBP_ANIMATION_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_ANIMATION_RENDER));
    }

    private void onRegisterClientReloadListeners(final AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(FancyBlockParticles.MOD_ID, "config"), (ResourceManagerReloadListener) _ -> FancyBlockParticles.CONFIG.load());
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

    private void postScreenInit(final ScreenEvent.Init.Post event) {
        FancyBlockParticles.postScreenInit(event.getScreen());
    }

    private void onClientLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        FancyBlockParticles.onLevelLoad();
    }
}
