package hantonik.fbp.particle.api;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.renderer.state.FBPParticleRenderState;
import net.minecraft.client.Camera;

public interface IFBPRendererParticle {
    default void render(VertexConsumer consumer, Camera camera, float partialTick) {

    }

    default void extract(FBPParticleRenderState renderState, Camera camera, float partialTick) {

    }
}
