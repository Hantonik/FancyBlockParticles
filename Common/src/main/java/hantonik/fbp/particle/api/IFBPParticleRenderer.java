package hantonik.fbp.particle.api;

import hantonik.fbp.renderer.state.FBPTerrainParticleRenderState;
import net.minecraft.client.Camera;

public interface IFBPParticleRenderer {
    void extract(FBPTerrainParticleRenderState renderState, Camera camera, float partialTick);
}
