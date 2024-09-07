package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class FBPLavaParticle extends LavaParticle implements IKillableParticle {
    private final Vector3d[] rotatedCube;

    private final float multiplier;

    private float startSize;
    private float scaleAlpha;
    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    public FBPLavaParticle(ClientWorld level, double x, double y, double z) {
        super(level, x, y, z);

        this.rCol = 1.0F;
        this.gCol = 0.6F;
        this.bCol = 0.0F;

        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();
        this.quadSize = FancyBlockParticles.CONFIG.flame.getSizeMultiplier() * (FancyBlockParticles.CONFIG.flame.isRandomSize() ? (float) FBPConstants.RANDOM.nextDouble(0.6D, 1.0D) : 1.0F) * 4.5F;
        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()) + 0.5D);
        this.gravity = 0.75F * 0.85F;

        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        float angleY = this.random.nextFloat();

        for (int i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.multiplier = FancyBlockParticles.CONFIG.flame.isRandomFadingSpeed() ? (float) FBPConstants.RANDOM.nextDouble(0.9875D, 1.0D) : 1.0F;

        this.scale(1.0F);
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        this.startSize = this.quadSize;
        this.scaleAlpha = this.quadSize * 0.35F;

        float size = this.quadSize / 80.0F;
        this.setBoundingBox(new AxisAlignedBB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

        return this;
    }

    @Override
    public float getQuadSize(float scale) {
        float factor = (this.age + scale) / this.lifetime;

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

                    if (this.alpha > 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= this.multiplier * 0.9F;

                    if (this.alpha <= 0.01D)
                        this.remove();
                }

                if (!this.removed)
                    if (FBPConstants.RANDOM.nextDouble() > (double) this.age / (double) this.lifetime && FBPConstants.RANDOM.nextDouble() > 0.3D)
                        this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);

                this.yd -= 0.04D * this.gravity;

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
    }

    @Override
    public void killParticle() {
        this.killToggle = true;
    }

    @Override
    public void move(double x, double y, double z) {
        double xo = x;
        double yo = y;
        double zo = z;

        if ((x != 0.0D || y != 0.0D || z != 0.0D) && x * x + y * y + z * z < MathHelper.square(100.0F)) {
            Vector3d vec = Entity.collideBoundingBoxHeuristically(null, new Vector3d(x, y, z), this.getBoundingBox(), this.level, ISelectionContext.empty(), new ReuseableStream<>(Stream.empty()));

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

        AxisAlignedBB box = this.getBoundingBox();
        this.y = (box.minY + box.maxY) / 2.0D;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return FBPConstants.FBP_PARTICLE_RENDER;
    }

    @Override
    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        int j = 0;

        BlockPos pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @Override
    public void render(IVertexBuilder builder, ActiveRenderInfo info, float partialTick) {
        float u = this.sprite.getU(1.1F / 4.0F * 16.0F);
        float v = this.sprite.getV(1.1F / 4.0F * 16.0F);

        double posX = MathHelper.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        double posY = MathHelper.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        double posZ = MathHelper.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        float scale = MathHelper.lerp(partialTick, this.lastSize, this.quadSize);
        float alpha = MathHelper.lerp(partialTick, this.lastAlpha, this.alpha);

        int light = this.getLightColor(partialTick);

        if (this.age >= this.lifetime)
            this.gCol = Math.min(0.6F, scale / this.startSize);

        Vector3d[] cube = new Vector3d[this.rotatedCube.length];

        for (int i = 0; i < cube.length; i++) {
            Vector3d corner = new Vector3d(
                    this.rotatedCube[i].x,
                    this.rotatedCube[i].y,
                    this.rotatedCube[i].z
            );

            corner = corner.scale(scale / 80.0F);
            corner = corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        this.putCube(builder, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(IVertexBuilder builder, Vector3d[] cube, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        float brightness = 1.0F;

        float red;
        float greed;
        float blue;

        for (int i = 0; i < cube.length; i += 4) {
            Vector3d vec0 = cube[i];
            Vector3d vec1 = cube[i + 1];
            Vector3d vec2 = cube[i + 2];
            Vector3d Vector3d = cube[i + 3];

            red = rCol * brightness;
            greed = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.95F;

            this.addVertex(builder, vec0, u, v, light, red, greed, blue, alpha);
            this.addVertex(builder, vec1, u, v, light, red, greed, blue, alpha);
            this.addVertex(builder, vec2, u, v, light, red, greed, blue, alpha);
            this.addVertex(builder, Vector3d, u, v, light, red, greed, blue, alpha);
        }
    }

    private void addVertex(IVertexBuilder builder, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        builder.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    public static class Provider implements IParticleFactory<BasicParticleType> {
        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.flame.isSpawnWhileFrozen())
                return null;

            return new FBPLavaParticle(level, x, y - 0.06D, z);
        }
    }
}
