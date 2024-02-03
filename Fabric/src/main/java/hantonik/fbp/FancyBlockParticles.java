package hantonik.fbp;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import hantonik.fbp.config.FBPPhysicsConfig;
import hantonik.fbp.config.FBPRenderConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.FBPBlacklistScreen;
import hantonik.fbp.screen.FBPOptionsScreen;
import hantonik.fbp.util.FBPConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class FancyBlockParticles implements ClientModInitializer {
    public static final String MOD_ID = "fbp";
    public static final String MOD_NAME = "FancyBlockParticles";
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker SETUP_MARKER = MarkerFactory.getMarker("SETUP");

    public static final FBPPhysicsConfig PHYSICS_CONFIG = FBPPhysicsConfig.load();
    public static final FBPRenderConfig RENDER_CONFIG = FBPRenderConfig.load();

    @Override
    public void onInitializeClient() {
        LOGGER.info(SETUP_MARKER, "Initializing client...");

        FancyBlockParticles.RENDER_CONFIG.register();
        FancyBlockParticles.PHYSICS_CONFIG.register();

        FBPKeyMappings.register();

        ScreenEvents.AFTER_INIT.register(this::postScreenInit);
        ClientTickEvents.END_CLIENT_TICK.register(this::postClientTick);
        HudRenderCallback.EVENT.register(this::onRenderHud);

        ParticleEngine.RENDER_ORDER = Util.make(new ImmutableList.Builder<ParticleRenderType>(), builder -> {
            builder.add(FBPConstants.FBP_PARTICLE_RENDER);
            builder.add(FBPConstants.FBP_TERRAIN_RENDER);

            builder.addAll(ParticleEngine.RENDER_ORDER);
        }).build();
    }

    private void postScreenInit(Minecraft client, Screen screen, int width, int height) {
        if (screen.isPauseScreen())
            if (!(screen instanceof FBPOptionsScreen))
                FancyBlockParticles.RENDER_CONFIG.save();
    }

    private void postClientTick(Minecraft client) {
        if (FBPKeyMappings.TOGGLE.consumeClick())
            FancyBlockParticles.RENDER_CONFIG.setEnabled(!FancyBlockParticles.RENDER_CONFIG.isEnabled());

        if (FBPKeyMappings.SETTINGS.consumeClick())
            client.setScreen(new FBPOptionsScreen());

        if (FBPKeyMappings.FAST_BLACKLIST.isDown()) {
            if (Screen.hasShiftDown()) {
                var heldItem = client.player.getMainHandItem();

                if (heldItem.getItem() instanceof BlockItem)
                    client.setScreen(new FBPBlacklistScreen(heldItem));
            } else {
                var hit = client.hitResult;

                if (hit != null && hit.getType() == HitResult.Type.BLOCK)
                    client.setScreen(new FBPBlacklistScreen(((BlockHitResult) hit).getBlockPos()));
            }
        }

        if (FBPKeyMappings.FREEZE.consumeClick())
            if (FancyBlockParticles.RENDER_CONFIG.isEnabled())
                FancyBlockParticles.RENDER_CONFIG.setFrozen(!FancyBlockParticles.RENDER_CONFIG.isFrozen());
    }

    public void onRenderHud(GuiGraphics graphics, float partialTick) {
        if (FancyBlockParticles.RENDER_CONFIG.isEnabled() && FancyBlockParticles.RENDER_CONFIG.isFrozen())
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("screen.fbp.freeze"), graphics.guiWidth() / 2, 5, 0x0080FF);
    }
}
