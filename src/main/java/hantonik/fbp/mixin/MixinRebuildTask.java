package hantonik.fbp.mixin;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkRenderDispatcher.ChunkRender.RebuildTask.class)
public abstract class MixinRebuildTask {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), method = "compile")
    private BlockState getBlockState(ChunkRenderCache instance, BlockPos pos) {
        return FBPPlacingAnimationManager.isHidden(pos) ? Blocks.AIR.defaultBlockState() : instance.getBlockState(pos);
    }
}
