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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPauseEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public final class FBPClientHandler {
    @SubscribeEvent
    public void onClientPause(final ClientPauseEvent event) {
        if (event.isPaused())
            if (!(Minecraft.getInstance().screen instanceof FBPOptionsScreen))
                FancyBlockParticles.CONFIG.save();
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            var minecraft = Minecraft.getInstance();

            if (FBPKeyMappings.TOGGLE.get().consumeClick())
                FancyBlockParticles.CONFIG.setEnabled(!FancyBlockParticles.CONFIG.isEnabled());

            if (FBPKeyMappings.SETTINGS.get().consumeClick())
                minecraft.setScreen(new FBPOptionsScreen());

            if (FBPKeyMappings.FAST_BLACKLIST.get().isDown()) {
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

            if (FBPKeyMappings.FREEZE.get().consumeClick())
                if (FancyBlockParticles.CONFIG.isEnabled())
                    FancyBlockParticles.CONFIG.setFrozen(!FancyBlockParticles.CONFIG.isFrozen());
        }
    }

    @SubscribeEvent
    public void postRenderGuiOverlay(final RenderGuiOverlayEvent.Post event) {
        if (FancyBlockParticles.CONFIG.isEnabled() && FancyBlockParticles.CONFIG.isFrozen())
            event.getGuiGraphics().drawCenteredString(Minecraft.getInstance().font, Component.translatable("screen.fbp.freeze"), event.getGuiGraphics().guiWidth() / 2, 5, 0x0080FF);
    }
}
