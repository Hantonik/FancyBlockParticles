package hantonik.fbp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.platform.Services;
import hantonik.fbp.screen.FBPFastBlacklistScreen;
import hantonik.fbp.screen.FBPOculusWarningScreen;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class FancyBlockParticles {
    public static final String MOD_ID = "fbp";
    public static final String MOD_NAME = "FancyBlockParticles";
    public static final String MOD_VERSION = Services.PLATFORM.getModVersion(MOD_ID);

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker SETUP_MARKER = MarkerFactory.getMarker("SETUP");

    public static final FBPConfig CONFIG = FBPConfig.create();

    private static boolean OCULUS_WARNING_SCREEN_SHOWN = false;

    public static void postClientTick(Minecraft client) {
        if (FBPKeyMappings.TOGGLE_MOD.consumeClick()) {
            FancyBlockParticles.CONFIG.global.setEnabled(!FancyBlockParticles.CONFIG.global.isEnabled());

            FancyBlockParticles.CONFIG.save();
        }

        if (FBPKeyMappings.TOGGLE_ANIMATIONS.consumeClick()) {
            if (!Services.PLATFORM.isModLoaded("a_good_place")) {
                FancyBlockParticles.CONFIG.animations.setEnabled(!FancyBlockParticles.CONFIG.animations.isEnabled());

                FancyBlockParticles.CONFIG.save();
            }
        }

        if (FBPKeyMappings.OPEN_SETTINGS.consumeClick())
            client.setScreen(new FBPOptionsScreen(null));

        if (FBPKeyMappings.ADD_TO_BLACKLIST.isDown()) {
            if (Screen.hasShiftDown()) {
                var heldItem = client.player.getMainHandItem();

                if (heldItem.getItem() instanceof BlockItem)
                    client.setScreen(new FBPFastBlacklistScreen(heldItem));
            } else {
                var hit = client.hitResult;

                if (hit != null && hit.getType() == HitResult.Type.BLOCK)
                    client.setScreen(new FBPFastBlacklistScreen(((BlockHitResult) hit).getBlockPos()));
            }

            FancyBlockParticles.CONFIG.save();
        }

        if (FBPKeyMappings.FREEZE_PARTICLES.consumeClick()) {
            if (FancyBlockParticles.CONFIG.global.isEnabled()) {
                FancyBlockParticles.CONFIG.global.setFreezeEffect(!FancyBlockParticles.CONFIG.global.isFreezeEffect());

                FancyBlockParticles.CONFIG.save();
            }
        }

        if (FBPKeyMappings.RELOAD_CONFIG.consumeClick()) {
            FancyBlockParticles.CONFIG.load();
            FancyBlockParticles.CONFIG.save();
        }
    }

    public static void onRenderHud(PoseStack stack, int width) {
        if (FancyBlockParticles.CONFIG.global.isEnabled() && FancyBlockParticles.CONFIG.overlay.isFreezeEffectOverlay() && FancyBlockParticles.CONFIG.global.isFreezeEffect() && !Minecraft.getInstance().options.hideGui)
            GuiComponent.drawCenteredString(stack, Minecraft.getInstance().font, new TranslatableComponent("gui.fbp.freeze_effect").withStyle(ChatFormatting.BOLD), width / 2, 5, FancyBlockParticles.CONFIG.overlay.getFreezeEffectColor());
    }

    public static void postScreenInit(Screen screen) {
        if (screen instanceof PauseScreen)
            FancyBlockParticles.CONFIG.save();

        if (!OCULUS_WARNING_SCREEN_SHOWN) {
            if (!FancyBlockParticles.CONFIG.global.isDisableOculusWarning()) {
                if (screen instanceof TitleScreen) {
                    if (Services.PLATFORM.isModLoaded("oculus")) {
                        Minecraft.getInstance().setScreen(new FBPOculusWarningScreen(screen));

                        OCULUS_WARNING_SCREEN_SHOWN = true;
                    }
                }
            }
        }
    }

    public static void onLevelLoad() {
        FBPPlacingAnimationManager.clear();
    }
}
