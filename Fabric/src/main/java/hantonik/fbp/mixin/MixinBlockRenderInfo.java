//package hantonik.fbp.mixin;
//
//import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import hantonik.fbp.animation.FBPPlacingAnimationManager;
//import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.level.block.state.BlockState;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//
//@Mixin(BlockRenderInfo.class)
//public abstract class MixinBlockRenderInfo {
//    @Final
//    @Shadow
//    private BlockPos.MutableBlockPos searchPos;
//
//    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"), method = "shouldDrawSide")
//    private boolean shouldDrawFace(BlockState state, BlockState faceState, Direction face, Operation<Boolean> original) {
//        if (FBPPlacingAnimationManager.isHidden(this.searchPos))
//            return true;
//
//        return original.call(state, faceState, face);
//    }
//}
