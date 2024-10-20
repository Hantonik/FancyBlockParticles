package hantonik.fbp.mixin;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;updateBlockStateFromTag(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"), method = "place", locals = LocalCapture.CAPTURE_FAILHARD)
    public void place(BlockItemUseContext oldContext, CallbackInfoReturnable<ActionResultType> callback, BlockItemUseContext context, BlockState oldState) {
        if (ModList.get().isLoaded("a_good_place"))
            return;

        World level = context.getLevel();

        if (level instanceof ClientWorld) {
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);

            if (state.is(oldState.getBlock()))
                FBPPlacingAnimationManager.addAnimation((ClientWorld) level, state, pos, context.getPlayer(), context.getHand());
        }
    }
}
