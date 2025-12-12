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
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"), method = "tesselateWithAO")
    private boolean shouldRenderFaceWithAO(BlockState state, BlockState faceState, Direction face, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
        if (FBPPlacingAnimationManager.isHidden(pos))
            return false;

        return original.call(state, faceState, face);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"), method = "tesselateWithoutAO")
    private boolean shouldRenderFaceWithoutAO(BlockState state, BlockState faceState, Direction face, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
        if (FBPPlacingAnimationManager.isHidden(pos))
            return true;

        return original.call(state, faceState, face);
    }
}
