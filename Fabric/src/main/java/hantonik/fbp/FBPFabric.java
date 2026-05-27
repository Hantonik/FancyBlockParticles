package hantonik.fbp;

import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.particle.group.FBPParticleGroup;
import hantonik.fbp.util.FBPConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class FBPFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing client...");

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(FancyBlockParticles.MOD_ID, "config");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                FancyBlockParticles.CONFIG.load();
            }
        });

        for (var mapping : FBPKeyMappings.MAPPINGS)
            KeyMappingHelper.registerKeyMapping(mapping);

        ClientTickEvents.END_CLIENT_TICK.register(FancyBlockParticles::postClientTick);
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath(FancyBlockParticles.MOD_ID, "hud"), (graphics, _) -> FancyBlockParticles.onRenderHud(graphics));
        ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
            if (screen instanceof PauseScreen)
                FancyBlockParticles.onClientPause(screen);

            FancyBlockParticles.postScreenInit(screen);
        });
        ClientPlayConnectionEvents.JOIN.register(((_, _, _) -> FancyBlockParticles.onLevelLoad()));

        ParticleGroupRegistry.register(FBPConstants.FBP_PARTICLE_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_PARTICLE_RENDER));
        ParticleGroupRegistry.register(FBPConstants.FBP_TERRAIN_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_TERRAIN_RENDER));
        ParticleGroupRegistry.register(FBPConstants.FBP_ANIMATION_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_ANIMATION_RENDER));
    }
}
