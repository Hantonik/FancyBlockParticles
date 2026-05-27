package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AltModelBlockRendererImpl.class)
public abstract class MixinAltModelBlockRendererImpl {
    @Final
    @Shadow
    private BlockPos.MutableBlockPos scratchPos;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"), method = "shouldCullFace")
    private boolean shouldCullFace(BlockState state, BlockState faceState, Direction face, Operation<Boolean> original) {
        if (FBPPlacingAnimationManager.isHidden(this.scratchPos))
            return true;

        return original.call(state, faceState, face);
    }
}
