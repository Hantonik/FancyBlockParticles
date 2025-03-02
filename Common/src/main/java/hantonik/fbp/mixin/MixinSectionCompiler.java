package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {
    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"), method = "compile")
    private RenderShape getBlockState(RenderShape original, @Local(ordinal = 2) BlockPos pos) {
        if (original != RenderShape.INVISIBLE && FBPPlacingAnimationManager.isHidden(pos))
            return RenderShape.INVISIBLE;

        return original;
    }

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSolidRender()Z"), method = "compile")
    private boolean getBlockState(boolean original, @Local(ordinal = 2) BlockPos pos) {
        if (original && FBPPlacingAnimationManager.isHidden(pos))
            return false;

        return original;
    }
}
