package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RainParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class FBPRainParticle extends RainParticle implements IKillableParticle {
    private final double angleY;

    private final float uo;
    private final float vo;

    private final float multiplier;

    private final float targetSize;

    private float height;

    private float lastAlpha;
    private float lastSize;
    private float lastHeight;

    private boolean killToggle;
    private boolean visible;

    public FBPRainParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.sprite = sprite;

        this.lifetime = FBPConstants.RANDOM.nextInt(50, 70);

        this.targetSize = Math.max((float) FBPConstants.RANDOM.nextDouble(FancyBlockParticles.CONFIG.rain.getSizeMultiplier() - 0.1D, FancyBlockParticles.CONFIG.rain.getSizeMultiplier() + 0.1D) * 4.0F, 0.1F) * (FancyBlockParticles.CONFIG.rain.isRandomSize() ? (float) FBPConstants.RANDOM.nextDouble(0.7D, 1.0D) : 1.0F);
        this.quadSize = 0.0F;
        this.gravity = 0.025F * FancyBlockParticles.CONFIG.rain.getGravityMultiplier();

        this.alpha = 0.0F;

        this.hasPhysics = true;

        this.angleY = FBPConstants.RANDOM.nextDouble() * 45.0D;

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.multiplier = FancyBlockParticles.CONFIG.rain.isRandomFadingSpeed() ? (float) FBPConstants.RANDOM.nextDouble(0.85D, 1.0D) : 1.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;
        this.lastHeight = this.height;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.rain.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                this.age++;

                if (this.y < Minecraft.getInstance().player.getY() - (Minecraft.getInstance().options.renderDistance * 9.0D))
                    this.remove();

                if (!this.onGround) {
                    if (this.age < this.lifetime) {
                        float max = FancyBlockParticles.CONFIG.rain.getSizeMultiplier() * 0.5F;

                        if (this.quadSize < max) {
                            this.quadSize += 0.05F * this.multiplier;

                            if (this.quadSize > max)
                                this.quadSize = max;

                            this.height = this.quadSize;
                        }

                        if (this.alpha < 0.6F) {
                            this.alpha += 0.085F * this.multiplier;

                            if (this.alpha > 0.6F)
                                this.alpha = 0.6F;
                        }
                    } else
                        this.remove();
                }

                this.yd -= 0.04D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                this.yd *= 1.00025D;

                if (this.onGround) {
                    this.xd = 0.0D;
                    this.yd = -0.25D;
                    this.zd = 0.0D;

                    if (this.height > 0.075F)
                        this.height *= 0.725F;

                    if (this.quadSize < this.targetSize) {
                        this.quadSize += this.targetSize / 10.0F;

                        if (this.quadSize > this.targetSize)
                            this.quadSize = this.targetSize;
                    }

                    if (this.quadSize >= this.targetSize / 2.0F) {
                        this.alpha *= 0.75F * this.multiplier;

                        if (this.alpha < 0.01F)
                            this.remove();
                    }
                }

                BlockState state = this.level.getBlockState(new BlockPos(this.x, this.y, this.z).relative(Direction.DOWN));

                if (this.isInLava(this.getBoundingBox()) || ((state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state)) && this.onGround)) {
                    this.remove();

                    Minecraft.getInstance().particleEngine.add(new FBPSmokeParticle.Provider(this.quadSize / 5.0F).createParticle(ParticleTypes.SMOKE, this.level, this.x, this.y, this.z, 0.0D, 0.05D, 0.0D));
                }
            }
        }

        Vector3d color = this.level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition(), 0.0F);

        this.rCol = (float) color.x;
        this.gCol = (float) MathHelper.clamp(color.y + 0.1D, 0.1D, 1.0D);
        this.bCol = (float) MathHelper.clamp(color.y + 0.5D, 0.5D, 1.0D);

        if (Minecraft.getInstance().cameraEntity.position().distanceTo(new Vector3d(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) > FancyBlockParticles.CONFIG.rain.getSimulationDistance() * 16)
            this.remove();

        this.visible = Minecraft.getInstance().cameraEntity.position().distanceTo(new Vector3d(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) <= Math.min(FancyBlockParticles.CONFIG.rain.getRenderDistance(), Minecraft.getInstance().options.renderDistance) * 16;
    }

    private boolean isInLava(AxisAlignedBB box) {
        if (!this.touchingUnloadedChunk()) {
            box = box.deflate(0.001D);

            int minX = MathHelper.floor(box.minX);
            int maxX = MathHelper.ceil(box.maxX);
            int minY = MathHelper.floor(box.minY);
            int maxY = MathHelper.ceil(box.maxY);
            int minZ = MathHelper.floor(box.minZ);
            int maxZ = MathHelper.ceil(box.maxZ);

            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        FluidState fluidState = this.level.getFluidState(pos);

                        if (fluidState.is(FluidTags.LAVA))
                            if (fluidState.getHeight(this.level, pos) + y >= box.minY)
                                return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean touchingUnloadedChunk() {
        AxisAlignedBB box = this.getBoundingBox().inflate(1.0D);

        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        return !this.level.hasChunksAt(minX, 0, minZ, maxX, 0, maxZ);
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
        return FBPConstants.FBP_TERRAIN_RENDER;
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
        if (!this.visible)
            return;

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

        float width = MathHelper.lerp(partialTick, this.lastSize, this.quadSize) / 10.0F;
        float height = MathHelper.lerp(partialTick, this.lastHeight, this.height) / 10.0F;

        float alpha = MathHelper.lerp(partialTick, this.lastAlpha, this.alpha);

        int light = this.getLightColor(partialTick);

        Vector3d smoothRotation = new Vector3d(0.0D, this.angleY, 0.0D);

        FBPRenderHelper.renderCubeShaded(builder, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, posX, posY + height, posZ, width, height, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    @RequiredArgsConstructor
    public static class Provider implements IParticleFactory<BasicParticleType> {
        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect())
                return null;

            return new FBPRainParticle(level, x, y, z, 0.1D, -FBPConstants.RANDOM.nextDouble(0.65D, 0.85D), 0.1D, Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WATER.defaultBlockState()));
        }
    }
}
