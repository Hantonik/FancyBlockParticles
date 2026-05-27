package hantonik.fbp.particle.api;

import hantonik.fbp.renderer.state.FBPAnimationParticleRenderState;
import net.minecraft.client.Camera;

public interface IFBPAnimationParticleRenderer {
    void extract(FBPAnimationParticleRenderState renderState, Camera camera, float partialTick);
}
