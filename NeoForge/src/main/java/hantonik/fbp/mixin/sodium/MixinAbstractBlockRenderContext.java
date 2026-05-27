package hantonik.fbp.mixin.sodium;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public abstract class MixinAbstractBlockRenderContext {
    @Final
    @Shadow
    private BlockPos.MutableBlockPos cachedPositionObject;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;setWithOffset(Lnet/minecraft/core/Vec3i;Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos$MutableBlockPos;", shift = At.Shift.AFTER), method = "shouldDrawSide", cancellable = true)
    public void shouldDrawSide(Direction facing, CallbackInfoReturnable<Boolean> callback) {
        if (FBPPlacingAnimationManager.isHidden(this.cachedPositionObject))
            callback.setReturnValue(true);
    }
}
