package hantonik.fbp.mixin;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class MixinBlock {
    @Inject(at = @At("HEAD"), method = "shouldRenderFace", cancellable = true)
    private static void shouldRenderFace(BlockState state, BlockGetter level, BlockPos otherPos, Direction facing, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (FBPPlacingAnimationManager.isHidden(pos))
            callback.setReturnValue(true);
    }
}
