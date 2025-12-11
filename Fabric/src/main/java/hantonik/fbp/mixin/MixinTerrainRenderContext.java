//package hantonik.fbp.mixin;
//
//import hantonik.fbp.animation.FBPPlacingAnimationManager;
//import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
//import net.minecraft.client.renderer.block.model.BlockStateModel;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.block.state.BlockState;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(TerrainRenderContext.class)
//public abstract class MixinTerrainRenderContext {
//    @Inject(at = @At("HEAD"), method = "bufferModel", cancellable = true)
//    public void bufferModel(BlockStateModel model, BlockState state, BlockPos pos, CallbackInfo callback) {
//        if (FBPPlacingAnimationManager.isHidden(pos))
//            callback.cancel();
//    }
//}
