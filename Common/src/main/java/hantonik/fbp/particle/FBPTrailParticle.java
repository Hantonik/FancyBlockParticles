package hantonik.fbp.particle;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.TrailParticle;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FBPTrailParticle extends TrailParticle implements IKillableParticle {
    private final Vector3f rotation;

    private final float multiplier;

    private final float scaleAlpha;

    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    protected FBPTrailParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, Vec3 target, int color) {
        super(level, x, y, z, xd, yd, zd, target, color, FBPConstants.FBP_PARTICLE_SPRITE.get());

        this.quadSize = FancyBlockParticles.CONFIG.trail.getSizeMultiplier() * (FancyBlockParticles.CONFIG.trail.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.6F, 1.0F) : 1.0F) * 3.0F;
        this.scaleAlpha = this.quadSize * 0.82F;

        this.rotation = new Vector3f((float) Math.toRadians(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1.0D : -1.0D), (float) Math.toRadians(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1.0D : -1.0D), (float) Math.toRadians(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1.0D : -1.0D));

        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.trail.getMinLifetime(), FancyBlockParticles.CONFIG.trail.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.trail.getMinLifetime(), FancyBlockParticles.CONFIG.trail.getMaxLifetime()) + 0.5F);

        this.multiplier = FancyBlockParticles.CONFIG.trail.isRandomFadingSpeed() ? Mth.clamp(FBPConstants.RANDOM.nextFloat(0.5F, 0.9F), 0.6F, 0.8F) : 0.75F;

        this.scale(1.0F);
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        var size = this.quadSize / 10.0F;
        this.setBoundingBox(new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

        return this;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastSize = this.quadSize;
        this.lastAlpha = this.alpha;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.trail.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!FancyBlockParticles.CONFIG.trail.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                if (this.age < this.lifetime) {
                    var progress = (double) this.age / (double) this.lifetime;

                    this.x = Mth.lerp(easeInOutCubic(progress), this.x, this.target.x);
                    this.y = Mth.lerp(easeInOutCubic(progress), this.y, this.target.y);
                    this.z = Mth.lerp(easeInOutCubic(progress), this.z, this.target.z);
                }

                if (this.age + 10 >= this.lifetime) {
                    this.quadSize *= 0.9F * this.multiplier;

                    if (this.alpha >= 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.7F * this.multiplier;

                    if (this.alpha < 0.01D)
                        this.remove();
                }
            }
        }
    }

    private static double easeInOutCubic(double input) {
        return input < 0.5D ? (4.0D * input * input * input) : (1.0D - Math.pow(-2.0D * input + 2.0D, 3.0D) / 2.0D);
    }

    @Override
    public void killParticle() {
        this.killToggle = true;
    }

    @Override
    public Layer getLayer() {
        return Layer.TERRAIN;
    }

    @Override
    public int getLightColor(float partialTick) {
        var factor = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);

        var i = super.getLightColor(partialTick);
        var j = i & 255;
        var k = i >> 16 & 255;

        j = Math.min((int) (factor * 15.0F * 16.0F) + j, 240);

        i = j | k << 16;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera info, float partialTick) {
        var u = this.sprite.getU(1.1F / 4.0F);
        var v = this.sprite.getV(1.1F / 4.0F);

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize) / 70.0F;
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        this.putCube(renderState, (float) posX, (float) posY, (float) posZ, scale, this.rotation, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(QuadParticleRenderState renderState, float x, float y, float z, float scale, Vector3f rotationRad, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        var brightness = 1.0F;

        float red;
        float green;
        float blue;

        var rotation = new Quaternionf().rotateXYZ(rotationRad.x, rotationRad.y, rotationRad.z);

        var rotationX = new Quaternionf().rotateX(rotationRad.x);
        var rotationY = new Quaternionf().rotateY(rotationRad.y);
        var rotationZ = new Quaternionf().rotateZ(rotationRad.z);

        for (var i = 0; i < FBPConstants.CUBE_NORMALS.length; i++) {
            var normal = FBPConstants.CUBE_NORMALS[i].rotate(rotation, new Vector3f());
            var face = new Vector3f(normal).mul(scale).add(x, y, z);
            var faceRotation = new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, 1.0F), normal);

            if (i < 2)
                rotationY.mul(faceRotation, faceRotation);
            else if (i < 4)
                rotationZ.mul(faceRotation, faceRotation);
            else
                rotationX.mul(faceRotation, faceRotation);

            red = rCol * brightness;
            green = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.95F;

            renderState.add(this.getLayer(), face.x, face.y, face.z, faceRotation.x, faceRotation.y, faceRotation.z, faceRotation.w, scale, u, u, v, v, ARGB.colorFromFloat(alpha, red, green, blue), light);
        }
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<TrailParticleOption> {
        @Nullable
        @Override
        public Particle createParticle(TrailParticleOption type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.trail.isSpawnWhileFrozen())
                return null;

            return new FBPTrailParticle(level, x, y, z, xd, yd, zd, type.target(), type.color());
        }
    }
}
