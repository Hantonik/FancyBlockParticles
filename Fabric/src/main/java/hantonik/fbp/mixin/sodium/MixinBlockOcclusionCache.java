package hantonik.fbp.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BlockOcclusionCache.class, remap = false)
public abstract class MixinBlockOcclusionCache {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;", shift = At.Shift.AFTER, remap = true), method = "shouldDrawSide", cancellable = true)
    public void shouldDrawSide(BlockState selfState, BlockGetter view, BlockPos selfPos, Direction facing, CallbackInfoReturnable<Boolean> callback, @Local BlockPos.MutableBlockPos otherPos) {
        if (FBPPlacingAnimationManager.isHidden(otherPos))
            callback.setReturnValue(true);
    }
}
