package hantonik.fbp.animation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.particle.FBPPlacingAnimationParticle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.ModList;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPPlacingAnimationManager {
    private static final Map<BlockPos, FBPPlacingAnimationParticle> ACTIVE_ANIMATIONS = Maps.newHashMap();
    private static final Set<BlockPos> HIDDEN_BLOCKS = Sets.newConcurrentHashSet();

    public static void addAnimation(ClientWorld level, BlockState state, BlockPos pos, LivingEntity placer, Hand hand) {
        if (ModList.get().isLoaded("a_good_place"))
            return;

        if (FancyBlockParticles.CONFIG.animations.isEnabled() && FancyBlockParticles.CONFIG.isBlockAnimationsEnabled(state.getBlock())) {
            if (!state.is(BlockTags.BEDS) && !(state.getBlock() instanceof DoublePlantBlock) && !(state.getBlock() instanceof DoorBlock) && (!state.hasProperty(ChestBlock.TYPE) || state.getValue(ChestBlock.TYPE) != ChestType.SINGLE)) {
                if (Minecraft.getInstance().cameraEntity.position().distanceTo(new Vector3d(pos.getX(), pos.getY(), pos.getZ())) <= Minecraft.getInstance().options.renderDistance * 16) {
                    FBPPlacingAnimationParticle animation = new FBPPlacingAnimationParticle(level, state, pos, placer, hand);
                    FBPPlacingAnimationParticle oldAnimation = ACTIVE_ANIMATIONS.put(pos, animation);

                    if (oldAnimation != null) {
                        oldAnimation.remove();

                        ACTIVE_ANIMATIONS.remove(pos);
                    }

                    hideBlock(pos);

                    Minecraft.getInstance().particleEngine.add(animation);
                }
            }
        }
    }

    public static void hideBlock(BlockPos pos) {
        HIDDEN_BLOCKS.add(pos);
    }

    public static void showBlock(BlockPos pos, boolean removeAnimation) {
        if (HIDDEN_BLOCKS.remove(pos))
            markBlockForRender(pos);

        if (removeAnimation) {
            FBPPlacingAnimationParticle animation = ACTIVE_ANIMATIONS.remove(pos);

            if (animation != null)
                animation.remove();
        }
    }

    public static boolean isHidden(BlockPos pos) {
        return HIDDEN_BLOCKS.contains(pos);
    }

    private static void markBlockForRender(BlockPos pos) {
        ClientWorld level = Minecraft.getInstance().level;
        BlockState state = level.getBlockState(pos);

        level.sendBlockUpdated(pos, state, state, 3);
    }

    public static void clear() {
        for (BlockPos pos : HIDDEN_BLOCKS)
            showBlock(pos, true);

        HIDDEN_BLOCKS.clear();
        ACTIVE_ANIMATIONS.clear();
    }
}
