package hantonik.fbp.particle;

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
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FBPSnowParticle extends WaterDropParticle implements IKillableParticle {
    private Vec3 rotation;
    private Vec3 rotationStep;
    private Vec3 lastRotation;

    private final float multiplier;

    private final float scaleAlpha;

    private final float targetSize;

    private final float uo;
    private final float vo;

    private float lastAlpha;
    private float lastSize;

    private double lastXSpeed;
    private double lastZSpeed;

    private boolean wasFrozen;
    private boolean wasInWater;

    private boolean killToggle;
    private boolean visible;

    public FBPSnowParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.sprite = sprite;

        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.snow.getMinLifetime(), FancyBlockParticles.CONFIG.snow.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.snow.getMinLifetime(), FancyBlockParticles.CONFIG.snow.getMaxLifetime()) + 0.5F);

        this.quadSize = Math.max(FBPConstants.RANDOM.nextFloat(FancyBlockParticles.CONFIG.snow.getSizeMultiplier() - 0.1F, FancyBlockParticles.CONFIG.snow.getSizeMultiplier() + 0.1F), 0.1F) * (FancyBlockParticles.CONFIG.snow.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.7F, 1.0F) : 1.0F);
        this.targetSize = this.quadSize;

        this.scale(1.0F);

        this.gravity = FancyBlockParticles.CONFIG.snow.getGravityMultiplier();

        this.hasPhysics = true;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.alpha = 0.0F;

        var rx = FBPConstants.RANDOM.nextDouble();
        var ry = FBPConstants.RANDOM.nextDouble();
        var rz = FBPConstants.RANDOM.nextDouble();

        this.rotationStep = new Vec3(rx > 0.5D ? 1.0D : -1.0D, ry > 0.5D ? 1.0D : -1.0D, rz > 0.5D ? 1.0D : -1.0D);

        this.lastRotation = Vec3.ZERO;
        this.rotation = this.rotationStep;

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.scaleAlpha = this.quadSize * 0.75F;
        this.quadSize = 0.0F;

        this.multiplier = FancyBlockParticles.CONFIG.snow.isRandomFadingSpeed() ? FBPConstants.RANDOM.nextFloat(0.8F, 1.0F) : 1.0F;
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        var size = this.quadSize / 10.0F;

        this.setBoundingBox(new AABB(this.x - size, this.y, this.z - size, this.x + size, this.y + 2 * size, this.z + size));

        return this;
    }

    @Override
    public void tick() {
        if (FancyBlockParticles.CONFIG.snow.isBounceOffWalls()) {
            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!Minecraft.getInstance().isPaused() && this.age > 0) {
                    if (!this.wasFrozen && (Math.abs(this.xd) > 0.00001D || Math.abs(this.zd) > 0.00001D)) {
                        if (this.xo == this.x)
                            this.xd = -this.lastXSpeed * 0.625D;
                        if (this.zo == this.z)
                            this.zd = -this.lastZSpeed * 0.625D;
                    } else
                        this.wasFrozen = false;
                }
            } else
                this.wasFrozen = true;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        this.lastRotation = this.rotation;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.snow.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (this.y < Minecraft.getInstance().player.getY() - (Minecraft.getInstance().options.renderDistance().get() * 16.0D))
                    this.remove();

                if (!FancyBlockParticles.CONFIG.snow.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                this.rotation = this.rotation.add(this.rotationStep.scale(FancyBlockParticles.CONFIG.snow.getRotationMultiplier() * 5.0F));

                var pos = new BlockPos(this.x, this.y, this.z);
                var biome = this.level.getBiome(pos);

                if (this.age >= this.lifetime || !Services.CLIENT.coldEnoughToSnow(biome, pos, this.level)) {
                    this.quadSize *= 0.75F * this.multiplier;

                    if (this.alpha >= 0.01F && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.65F * this.multiplier;

                    if (this.alpha < 0.01F) {
                        this.remove();

                        if (Services.CLIENT.warmEnoughToRain(biome, pos, this.level))
                            Minecraft.getInstance().particleEngine.add(new FBPRainParticle.Provider().createParticle(ParticleTypes.RAIN.getType(), this.level, x, y, z, 0.0D, 0.0D, 0.0D));
                    }
                } else {
                    if (!this.wasInWater) {
                        if (this.quadSize < this.targetSize) {
                            this.quadSize += 0.075F * this.multiplier;

                            if (this.quadSize > this.targetSize)
                                this.quadSize = this.targetSize;
                        }

                        if (this.alpha < 1.0F) {
                            this.alpha += 0.045F * this.multiplier;

                            if (this.alpha > 1.0F)
                                this.alpha = 1.0F;
                        }
                    }
                }

                if (!this.onGround)
                    this.yd -= (this.wasInWater ? 0.02D : 0.04D) * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                if (Math.abs(this.xd) > 0.00001D)
                    this.lastXSpeed = this.xd;
                if (Math.abs(this.zd) > 0.00001D)
                    this.lastZSpeed = this.zd;

                if (this.onGround && FancyBlockParticles.CONFIG.snow.isRestOnFloor())
                    this.rotation = this.rotation.with(Direction.Axis.X, Math.round(this.rotation.x / 90.0D) * 90.0D).with(Direction.Axis.Z, Math.round(this.rotation.z / 90.0D) * 90.0D);

                this.xd *= 0.98D;

                if (this.yd < -0.2D)
                    this.yd *= 0.75D;

                this.zd *= 0.98D;

                var state = this.level.getBlockState(pos.relative(Direction.DOWN));

                if (this.isInFluid(this.getBoundingBox(), FluidTags.LAVA) || ((state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state)) && this.onGround)) {
                    this.remove();

                    Minecraft.getInstance().particleEngine.add(new FBPSmokeParticle.Provider(this.quadSize / 5.0F).createParticle(ParticleTypes.SMOKE, this.level, this.x, this.y, this.z, 0.0D, 0.05D, 0.0D));
                }

                if (this.isInFluid(this.getBoundingBox(), FluidTags.WATER)) {
                    if (FancyBlockParticles.CONFIG.snow.isWaterPhysics()) {
                        this.xd *= 0.3D;
                        this.yd *= 0.05D;
                        this.zd *= 0.3D;

                        this.quadSize *= 0.98F * this.multiplier;

                        this.wasInWater = true;

                        if (this.alpha >= 0.01F && this.quadSize <= this.scaleAlpha)
                            this.alpha *= 0.95F * this.multiplier;

                        if (this.alpha < 0.01F)
                            this.remove();
                    } else
                        this.remove();
                } else
                    this.wasInWater = false;

                if (this.onGround) {
                    if (FancyBlockParticles.CONFIG.snow.isLowTraction()) {
                        this.xd *= 0.932D;
                        this.zd *= 0.932D;
                    } else {
                        this.xd *= 0.665D;
                        this.zd *= 0.665D;
                    }

                    this.rotationStep = this.rotationStep.scale(0.85D);

                    if (!FancyBlockParticles.CONFIG.snow.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                        this.age += 2;
                }
            }
        }

        if (Minecraft.getInstance().cameraEntity.position().distanceTo(new Vec3(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) > Math.min(FancyBlockParticles.CONFIG.snow.getSimulationDistance(), Minecraft.getInstance().options.simulationDistance().get()) * 16)
            this.remove();

        this.visible = Minecraft.getInstance().cameraEntity.position().distanceTo(new Vec3(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) <= Math.min(FancyBlockParticles.CONFIG.snow.getRenderDistance(), Minecraft.getInstance().options.renderDistance().get()) * 16;
    }

    private boolean isInFluid(AABB box, TagKey<Fluid> fluid) {
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
        var box = this.getBoundingBox().inflate(1.0D);

        var minX = Mth.floor(box.minX);
        var maxX = Mth.ceil(box.maxX);
        var minZ = Mth.floor(box.minZ);
        var maxZ = Mth.ceil(box.maxZ);

        return !this.level.hasChunksAt(minX, maxX, minZ, maxZ);
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

        if (!FancyBlockParticles.CONFIG.snow.isLowTraction() && !FancyBlockParticles.CONFIG.snow.isBounceOffWalls()) {
            if (x != xo)
                this.xd *= 0.7D;

            if (z != zo)
                this.zd *= 0.7D;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    protected int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);
        var j = 0;

        var pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        if (!this.visible)
            return;

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

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize) / 10.0F;
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        if (FancyBlockParticles.CONFIG.snow.isRestOnFloor())
            posY += scale;

        var smoothRotation = Vec3.ZERO;

        if (FancyBlockParticles.CONFIG.snow.getRotationMultiplier() > 0.0F) {
            smoothRotation = smoothRotation.with(Direction.Axis.Y, this.rotation.y).with(Direction.Axis.Z, this.rotation.z);

            if (!FancyBlockParticles.CONFIG.snow.isRandomRotation())
                smoothRotation = smoothRotation.with(Direction.Axis.X, this.rotation.x);

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                var vec = this.rotation.lerp(this.lastRotation, partialTick);

                if (FancyBlockParticles.CONFIG.snow.isRandomRotation())
                    smoothRotation = smoothRotation.with(Direction.Axis.Y, vec.y).with(Direction.Axis.Z, vec.z);
                else
                    smoothRotation = smoothRotation.with(Direction.Axis.X, vec.x);
            }
        }

        FBPRenderHelper.renderCubeShaded(buffer, new Vec2[] { new Vec2(u1, v1), new Vec2(u1, v0), new Vec2(u0, v0), new Vec2(u0, v1) }, posX, posY, posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect())
                return null;

            return new FBPSnowParticle(level, x, y, z, FBPConstants.RANDOM.nextDouble(-0.5D, 0.5D), -FBPConstants.RANDOM.nextDouble(0.25D, 1.0D), FBPConstants.RANDOM.nextDouble(-0.5D, 0.5D), Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.SNOW.defaultBlockState()));
        }
    }
}
