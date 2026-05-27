package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public abstract class MixinModelBlockRenderer {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"), method = "shouldRenderFace")
    private static boolean shouldRenderFace(BlockState state, BlockState neighborState, Direction direction, Operation<Boolean> original, @Local(argsOnly = true, name = "neighborPos") BlockPos neighborPos) {
        if (FBPPlacingAnimationManager.isHidden(neighborPos))
            return true;

        return original.call(state, neighborState, direction);
    }
}
