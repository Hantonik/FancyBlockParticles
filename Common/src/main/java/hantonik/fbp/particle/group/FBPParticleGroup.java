package hantonik.fbp.particle.group;

import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.particle.api.IFBPRendererParticle;
import hantonik.fbp.renderer.state.FBPParticleRenderState;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;

public class FBPParticleGroup extends ParticleGroup<SingleQuadParticle> {
    private final ParticleRenderType particleRenderType;
    private final FBPParticleRenderState renderState = new FBPParticleRenderState();

    public FBPParticleGroup(ParticleEngine engine, ParticleRenderType particleRenderType) {
        super(engine);

        this.particleRenderType = particleRenderType;
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        if (this.particleRenderType == FBPConstants.FBP_TERRAIN_RENDER) {
            return (nodeCollector, state) -> nodeCollector.submitCustomGeometry(new PoseStack(), RenderType.translucentMovingBlock(), (pose, consumer) -> {
                for (var particle : this.particles) {
                    if (particle instanceof IFBPRendererParticle rendererParticle) {
                        if (frustum.pointInFrustum(particle.x, particle.y, particle.z)) {
                            try {
                                rendererParticle.render(consumer, camera, partialTick);
                            } catch (Throwable throwable) {
                                var report = CrashReport.forThrowable(throwable, "Rendering Particle");
                                var reportCategory = report.addCategory("Particle being rendered");

                                reportCategory.setDetail("Particle", particle::toString);
                                reportCategory.setDetail("Particle Type", this.particleRenderType::toString);

                                throw new ReportedException(report);
                            }
                        }
                    }
                }
            });
        } else if (this.particleRenderType == FBPConstants.FBP_PARTICLE_RENDER) {
            for (var particle : this.particles) {
                if (particle instanceof IFBPRendererParticle rendererParticle) {
                    if (frustum.pointInFrustum(particle.x, particle.y, particle.z)) {
                        try {
                            rendererParticle.extract(this.renderState, camera, partialTick);
                        } catch (Throwable throwable) {
                            var report = CrashReport.forThrowable(throwable, "Rendering Particle");
                            var reportCategory = report.addCategory("Particle being rendered");

                            reportCategory.setDetail("Particle", particle::toString);
                            reportCategory.setDetail("Particle Type", this.particleRenderType::toString);

                            throw new ReportedException(report);
                        }
                    }
                }
            }

            return this.renderState;
        } else
            throw new IllegalStateException();
    }
}
