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
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public class FBPDripParticle extends DripParticle implements IKillableParticle {
    private final BlockState state;
    @Nullable
    private final SoundEvent sound;
    private final int lightLevel;

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

    protected FBPDripParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, float rCol, float gCol, float bCol, float alpha, int lightLevel, BlockState state, @Nullable SoundEvent sound) {
        super(level, x, y, z, state.getFluidState().getType());

        this.xd = FBPConstants.RANDOM.nextDouble(xd - 0.0005D, xd + 0.0005D);
        this.yd = FBPConstants.RANDOM.nextDouble(yd - 0.2D, yd - 0.1D);
        this.zd = FBPConstants.RANDOM.nextDouble(zd - 0.0005D, zd + 0.0005D);

        this.x += FBPConstants.RANDOM.nextDouble(-0.01D, 0.01D);
        this.y += FBPConstants.RANDOM.nextDouble(0.01D, 0.025D);
        this.z += FBPConstants.RANDOM.nextDouble(-0.01D, 0.01D);

        this.rCol = rCol;
        this.gCol = gCol;
        this.bCol = bCol;
        this.alpha = alpha;

        this.state = state;
        this.sound = sound;
        this.lightLevel = lightLevel;

        List<BakedQuad> quads = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(this.state).getQuads(this.state, null, this.random);

        if (quads.isEmpty())
            this.sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(this.state);
        else
            this.sprite = quads.get(0).getSprite();

        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.drip.getMinLifetime(), FancyBlockParticles.CONFIG.drip.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.drip.getMinLifetime(), FancyBlockParticles.CONFIG.drip.getMaxLifetime()) + 0.5D);

        this.targetSize = (float) Math.max(FBPConstants.RANDOM.nextDouble(FancyBlockParticles.CONFIG.drip.getSizeMultiplier() - 0.1D, FancyBlockParticles.CONFIG.drip.getSizeMultiplier() + 0.1D) * 2.0D, 0.1D) * (FancyBlockParticles.CONFIG.drip.isRandomSize() ? (float) FBPConstants.RANDOM.nextDouble(0.7D, 1.0D) : 1.0F);
        this.quadSize = 0.0F;
        this.gravity *= FancyBlockParticles.CONFIG.drip.getGravityMultiplier();

        this.hasPhysics = true;

        this.angleY = FBPConstants.RANDOM.nextDouble() * 45.0D;

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
        
        this.multiplier = FancyBlockParticles.CONFIG.drip.isRandomFadingSpeed() ? (float) FBPConstants.RANDOM.nextDouble(0.7D, 1.0D) : 1.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;
        this.lastHeight = this.height;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.drip.isEnabled())
            this.remove();
        
        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                this.age++;

                float max = FancyBlockParticles.CONFIG.drip.getSizeMultiplier() * 0.3F;

                if (!this.onGround) {
                    if (this.age < this.lifetime) {
                        if (this.quadSize < max) {
                            this.quadSize += 0.03F * this.multiplier;

                            if (this.quadSize > max) {
                                this.quadSize = max;

                                if (this.sound != null)
                                    this.level.playLocalSound(this.x, this.y, this.z, this.sound, SoundCategory.BLOCKS, 0.3F + this.level.random.nextFloat() * 2.0F / 3.0F, 1.0F, false);
                            }

                            this.height = this.quadSize;
                        }
                    } else {
                        this.quadSize *= 0.9F * this.multiplier;
                        this.height = this.quadSize;

                        if (this.alpha >= 0.01D)
                            this.alpha *= 0.7F * this.multiplier;

                        if (this.alpha < 0.01D)
                            this.remove();
                    }
                }

                if (this.quadSize >= max || this.age >= this.lifetime) {
                    this.yd -= 0.13D * this.gravity;

                    this.move(this.xd, this.yd, this.zd);

                    this.xd *= 0.6D;
                    this.yd *= 1.00025D;
                    this.zd *= 0.6D;

                    if (this.onGround && this.age < this.lifetime) {
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
                }

                BlockState state = this.level.getBlockState(new BlockPos(this.x, this.y, this.z).relative(Direction.DOWN));

                if (this.type.defaultFluidState().is(FluidTags.LAVA)) {
                    if (this.isInFluid(this.getBoundingBox(), FluidTags.WATER)) {
                        this.remove();

                        Minecraft.getInstance().particleEngine.add(new FBPSmokeParticle.Provider(this.quadSize / 5.0F).createParticle(ParticleTypes.SMOKE, this.level, this.x, this.y, this.z, 0.0D, 0.05D, 0.0D));
                    }
                } else {
                    if (this.isInFluid(this.getBoundingBox(), FluidTags.LAVA) || ((state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state)) && this.onGround)) {
                        this.remove();

                        Minecraft.getInstance().particleEngine.add(new FBPSmokeParticle.Provider(this.quadSize / 5.0F).createParticle(ParticleTypes.SMOKE, this.level, this.x, this.y, this.z, 0.0D, 0.05D, 0.0D));
                    }
                }
            }
        }
    }

    private boolean isInFluid(AxisAlignedBB box, ITag.INamedTag<Fluid> fluid) {
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

                        if (fluidState.is(fluid))
                            if (fluidState.getHeight(this.level, pos) + y >= box.minY)
                                return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean touchingUnloadedChunk() {
        Vector3d center = this.getBoundingBox().inflate(1.0D).getCenter();

        return !this.level.isLoaded(new BlockPos(center.x, center.y, center.z));
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
    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        int j = 0;

        BlockPos pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        float blockLight = this.lightLevel == -1 ? (float) this.state.getLightEmission() : MathHelper.clamp(this.lightLevel, 0, 15);

        return (int) MathHelper.lerp(blockLight / 15.0F, i == 0 ? j : i, 240);
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

        float width = MathHelper.lerp(partialTick, this.lastSize, this.quadSize) / 10.0F;
        float height = MathHelper.lerp(partialTick, this.lastHeight, this.height) / 10.0F;

        float alpha = MathHelper.lerp(partialTick, this.lastAlpha, this.alpha);

        int light = this.getLightColor(partialTick);

        Vector3d smoothRotation = new Vector3d(0.0D, this.angleY, 0.0D);

        FBPRenderHelper.renderCubeShaded(builder, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, posX, posY, posZ, width, height, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    @RequiredArgsConstructor
    public static class Provider implements IParticleFactory<BasicParticleType> {
        private final BlockState state;
        @Nullable
        private final SoundEvent sound;
        private final float rCol;
        private final float gCol;
        private final float bCol;
        private final float alpha;
        private final int lightLevel;

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.drip.isSpawnWhileFrozen())
                return null;

            return new FBPDripParticle(level, x, y, z, xd, yd, zd, this.rCol, this.gCol, this.bCol, this.alpha, this.lightLevel, this.state, this.sound);
        }
    }
}
