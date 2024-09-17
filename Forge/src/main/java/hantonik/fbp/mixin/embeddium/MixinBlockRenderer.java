package hantonik.fbp.mixin.embeddium;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import org.embeddedt.embeddium.api.render.chunk.BlockRenderContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildBuffers;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.BlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class MixinBlockRenderer {
    @Inject(at = @At("HEAD"), method = "renderModel", cancellable = true)
    public void renderModel(BlockRenderContext ctx, ChunkBuildBuffers buffers, CallbackInfo callback) {
        if (FBPPlacingAnimationManager.isHidden(ctx.pos()))
            callback.cancel();
    }
}
