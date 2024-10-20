package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
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
import java.util.stream.Stream;

public class FBPFlameParticle extends FlameParticle implements IKillableParticle {
    private final Vector3d startPos;
    private final Vector3d[] rotatedCube;

    private final boolean isSoulFire;

    private final float multiplier;

    private boolean hasChild;

    private float startSize;
    private float scaleAlpha;
    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    public FBPFlameParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, boolean isSoulFire, boolean hasChild) {
        super(level, x, y, z, xd, yd, zd);

        this.yd = -0.00085D;
        this.gravity = -0.05F;

        this.isSoulFire = isSoulFire;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 0.0F;

        if (this.isSoulFire) {
            this.rCol = 0.2F;
            this.bCol = 0.9F;
        }

        this.alpha = 1.0F;

        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();
        this.quadSize = FancyBlockParticles.CONFIG.flame.getSizeMultiplier() * (FancyBlockParticles.CONFIG.flame.isRandomSize() ? (float) FBPConstants.RANDOM.nextDouble(0.6D, 1.0D) : 1.0F) * 2.5F;
        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()) + 0.5D);

        this.startPos = new Vector3d(x, y, z);
        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        float angleY = this.random.nextFloat();

        for (int i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.hasChild = hasChild;

        this.multiplier = FancyBlockParticles.CONFIG.flame.isRandomFadingSpeed() ? (float) FBPConstants.RANDOM.nextDouble(0.9875D, 1.0D) : 1.0F;

        this.scale(1.0F);
    }

    @Override
    public FBPFlameParticle scale(float scale) {
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
                    this.quadSize *= this.multiplier * 0.95F;

                    if (this.alpha >= 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= this.multiplier * 0.95F;

                    BlockState state = this.level.getBlockState(new BlockPos(this.x, this.y, this.z));

                    if (this.alpha < 0.01D)
                        this.remove();
                    else if (this.alpha <= 0.325D && this.hasChild && state.getBlock() instanceof TorchBlock) {
                        this.hasChild = false;

                        Minecraft.getInstance().particleEngine.add(new FBPFlameParticle(this.level, this.startPos.x, this.startPos.y, this.startPos.z, 0, 0, 0, this.isSoulFire, false));
                    }
                }

                this.yd -= 0.02D * this.gravity;

                this.move(0.0D, this.yd, 0.0D);

                this.yd *= 0.95D;

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
        float factor = MathHelper.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);

        int i = super.getLightColor(partialTick);
        int j = i & 255;
        int k = i >> 16 & 255;

        j = Math.min((int) (factor * 15.0F * 16.0F) + j, 240);

        i = j | k << 16;

        BlockPos pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return  i == 0 ? j : i;
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

        if (this.age >= this.lifetime) {
            this.gCol = this.isSoulFire ? Math.min(1.0F, (scale / this.startSize) * 1.2F) : scale / this.startSize;

            if (this.isSoulFire)
                this.bCol = Math.min(1.0F, (scale / this.startSize) * 1.2F);
        }

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
        float green;
        float blue;

        for (int i = 0; i < cube.length; i += 4) {
            Vector3d vec0 = cube[i];
            Vector3d vec1 = cube[i + 1];
            Vector3d vec2 = cube[i + 2];
            Vector3d Vector3d = cube[i + 3];

            red = rCol * brightness;
            green = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.95F;

            this.addVertex(builder, vec0, u, v, light, red, green, blue, alpha);
            this.addVertex(builder, vec1, u, v, light, red, green, blue, alpha);
            this.addVertex(builder, vec2, u, v, light, red, green, blue, alpha);
            this.addVertex(builder, Vector3d, u, v, light, red, green, blue, alpha);
        }
    }

    private void addVertex(IVertexBuilder builder, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        builder.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @Nullable
    private static FBPFlameParticle create(ClientWorld level, double x, double y, double z, double xd, double zd, boolean isSoulFire) {
        if (FancyBlockParticles.CONFIG.global.isFreezeEffect())
            return null;

        BlockState state = level.getBlockState(new BlockPos(x, y, z));

        if (state.getBlock() instanceof TorchBlock)
            y += 0.04D;

        return new FBPFlameParticle(level, x, y - 0.06D, z, xd, FBPConstants.RANDOM.nextDouble() * 0.025D, zd, isSoulFire, !(state.getBlock() instanceof TorchBlock));
    }

    @RequiredArgsConstructor
    public static class Provider implements IParticleFactory<BasicParticleType> {
        private final boolean isSoulFire;

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            return create(level, x, y, z, xd, zd, this.isSoulFire);
        }
    }
}
