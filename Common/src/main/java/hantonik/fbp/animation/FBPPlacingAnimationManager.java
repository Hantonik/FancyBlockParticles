package hantonik.fbp.animation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.particle.FBPPlacingAnimationParticle;
import hantonik.fbp.platform.Services;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPPlacingAnimationManager {
    private static final Map<BlockPos, FBPPlacingAnimationParticle> ACTIVE_ANIMATIONS = Maps.newHashMap();
    private static final Set<BlockPos> HIDDEN_BLOCKS = Sets.newConcurrentHashSet();

    public static void addAnimation(ClientLevel level, BlockState state, BlockPos pos, LivingEntity placer, InteractionHand hand) {
        if (Services.PLATFORM.isModLoaded("a_good_place"))
            return;

        if (FancyBlockParticles.CONFIG.animations.isEnabled() && FancyBlockParticles.CONFIG.isBlockAnimationsEnabled(state.getBlock())) {
            if (!state.is(BlockTags.BEDS) && !(state.getBlock() instanceof DoublePlantBlock) && !(state.getBlock() instanceof DoorBlock) && (!state.hasProperty(ChestBlock.TYPE) || state.getValue(ChestBlock.TYPE) != ChestType.SINGLE)) {
                if (Minecraft.getInstance().cameraEntity.position().distanceTo(pos.getCenter()) <= Minecraft.getInstance().options.renderDistance().get() * 16) {
                    var animation = new FBPPlacingAnimationParticle(level, state, pos, placer, hand);
                    var oldAnimation = ACTIVE_ANIMATIONS.put(pos, animation);

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
            var animation = ACTIVE_ANIMATIONS.remove(pos);

            if (animation != null)
                animation.remove();
        }
    }

    public static boolean isHidden(BlockPos pos) {
        return HIDDEN_BLOCKS.contains(pos);
    }

    private static void markBlockForRender(BlockPos pos) {
        var level = Minecraft.getInstance().level;
        var state = level.getBlockState(pos);

        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    public static void clear() {
        for (var pos : HIDDEN_BLOCKS)
            showBlock(pos, true);

        HIDDEN_BLOCKS.clear();
        ACTIVE_ANIMATIONS.clear();
    }
}
