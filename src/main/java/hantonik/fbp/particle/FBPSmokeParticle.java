package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.Stream;

public class FBPSmokeParticle extends SmokeParticle implements IKillableParticle {
    private final Vector3d[] rotatedCube;

    private final float multiplier;

    private float scaleAlpha;
    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    public FBPSmokeParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, float scale) {
        super(level, x, y, z, xd, yd, zd, scale, new IAnimatedSprite() {
            @Override
            public TextureAtlasSprite get(int age, int lifetime) {
                return FBPConstants.FBP_PARTICLE_SPRITE.get();
            }

            @Override
            public TextureAtlasSprite get(Random random) {
                return FBPConstants.FBP_PARTICLE_SPRITE.get();
            }
        });

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.quadSize = scale * 10.0F;
        this.scaleAlpha = this.quadSize * 0.85F;

        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.smoke.getMinLifetime(), FancyBlockParticles.CONFIG.smoke.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.smoke.getMinLifetime(), FancyBlockParticles.CONFIG.smoke.getMaxLifetime()) + 0.5D);

        BlockState state = level.getBlockState(new BlockPos(x, y, z));

        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            this.xd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);
            this.yd = FBPConstants.RANDOM.nextDouble() * 0.175D;
            this.zd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);

            this.quadSize *= 0.65F;
            this.scaleAlpha = this.quadSize * 0.5F;

            this.gravity *= 0.25F;
        } else if (state.getBlock() instanceof TorchBlock) {
            this.xd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);
            this.zd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);

            this.xd *= 0.925D;
            this.yd = 0.005D;
            this.zd *= 0.925D;

            this.rCol = 0.275F;
            this.gCol = 0.275F;
            this.bCol = 0.275F;

            this.quadSize *= 0.45F;
            this.scaleAlpha = this.quadSize * 0.75F;

            this.lifetime = (int) (FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.smoke.getMinLifetime(), FancyBlockParticles.CONFIG.smoke.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.smoke.getMinLifetime(), FancyBlockParticles.CONFIG.smoke.getMaxLifetime()) + 0.5D) * 0.75D);
        } else
            this.yd *= 0.935D;

        this.quadSize = FancyBlockParticles.CONFIG.smoke.getSizeMultiplier() * (FancyBlockParticles.CONFIG.smoke.isRandomSize() ? this.quadSize : 1.0F);

        this.alpha = 1.0F;
        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();

        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        float angleY = this.random.nextFloat();

        for (int i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.multiplier = FancyBlockParticles.CONFIG.smoke.isRandomFadingSpeed() ? MathHelper.clamp((float) FBPConstants.RANDOM.nextDouble(0.425D, 1.15D), 0.5432F, 1.0F) : 0.75F;

        this.scale(1.0F);
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        float size = this.quadSize / 20.0F;
        this.setBoundingBox(new AxisAlignedBB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

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

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!FancyBlockParticles.CONFIG.smoke.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                if (this.age >= this.lifetime) {
                    this.quadSize *= 0.9F * this.multiplier;

                    if (this.alpha >= 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.76F * this.multiplier;

                    if (this.alpha < 0.01D)
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
    protected int getLightColor(float partialTick) {
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

        Vector3d[] cube = new Vector3d[this.rotatedCube.length];

        for (int i = 0; i < cube.length; i++) {
            Vector3d corner = new Vector3d(
                    this.rotatedCube[i].x,
                    this.rotatedCube[i].y,
                    this.rotatedCube[i].z
            );

            corner = corner.scale(scale / 20.0F);
            corner = corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        this.putCube(builder, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(IVertexBuilder builder, Vector3d[] cube, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        float brightness = 1.0F;

        float red;
        float green;
        float blue;

        for (int i = 0; i < cube.length; i += 4) {
            Vector3d vec0 = cube[i];
            Vector3d vec1 = cube[i + 1];
            Vector3d vec2 = cube[i + 2];
            Vector3d vec3 = cube[i + 3];

            red = rCol * brightness;
            green = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.875F;

            this.addVertex(builder, vec0, u, v, light, red, green, blue, alpha);
            this.addVertex(builder, vec1, u, v, light, red, green, blue, alpha);
            this.addVertex(builder, vec2, u, v, light, red, green, blue, alpha);
            this.addVertex(builder, vec3, u, v, light, red, green, blue, alpha);
        }
    }

    private void addVertex(IVertexBuilder builder, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        builder.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @RequiredArgsConstructor
    public static class Provider implements IParticleFactory<BasicParticleType> {
        private final float scale;

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.smoke.isSpawnWhileFrozen())
                return null;

            FBPSmokeParticle particle = new FBPSmokeParticle(level, x, y, z, xd, yd, zd, this.scale);

            particle.setColor(MathHelper.clamp(particle.rCol + 0.1F, 0.1F, 1.0F), MathHelper.clamp(particle.gCol + 0.1F, 0.1F, 1.0F), MathHelper.clamp(particle.bCol + 0.1F, 0.1F, 1.0F));

            return particle;
        }
    }
}
