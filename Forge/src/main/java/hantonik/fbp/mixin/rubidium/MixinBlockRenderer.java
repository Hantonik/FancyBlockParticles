package hantonik.fbp.mixin.rubidium;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class MixinBlockRenderer {
    @Inject(at = @At("HEAD"), method = "renderModel", cancellable = true)
    public void renderModel(BlockAndTintGetter world, BlockState state, BlockPos pos, BlockPos origin, BakedModel model, ChunkModelBuilder buffers, boolean cull, long seed, IModelData modelData, CallbackInfoReturnable<Boolean> callback) {
        if (FBPPlacingAnimationManager.isHidden(pos))
            callback.setReturnValue(false);
    }
}
