package hantonik.fbp.mixin;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class MixinBlock {
    @Inject(at = @At("HEAD"), method = "shouldRenderFace", cancellable = true)
    private static void shouldRenderFace(BlockState state, IBlockReader level, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> callback) {
        if (FBPPlacingAnimationManager.isHidden(pos.relative(facing)))
            callback.setReturnValue(true);
    }
}
