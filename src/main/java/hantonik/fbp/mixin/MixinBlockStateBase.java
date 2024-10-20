package hantonik.fbp.mixin;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinBlockStateBase {
    @Inject(at = @At("HEAD"), method = "isSolidRender", cancellable = true)
    public void isSolidRender(IBlockReader level, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (FBPPlacingAnimationManager.isHidden(pos))
            callback.setReturnValue(false);
    }
}
