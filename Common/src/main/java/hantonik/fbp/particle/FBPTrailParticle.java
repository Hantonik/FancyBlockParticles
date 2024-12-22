package hantonik.fbp.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.platform.Services;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrailParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class FBPTrailParticle extends TrailParticle implements IKillableParticle {
    private final Vector3d rotation;

    private final float multiplier;

    private final float scaleAlpha;

    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    protected FBPTrailParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, Vec3 target, int color) {
        super(level, x, y, z, xd, yd, zd, target, color);

        this.quadSize = FancyBlockParticles.CONFIG.trail.getSizeMultiplier() * (FancyBlockParticles.CONFIG.trail.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.6F, 1.0F) : 1.0F) * 3.0F;
        this.scaleAlpha = this.quadSize * 0.82F;

        this.rotation = new Vector3d(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1.0D : -1.0D, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1.0D : -1.0D, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1.0D : -1.0D);

        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();
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
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_PARTICLE_RENDER;
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
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        var u = this.sprite.getU(1.1F / 4.0F);
        var v = this.sprite.getV(1.1F / 4.0F);

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize) / 70.0F;
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.setShader(Services.CLIENT.getParticleTranslucentShader());

        RenderSystem.enableCull();

        this.putCube(buffer, u, v, posX, posY, posZ, scale, this.rotation, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(VertexConsumer buffer, float u, float v, double xPos, double yPos, double zPos, double scale, Vector3d rotation, int light, float rCol, float gCol, float bCol, float alpha) {
        var radX = (float) Math.toRadians(rotation.x);
        var radY = (float) Math.toRadians(rotation.y);
        var radZ = (float) Math.toRadians(rotation.z);

        var brightness = 1.0F;

        float red;
        float green;
        float blue;

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var v1 = FBPRenderHelper.rotate(FBPConstants.CUBE[i], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);
            var v2 = FBPRenderHelper.rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);
            var v3 = FBPRenderHelper.rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);
            var v4 = FBPRenderHelper.rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);

            red = rCol * brightness;
            green = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.95F;

            addVertex(buffer, v1, u, v, light, red, green, blue, alpha);
            addVertex(buffer, v2, u, v, light, red, green, blue, alpha);
            addVertex(buffer, v3, u, v, light, red, green, blue, alpha);
            addVertex(buffer, v4, u, v, light, red, green, blue, alpha);
        }
    }

    private void addVertex(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        buffer.addVertex((float) pos.x, (float) pos.y, (float) pos.z).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setLight(light);
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<TrailParticleOption> {
        @Nullable
        @Override
        public Particle createParticle(TrailParticleOption type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.trail.isSpawnWhileFrozen())
                return null;

            return new FBPTrailParticle(level, x, y, z, xd, yd, zd, type.target(), type.color());
        }
    }
}
