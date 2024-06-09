package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

public class FBPWhiteSmokeParticle extends WhiteSmokeParticle implements IKillableParticle {
    private final Vector3d[] rotatedCube;

    private final float multiplier;

    private final float scaleAlpha;

    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    protected FBPWhiteSmokeParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, float scale) {
        super(level, x, y, z, xd, yd, zd, scale, new SpriteSet() {
            @Override
            public TextureAtlasSprite get(int age, int lifetime) {
                return FBPConstants.FBP_PARTICLE_SPRITE.get();
            }

            @Override
            public TextureAtlasSprite get(RandomSource random) {
                return FBPConstants.FBP_PARTICLE_SPRITE.get();
            }
        });

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.yd *= 0.935D;

        this.quadSize = FancyBlockParticles.CONFIG.smoke.getSizeMultiplier() * (FancyBlockParticles.CONFIG.smoke.isRandomSize() ? scale * 10.0F : 1.0F);
        this.scaleAlpha = this.quadSize * 0.85F;

        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.smoke.getMinLifetime(), FancyBlockParticles.CONFIG.smoke.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.smoke.getMinLifetime(), FancyBlockParticles.CONFIG.smoke.getMaxLifetime()) + 0.5F);

        this.alpha = FBPConstants.RANDOM.nextFloat(0.6F, 0.9F);
        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();

        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        var angleY = this.random.nextFloat();

        for (var i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.multiplier = FancyBlockParticles.CONFIG.smoke.isRandomFadingSpeed() ? Mth.clamp(FBPConstants.RANDOM.nextFloat(0.425F, 1.15F), 0.5432F, 1.0F) : 0.75F;

        this.rCol = FBPConstants.RANDOM.nextFloat(Math.max(1.0F, this.rCol * 1.1F) - 0.1F, 1.0F);
        this.gCol = FBPConstants.RANDOM.nextFloat(Math.max(1.0F, this.gCol * 1.1F) - 0.1F, 1.0F);
        this.bCol = FBPConstants.RANDOM.nextFloat(Math.max(1.0F, this.bCol * 1.1F) - 0.1F, 1.0F);

        this.scale(1.0F);
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        var size = this.quadSize / 20.0F;
        this.setBoundingBox(new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

        return this;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.smoke.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused() && (!FancyBlockParticles.CONFIG.global.isFreezeEffect() || this.killToggle)) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.smoke.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                this.age++;

            if (this.age >= this.lifetime) {
                this.quadSize *= 0.9F * this.multiplier;

                if (this.alpha > 0.01D && this.quadSize <= this.scaleAlpha)
                    this.alpha *= 0.76F * this.multiplier;

                if (this.alpha <= 0.01D)
                    this.remove();
            }

            this.yd += 0.004D;

            this.move(this.xd, this.yd, this.zd);

            if (this.y == this.yo) {
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
    protected int getLightColor(float partialTick) {
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

        var cube = new Vector3d[this.rotatedCube.length];

        for (var i = 0; i < cube.length; i++) {
            var corner = new Vector3d();

            corner.x = this.rotatedCube[i].x;
            corner.y = this.rotatedCube[i].y;
            corner.z = this.rotatedCube[i].z;

            corner.mul(scale / 20.0F);
            corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        this.putCube(buffer, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(VertexConsumer buffer, Vector3d[] cube, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        var brightness = 1.0F;

        float red;
        float greed;
        float blue;

        for (var i = 0; i < cube.length; i += 4) {
            var vec0 = cube[i];
            var vec1 = cube[i + 1];
            var vec2 = cube[i + 2];
            var vec3 = cube[i + 3];

            red = rCol * brightness;
            greed = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.875F;

            this.addVertex(buffer, vec0, u, v, light, red, greed, blue, alpha);
            this.addVertex(buffer, vec1, u, v, light, red, greed, blue, alpha);
            this.addVertex(buffer, vec2, u, v, light, red, greed, blue, alpha);
            this.addVertex(buffer, vec3, u, v, light, red, greed, blue, alpha);
        }
    }

    private void addVertex(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        buffer.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final float scale;

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.smoke.isSpawnWhileFrozen())
                return null;

            var particle = new FBPWhiteSmokeParticle(level, x, y, z, xd, yd, zd, this.scale);

            particle.setColor(Mth.clamp(particle.rCol + 0.1F, 0.1F, 1.0F), Mth.clamp(particle.gCol + 0.1F, 0.1F, 1.0F), Mth.clamp(particle.bCol + 0.1F, 0.1F, 1.0F));

            return particle;
        }
    }
}
