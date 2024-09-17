package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.platform.Services;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;updateBlockStateFromTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"), method = "place")
    public void place(BlockPlaceContext oldContext, CallbackInfoReturnable<InteractionResult> callbackInfo, @Local(ordinal = 1) BlockPlaceContext context, @Local(ordinal = 0) BlockState oldState) {
        if (Services.PLATFORM.isModLoaded("a_good_place"))
            return;

        if (context.getLevel() instanceof ClientLevel level) {
            var pos = context.getClickedPos();
            var state = level.getBlockState(pos);

            if (state.is(oldState.getBlock()))
                FBPPlacingAnimationManager.addAnimation(level, state, pos, context.getPlayer(), context.getHand());
        }
    }
}
