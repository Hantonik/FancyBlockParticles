package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.CampfireParticle;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class FBPCampfireSmokeParticle extends CampfireParticle implements IKillableParticle {
    private final Vector3d[] rotatedCube;

    private final float uo;
    private final float vo;

    private final float multiplier;

    private final float targetAlpha;
    private final float scaleAlpha;
    private float lastAlpha;

    private final float targetSize;
    private float lastSize;

    private boolean killToggle;

    protected FBPCampfireSmokeParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, boolean isSignal, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, isSignal);

        this.sprite = sprite;

        this.rCol = 0.62F;
        this.gCol = 0.59F;
        this.bCol = 0.56F;

        this.targetAlpha = FancyBlockParticles.CONFIG.campfireSmoke.getTransparency();
        this.alpha = 0.0F;

        this.targetSize = FancyBlockParticles.CONFIG.campfireSmoke.getSizeMultiplier() * 6.0F * (FancyBlockParticles.CONFIG.campfireSmoke.isRandomSize() ? this.quadSize : 1.0F);
        this.scaleAlpha = this.targetSize * 0.85F;
        this.quadSize = 0.0F;

        this.hasPhysics = true;

        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        float angleY = this.random.nextFloat();

        for (int i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.multiplier = FancyBlockParticles.CONFIG.campfireSmoke.isRandomFadingSpeed() ? MathHelper.clamp((float) FBPConstants.RANDOM.nextDouble(0.85D, 1.1D), 0.95F, 1.0F) : 0.95F;

        this.scale(1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.campfireSmoke.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                this.age++;

                if (this.age >= this.lifetime) {
                    this.quadSize *= 0.98F * this.multiplier;

                    if (this.alpha >= 0.001D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.96F * this.multiplier;

                    if (this.alpha < 0.001D)
                        this.remove();
                } else {
                    if (this.quadSize < this.targetSize) {
                        this.quadSize += 0.17F * this.multiplier;

                        if (this.quadSize > this.targetSize)
                            this.quadSize = this.targetSize;
                    }

                    if (this.alpha < this.targetAlpha) {
                        this.alpha += 0.1F * this.multiplier;

                        if (this.alpha > this.targetAlpha)
                            this.alpha = this.targetAlpha;
                    }
                }

                this.xd = this.xd + this.random.nextDouble() * (this.random.nextBoolean() ? 1.0D : -1.0D) / 5000.0D;
                this.yd = this.yd - this.gravity;
                this.zd = this.zd + this.random.nextDouble() * (this.random.nextBoolean() ? 1.0D : -1.0D) / 5000.0D;

                this.move(this.xd, this.yd, this.zd);

                if (this.y == this.yo) {
                    this.xd *= 1.1D;
                    this.zd *= 1.1D;
                }

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
        float u0 = 0.0F;
        float v0 = 0.0F;

        if (!FancyBlockParticles.CONFIG.global.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F * 16.0F);
            v0 = this.sprite.getV(this.vo / 4.0F * 16.0F);
        }

        float u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F * 16.0F);
        float v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F * 16.0F);

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

        this.putCube(builder, cube, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    private void putCube(IVertexBuilder builder, Vector3d[] cube, Vector2f[] uv, int light, float rCol, float gCol, float bCol, float alpha, boolean cartoon) {
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

            if (cartoon) {
                this.addVertex(builder, vec0, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(builder, vec1, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(builder, vec2, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(builder, vec3, uv[0].x, uv[0].y, light, red, green, blue, alpha);
            } else {
                this.addVertex(builder, vec0, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(builder, vec1, uv[1].x, uv[1].y, light, red, green, blue, alpha);
                this.addVertex(builder, vec2, uv[2].x, uv[2].y, light, red, green, blue, alpha);
                this.addVertex(builder, vec3, uv[3].x, uv[3].y, light, red, green, blue, alpha);
            }
        }
    }

    private void addVertex(IVertexBuilder builder, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        builder.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @RequiredArgsConstructor
    public static class Provider implements IParticleFactory<BasicParticleType> {
        private final boolean isSignal;

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.campfireSmoke.isSpawnWhileFrozen())
                return null;

            return new FBPCampfireSmokeParticle(level, x, y, z, xd, yd, zd, this.isSignal, Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.QUARTZ_BLOCK.defaultBlockState()));
        }
    }
}
