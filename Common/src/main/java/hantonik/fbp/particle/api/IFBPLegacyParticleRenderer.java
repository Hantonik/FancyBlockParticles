package hantonik.fbp.particle.api;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;

public interface IFBPLegacyParticleRenderer {
    void render(VertexConsumer consumer, Camera camera, float partialTick);
}
