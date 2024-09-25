package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
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

    protected FBPDripParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, float rCol, float gCol, float bCol, float alpha, int lightLevel, BlockState state, @Nullable SoundEvent sound) {
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

        var quads = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(this.state).getQuads(this.state, null, this.random);

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

                var max = FancyBlockParticles.CONFIG.drip.getSizeMultiplier() * 0.3F;

                if (!this.onGround) {
                    if (this.age < this.lifetime) {
                        if (this.quadSize < max) {
                            this.quadSize += 0.03F * this.multiplier;

                            if (this.quadSize > max) {
                                this.quadSize = max;

                                if (this.sound != null) {
                                    var volume = Mth.randomBetween(this.random, 0.3F, 1.0F);

                                    this.level.playLocalSound(this.x, this.y, this.z, this.sound, SoundSource.BLOCKS, volume, 1.0F, false);
                                }
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

                var state = this.level.getBlockState(new BlockPos(this.x, this.y, this.z).relative(Direction.DOWN));

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

    private boolean isInFluid(AABB box, Tag.Named<Fluid> fluid) {
        if (!this.touchingUnloadedChunk()) {
            box = box.deflate(0.001D);

            var minX = Mth.floor(box.minX);
            var maxX = Mth.ceil(box.maxX);
            var minY = Mth.floor(box.minY);
            var maxY = Mth.ceil(box.maxY);
            var minZ = Mth.floor(box.minZ);
            var maxZ = Mth.ceil(box.maxZ);

            for (var x = minX; x < maxX; x++) {
                for (var y = minY; y < maxY; y++) {
                    for (var z = minZ; z < maxZ; z++) {
                        var pos = new BlockPos(x, y, z);
                        var fluidState = this.level.getFluidState(pos);

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
        var center = this.getBoundingBox().inflate(1.0D).getCenter();

        return !this.level.isLoaded(new BlockPos(center.x, center.y, center.z));
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
            var vec = Entity.collideBoundingBoxHeuristically(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, CollisionContext.empty(), new RewindableStream<>(Stream.empty()));

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
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    public int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);
        var j = 0;

        var pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        var blockLight = this.lightLevel == -1 ? (float) this.state.getLightEmission() : Mth.clamp(this.lightLevel, 0, 15);

        return (int) Mth.lerp(blockLight / 15.0F, i == 0 ? j : i, 240);
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        var u0 = 0.0F;
        var v0 = 0.0F;

        if (!FancyBlockParticles.CONFIG.global.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F * 16.0F);
            v0 = this.sprite.getV(this.vo / 4.0F * 16.0F);
        }

        var u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F * 16.0F);
        var v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F * 16.0F);

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        var width = Mth.lerp(partialTick, this.lastSize, this.quadSize) / 10.0F;
        var height = Mth.lerp(partialTick, this.lastHeight, this.height) / 10.0F;

        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        var smoothRotation = new Vec3(0.0D, this.angleY, 0.0D);

        FBPRenderHelper.renderCubeShaded(buffer, new Vec2[] { new Vec2(u1, v1), new Vec2(u1, v0), new Vec2(u0, v0), new Vec2(u0, v1) }, posX, posY, posZ, width, height, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<SimpleParticleType> {
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
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.drip.isSpawnWhileFrozen())
                return null;

            return new FBPDripParticle(level, x, y, z, xd, yd, zd, this.rCol, this.gCol, this.bCol, this.alpha, this.lightLevel, this.state, this.sound);
        }
    }
}
