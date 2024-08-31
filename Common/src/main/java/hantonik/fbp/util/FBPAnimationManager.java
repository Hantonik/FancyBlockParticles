package hantonik.fbp.util;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPAnimationManager {
    private static final Map<BlockPos, AnimationEntry> ANIMATIONS = Maps.newHashMap();

    public static boolean isAnimating(BlockPos pos) {
        return ANIMATIONS.containsKey(pos);
    }

    public record AnimationEntry(BlockState original, @Nullable BlockEntity entity) {}
}
