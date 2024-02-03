package hantonik.fbp.util.handler;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.FBPBlacklistScreen;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPauseUpdatedEvent;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.event.TickEvent;

@OnlyIn(Dist.CLIENT)
public final class FBPClientHandler {
    @SubscribeEvent
    public void onClientPauseUpdated(final ClientPauseUpdatedEvent event) {
        if (event.isPaused())
            if (!(Minecraft.getInstance().screen instanceof FBPOptionsScreen))
                FancyBlockParticles.RENDER_CONFIG.save();
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            var minecraft = Minecraft.getInstance();

            if (FBPKeyMappings.TOGGLE.consumeClick())
                FancyBlockParticles.RENDER_CONFIG.setEnabled(!FancyBlockParticles.RENDER_CONFIG.isEnabled());

            if (FBPKeyMappings.SETTINGS.consumeClick())
                minecraft.setScreen(new FBPOptionsScreen());

            if (FBPKeyMappings.FAST_BLACKLIST.isDown()) {
                if (Screen.hasShiftDown()) {
                    var heldItem = minecraft.player.getMainHandItem();

                    if (heldItem.getItem() instanceof BlockItem)
                        minecraft.setScreen(new FBPBlacklistScreen(heldItem));
                } else {
                    var hit = minecraft.hitResult;

                    if (hit != null && hit.getType() == HitResult.Type.BLOCK)
                        minecraft.setScreen(new FBPBlacklistScreen(((BlockHitResult) hit).getBlockPos()));
                }
            }

            if (FBPKeyMappings.FREEZE.consumeClick())
                if (FancyBlockParticles.RENDER_CONFIG.isEnabled())
                    FancyBlockParticles.RENDER_CONFIG.setFrozen(!FancyBlockParticles.RENDER_CONFIG.isFrozen());
        }
    }

    @SubscribeEvent
    public void postRenderGuiOverlay(final RenderGuiOverlayEvent.Post event) {
        if (FancyBlockParticles.RENDER_CONFIG.isEnabled() && FancyBlockParticles.RENDER_CONFIG.isFrozen())
            event.getGuiGraphics().drawCenteredString(Minecraft.getInstance().font, Component.translatable("screen.fbp.freeze"), event.getGuiGraphics().guiWidth() / 2, 5, 0x0080FF);
    }
}
