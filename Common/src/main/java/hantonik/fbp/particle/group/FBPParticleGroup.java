package hantonik.fbp.particle.group;

import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.particle.api.IFBPAnimationParticleRenderer;
import hantonik.fbp.particle.api.IFBPLegacyParticleRenderer;
import hantonik.fbp.particle.api.IFBPParticleRenderer;
import hantonik.fbp.renderer.state.FBPTerrainParticleRenderState;
import hantonik.fbp.renderer.state.FBPAnimationParticleRenderState;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

public class FBPParticleGroup extends ParticleGroup<Particle> {
    private final ParticleRenderType particleRenderType;
    
    private final FBPTerrainParticleRenderState particleRenderState = new FBPTerrainParticleRenderState();
    private final FBPAnimationParticleRenderState animationRenderState = new FBPAnimationParticleRenderState();

    public FBPParticleGroup(ParticleEngine engine, ParticleRenderType particleRenderType) {
        super(engine);

        this.particleRenderType = particleRenderType;
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        if (this.particleRenderType == FBPConstants.FBP_TERRAIN_RENDER) {
            return (nodeCollector, _) -> nodeCollector.submitCustomGeometry(new PoseStack(), RenderTypes.translucentMovingBlock(), (_, consumer) -> {
                for (var particle : this.particles) {
                    if (particle instanceof IFBPLegacyParticleRenderer particleRenderer) {
                        if (frustum.pointInFrustum(particle.x, particle.y, particle.z)) {
                            try {
                                particleRenderer.render(consumer, camera, partialTick);
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
                if (particle instanceof IFBPParticleRenderer particleRenderer) {
                    if (frustum.pointInFrustum(particle.x, particle.y, particle.z)) {
                        try {
                            particleRenderer.extract(this.particleRenderState, camera, partialTick);
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

            return this.particleRenderState;
        } else if (this.particleRenderType == FBPConstants.FBP_ANIMATION_RENDER) {
            for (var particle : this.particles) {
                if (particle instanceof IFBPAnimationParticleRenderer particleRenderer) {
                    if (frustum.pointInFrustum(particle.x, particle.y, particle.z)) {
                        try {
                            particleRenderer.extract(this.animationRenderState, camera, partialTick);
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

            return this.animationRenderState;
        } else
            throw new IllegalStateException();
    }
}
