package hantonik.fbp.renderer.state;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@RequiredArgsConstructor
public class FBPAnimationParticleRenderState implements ParticleGroupRenderState {
    private final List<FBPAnimationRenderState> animations = Lists.newArrayList();

    public void add(PoseStack stack, MovingBlockRenderState blockRenderState) {
        this.animations.add(new FBPAnimationRenderState(stack, blockRenderState));
    }

    @Override
    public void clear() {
        this.animations.clear();
    }

    @Override
    public void submit(SubmitNodeCollector nodeCollector, CameraRenderState state) {
        for (var animation : this.animations)
            nodeCollector.submitMovingBlock(animation.stack, animation.renderState);
    }

    private record FBPAnimationRenderState(PoseStack stack, MovingBlockRenderState renderState) {}
}
