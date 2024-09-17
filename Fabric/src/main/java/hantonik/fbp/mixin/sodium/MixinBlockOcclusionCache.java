package hantonik.fbp.mixin.sodium;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BlockOcclusionCache.class, remap = false)
public abstract class MixinBlockOcclusionCache {
    @Final
    @Shadow
    private BlockPos.MutableBlockPos cachedPositionObject;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;", shift = At.Shift.AFTER), method = "shouldDrawSide", cancellable = true)
    public void shouldDrawSide(BlockState selfState, BlockGetter view, BlockPos selfPos, Direction facing, CallbackInfoReturnable<Boolean> callback) {
        if (FBPPlacingAnimationManager.isHidden(this.cachedPositionObject))
            callback.setReturnValue(true);
    }
}
