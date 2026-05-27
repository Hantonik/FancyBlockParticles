package hantonik.fbp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
    @Inject(at = @At("HEAD"), method = "renderHitOutline", cancellable = true)
    private void renderHitOutline(PoseStack poseStack, VertexConsumer builder, double camX, double camY, double camZ, BlockOutlineRenderState state, int color, float width, CallbackInfo callback) {
        if (!FancyBlockParticles.CONFIG.animations.isRenderOutline() && FBPPlacingAnimationManager.isHidden(state.pos()))
            callback.cancel();
    }
}
