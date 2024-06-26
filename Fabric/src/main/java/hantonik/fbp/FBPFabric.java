package hantonik.fbp;

import com.google.common.collect.ImmutableList;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.util.FBPConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class FBPFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing client...");

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "config");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                FancyBlockParticles.CONFIG.load();
            }
        });

        for (var mapping : FBPKeyMappings.MAPPINGS)
            KeyBindingHelper.registerKeyBinding(mapping);

        ClientTickEvents.END_CLIENT_TICK.register(FancyBlockParticles::postClientTick);
        HudRenderCallback.EVENT.register((graphics, partialTick) -> FancyBlockParticles.onRenderHud(graphics));
        ScreenEvents.AFTER_INIT.register(((minecraft, screen, width, height) -> {
            if (screen instanceof PauseScreen)
                FancyBlockParticles.onClientPause(screen);
        }));

        ParticleEngine.RENDER_ORDER = Util.make(new ImmutableList.Builder<ParticleRenderType>(), builder -> {
            builder.add(FBPConstants.FBP_PARTICLE_RENDER);
            builder.add(FBPConstants.FBP_TERRAIN_RENDER);

            builder.addAll(ParticleEngine.RENDER_ORDER);
        }).build();
    }
}
