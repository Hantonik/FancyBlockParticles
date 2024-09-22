package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

public class FBPLavaParticle extends LavaParticle implements IKillableParticle {
    private final Vector3d[] rotatedCube;

    private final float multiplier;

    private float startSize;
    private float scaleAlpha;
    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    public FBPLavaParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);

        this.rCol = 1.0F;
        this.gCol = 0.6F;
        this.bCol = 0.0F;

        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();
        this.quadSize = FancyBlockParticles.CONFIG.flame.getSizeMultiplier() * (FancyBlockParticles.CONFIG.flame.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.6F, 1.0F) : 1.0F) * 4.5F;
        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()) + 0.5F);
        this.gravity *= 0.85F;

        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        var angleY = this.random.nextFloat();

        for (var i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.multiplier = FancyBlockParticles.CONFIG.flame.isRandomFadingSpeed() ? FBPConstants.RANDOM.nextFloat(0.9875F, 1.0F) : 1.0F;

        this.scale(1.0F);
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        this.startSize = this.quadSize;
        this.scaleAlpha = this.quadSize * 0.35F;

        var size = this.quadSize / 80.0F;
        this.setBoundingBox(new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

        return this;
    }

    @Override
    public float getQuadSize(float scale) {
        var factor = (this.age + scale) / this.lifetime;

        return this.quadSize * (1.0F - factor * factor * 0.5F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.flame.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!FancyBlockParticles.CONFIG.flame.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                if (this.age >= this.lifetime) {
                    this.quadSize *= this.multiplier * 0.9F;

                    if (this.alpha >= 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= this.multiplier * 0.9F;

                    if (this.alpha < 0.01D)
                        this.remove();
                }

                if (!this.removed)
                    if (FBPConstants.RANDOM.nextFloat() > (float) this.age / (float) this.lifetime && FBPConstants.RANDOM.nextFloat() > 0.3F)
                        this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);

                this.yd -= 0.04D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                    this.xd *= 1.1D;
                    this.zd *= 1.1D;
                }

                this.xd *= 0.95D;
                this.yd *= 0.95D;
                this.zd *= 0.95D;

                if (this.onGround) {
                    this.xd *= 0.9D;
                    this.zd *= 0.9D;
                }
            }
        }
    }

    @Override
    public void killParticle() {
        this.killToggle = true;
    }

    @Override
    public void move(double x, double y, double z) {
        var xo = x;
        var yo = y;
        var zo = z;

        if ((x != 0.0D || y != 0.0D || z != 0.0D) && x * x + y * y + z * z < Mth.square(100.0D)) {
            var vec = Entity.collideBoundingBox(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, List.of());

            x = vec.x;
            y = vec.y;
            z = vec.z;
        }

        if (x != 0.0D || y != 0.0D || z != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            this.setLocationFromBoundingbox();
        }

        this.onGround = y != yo && yo < 0.0D;

        if (x != xo)
            this.xd = 0.0D;

        if (z != zo)
            this.zd = 0.0D;
    }

    @Override
    protected void setLocationFromBoundingbox() {
        super.setLocationFromBoundingbox();

        var box = this.getBoundingBox();
        this.y = (box.minY + box.maxY) / 2.0D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_PARTICLE_RENDER;
    }

    @Override
    public int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);
        var j = 0;

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

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize);
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        if (this.age >= this.lifetime)
            this.gCol = Math.min(0.6F, scale / this.startSize);

        var cube = new Vector3d[this.rotatedCube.length];

        for (var i = 0; i < cube.length; i++) {
            var corner = new Vector3d();

            corner.x = this.rotatedCube[i].x;
            corner.y = this.rotatedCube[i].y;
            corner.z = this.rotatedCube[i].z;

            corner.mul(scale / 80.0F);
            corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        this.putCube(buffer, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(VertexConsumer buffer, Vector3d[] cube, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        var brightness = 1.0F;

        float red;
        float green;
        float blue;

        for (var i = 0; i < cube.length; i += 4) {
            var vec0 = cube[i];
            var vec1 = cube[i + 1];
            var vec2 = cube[i + 2];
            var vec3 = cube[i + 3];

            red = rCol * brightness;
            green = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.95F;

            this.addVertex(buffer, vec0, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec1, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec2, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec3, u, v, light, red, green, blue, alpha);
        }
    }

    private void addVertex(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        buffer.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.flame.isSpawnWhileFrozen())
                return null;

            return new FBPLavaParticle(level, x, y - 0.06D, z);
        }
    }
}
