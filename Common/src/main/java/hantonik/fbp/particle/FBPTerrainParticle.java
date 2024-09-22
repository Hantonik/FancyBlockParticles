package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.List;

public class FBPTerrainParticle extends TerrainParticle implements IKillableParticle {
    private final Vector3d rotation;
    private final Vector3d rotationStep;
    private final Vector3d lastRotation;

    private final float multiplier;

    private final boolean destroyed;

    private final double startY;

    private final float scaleAlpha;

    private float lastAlpha;
    private float lastSize;

    private double lastXSpeed;
    private double lastZSpeed;

    private boolean wasFrozen;
    private boolean wasInWater;

    private boolean killToggle;
    private boolean modeDebounce;

    public FBPTerrainParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, float scale, float rCol, float gCol, float bCol, BlockPos pos, BlockState state, @Nullable Direction side, @Nullable TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, state, pos);

        this.pos = pos;

        if (!state.is(Blocks.GRASS_BLOCK) || side == Direction.UP) {
            var i = Minecraft.getInstance().getBlockColors().getColor(state, this.level, pos, 0);

            this.rCol = (i >> 16 & 255) / 255.0F;
            this.gCol = (i >> 8 & 255) / 255.0F;
            this.bCol = (i & 255) / 255.0F;
        } else {
            this.rCol = rCol;
            this.gCol = gCol;
            this.bCol = bCol;
        }

        if (scale < -1.0D) {
            if (side == Direction.UP && FancyBlockParticles.CONFIG.terrain.isSmartBreaking()) {
                this.xd *= 1.5D;
                this.yd *= 0.1D;
                this.zd *= 1.5D;

                var speed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);

                var cameraXRot = Minecraft.getInstance().cameraEntity.getLookAngle().x;
                var cameraZRot = Minecraft.getInstance().cameraEntity.getLookAngle().z;

                this.xd = (cameraXRot < 0.0D ? cameraXRot - 0.01D : cameraXRot + 0.01D) * speed;
                this.zd = (cameraZRot < 0.0D ? cameraZRot - 0.01D : cameraZRot + 0.01D) * speed;
            }
        }

        var size = scale > -1.0D ? scale : this.quadSize;
        this.quadSize = FancyBlockParticles.CONFIG.terrain.getSizeMultiplier() * (FancyBlockParticles.CONFIG.terrain.isRandomSize() ? size : 1.0F) / 10.0F;
        this.gravity *= FancyBlockParticles.CONFIG.terrain.getGravityMultiplier();
        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.terrain.getMinLifetime(), FancyBlockParticles.CONFIG.terrain.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.terrain.getMinLifetime(), FancyBlockParticles.CONFIG.terrain.getMaxLifetime()) + 0.5F);

        this.startY = this.y;

        this.scaleAlpha = this.quadSize * 0.82F;

        this.rotation = new Vector3d();
        this.lastRotation = new Vector3d();
        this.rotationStep = new Vector3d(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1);

        this.rotation.set(this.rotationStep);

        this.modeDebounce = !FancyBlockParticles.CONFIG.terrain.isRandomRotation();

        if (this.modeDebounce) {
            this.rotation.zero();

            this.calculateYAngle();
        }

        this.destroyed = side == null;

        if (sprite == null) {
            if (!this.destroyed) {
                var quads = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state).getQuads(state, side, this.random);

                if (!quads.isEmpty())
                    this.sprite = quads.get(0).getSprite();
            }

            if (this.sprite.atlasLocation() == MissingTextureAtlasSprite.getLocation())
                this.sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state);
        } else
            this.sprite = sprite;

        this.multiplier = FancyBlockParticles.CONFIG.terrain.isRandomFadingSpeed() ? Mth.clamp(FBPConstants.RANDOM.nextFloat(0.5F, 0.9F), 0.6F, 0.8F) : 0.75F;

        this.scale(1.0F);
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        var size = this.quadSize / 10.0F;

        if (FancyBlockParticles.CONFIG.terrain.isRestOnFloor() && this.destroyed)
            this.y = this.startY - size;

        this.yo = this.y;

        this.setBoundingBox(new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

        return this;
    }

    @Override
    public Particle setPower(float power) {
        super.setPower(power);

        this.yd = (this.yd - 0.1D) * (this.multiplier / 2.0F) + 0.1F;

        return this;
    }

    @Override
    public void tick() {
        if (FancyBlockParticles.CONFIG.terrain.isBounceOffWalls()) {
            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!Minecraft.getInstance().isPaused() && this.age > 0) {
                    if (!this.wasFrozen && (Math.abs(this.xd) > 0.00001D || Math.abs(this.zd) > 0.00001D)) {
                        if (this.xo == this.x)
                            this.xd = -this.lastXSpeed * 0.625D;
                        if (this.zo == this.z)
                            this.zd = -this.lastZSpeed * 0.625D;

                        if (!FancyBlockParticles.CONFIG.terrain.isRandomRotation() && (this.xo == this.x || this.zo == this.z))
                            this.calculateYAngle();
                    } else
                        this.wasFrozen = false;
                }
            } else
                this.wasFrozen = true;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastRotation.set(this.rotation);

        this.lastSize = this.quadSize;
        this.lastAlpha = this.alpha;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || (this.destroyed && !FancyBlockParticles.CONFIG.terrain.isFancyBreakingParticles()) || (!this.destroyed && !FancyBlockParticles.CONFIG.terrain.isFancyCrackingParticles()))
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!FancyBlockParticles.CONFIG.terrain.isRandomRotation()) {
                    if (!this.modeDebounce) {
                        this.modeDebounce = true;

                        this.rotation.z = 0.0D;

                        this.calculateYAngle();
                    }

                    var x = Math.abs((this.rotationStep.x * this.getMultiplier()));

                    if (this.xd > 0.0D) {
                        if (this.zd > 0.0D)
                            this.rotation.x -= x;
                        else if (this.zd < 0.0D)
                            this.rotation.x += x;
                    } else if (this.xd < 0.0D) {
                        if (this.zd < 0.0D)
                            this.rotation.x += x;
                        else if (this.zd > 0.0D)
                            this.rotation.x -= x;
                    }
                } else {
                    if (this.modeDebounce) {
                        this.modeDebounce = false;

                        this.rotation.z = FBPConstants.RANDOM.nextDouble(30.0D, 400.0D);
                    }

                    this.rotation.add(this.rotationStep.mul(this.getMultiplier(), new Vector3d()));
                }

                if (!FancyBlockParticles.CONFIG.terrain.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                if (this.age >= this.lifetime) {
                    this.quadSize *= 0.9F * this.multiplier;

                    if (this.alpha >= 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.7F * this.multiplier;

                    if (this.alpha < 0.01D)
                        this.remove();
                }

                this.move(this.xd, this.yd, this.zd);

                if (this.onGround && FancyBlockParticles.CONFIG.terrain.isRestOnFloor()) {
                    this.rotation.x = Math.round(this.rotation.x / 10.0D) * 10.0D;
                    this.rotation.z = Math.round(this.rotation.z / 10.0D) * 10.0D;
                }

                if (Math.abs(this.xd) > 0.00001D)
                    this.lastXSpeed = this.xd;
                if (Math.abs(this.zd) > 0.00001D)
                    this.lastZSpeed = this.zd;

                this.xd *= 0.98D;
                this.zd *= 0.98D;

                this.yd *= 0.98D;

                if (!this.level.noCollision(this.getBoundingBox().deflate(1.0E-7)))
                    this.moveTowardsClosestSpace(this.x, (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.z);

                if (FancyBlockParticles.CONFIG.terrain.isEntityCollision()) {
                    var entities = this.level.getEntities(null, this.getBoundingBox());

                    for (var entity : entities) {
                        if (!entity.noPhysics) {
                            var x = this.x - entity.position().x;
                            var y = this.y - entity.position().y;
                            var z = this.z - entity.position().z;

                            var distance = Mth.absMax(Mth.absMax(x, y), z);

                            if (distance >= 0.01D) {
                                distance = Math.sqrt(distance);

                                x /= distance;
                                y /= distance;
                                z /= distance;

                                var d = Math.min(1.0D / distance, 1.0D);

                                this.xd += x * d / 20.0D;
                                this.yd += y * d / 20.0D - 0.04D * this.gravity;
                                this.zd += z * d / 20.0D;

                                if (!FancyBlockParticles.CONFIG.terrain.isRandomRotation())
                                    this.calculateYAngle();

                                if (!FancyBlockParticles.CONFIG.global.isFreezeEffect())
                                    this.onGround = false;
                            }
                        }
                    }
                }

                if (FancyBlockParticles.CONFIG.terrain.isWaterPhysics() && this.isInWater(this.getBoundingBox())) {
                    this.xd *= 0.95D;
                    this.zd *= 0.95D;

                    if (this.yd > -0.005D && this.yd < 0.005D)
                        this.yd = 0.005D;

                    if (this.yd < 0.0D)
                        this.yd *= 0.79D * FBPConstants.RANDOM.nextDouble(0.8D, 1.2D);
                    else {
                        this.yd *= 1.1D * FBPConstants.RANDOM.nextDouble(0.8D, 0.9D);

                        if (!this.isInWater(this.getBoundingBox().move(this.xd, 0.3D, this.zd)))
                            this.yd *= 0.9D;
                    }

                    if (!FancyBlockParticles.CONFIG.terrain.isRandomRotation())
                        this.calculateYAngle();

                    this.onGround = false;
                    this.wasInWater = true;

                    return;
                } else {
                    if (!this.onGround)
                        this.yd -= (this.wasInWater ? 0.02D : 0.04D) * this.gravity;

                    this.wasInWater = false;
                }

                if (this.onGround) {
                    if (FancyBlockParticles.CONFIG.terrain.isLowTraction()) {
                        this.xd *= 0.932D;
                        this.zd *= 0.932D;
                    } else {
                        this.xd *= 0.665D;
                        this.zd *= 0.665D;
                    }
                }
            }
        }
    }

    private void moveTowardsClosestSpace(double x, double y, double z) {
        var pos = BlockPos.containing(x, y, z);
        var vec = new Vec3(x - (double) pos.getX(), y - (double) pos.getY(), z - (double) pos.getZ());

        var relativePos = new BlockPos.MutableBlockPos();

        var minDistance = Double.MAX_VALUE;
        var distanceDirection = Direction.UP;

        for (var direction : Direction.values()) {
            relativePos.setWithOffset(pos, direction);

            if (!this.level.getBlockState(relativePos).isCollisionShapeFullBlock(this.level, relativePos)) {
                var axisDistance = vec.get(direction.getAxis());
                var distance = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - axisDistance : axisDistance;

                if (distance < minDistance) {
                    minDistance = distance;
                    distanceDirection = direction;
                }
            }
        }

        var step = distanceDirection.getAxisDirection().getStep();
        var rand = this.random.nextFloat() * 0.1F + 0.1F;

        var movement = new Vec3(this.xd, this.yd, this.zd).scale(0.75D);

        this.xd = movement.x;
        this.yd = movement.y;
        this.zd = movement.z;

        switch (distanceDirection.getAxis()) {
            case X -> this.xd = step * rand;
            case Y -> this.yd = step * rand;
            case Z -> this.zd = step * rand;
        }

        this.onGround = false;
    }

    private boolean isInWater(AABB box) {
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
                        var pos = BlockPos.containing(x, y, z);
                        var fluid = this.level.getFluidState(pos);

                        if (fluid.is(FluidTags.WATER))
                            if (fluid.getHeight(this.level, pos) + y >= box.minY)
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

        if (!FancyBlockParticles.CONFIG.terrain.isLowTraction() && !FancyBlockParticles.CONFIG.terrain.isBounceOffWalls()) {
            if (x != xo)
                this.xd *= 0.7D;

            if (z != zo)
                this.zd *= 0.7D;
        }
    }

    @Override
    protected void setLocationFromBoundingbox() {
        super.setLocationFromBoundingbox();

        var box = this.getBoundingBox();
        this.y = (box.minY + box.maxY) / 2.0D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    public int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);
        var j = 0;

        var box = this.getBoundingBox();
        var pos = BlockPos.containing(this.x, this.y + ((box.maxY - box.minY) * 0.66D) + 0.01D - (FancyBlockParticles.CONFIG.terrain.isRestOnFloor() ? this.quadSize / 10.0D : 0.0D), this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        var u0 = 0.0F;
        var v0 = 0.0F;

        if (!FancyBlockParticles.CONFIG.global.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F);
            v0 = this.sprite.getV(this.vo / 4.0F);
        }

        var u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F);
        var v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F);

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize);
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        if (FancyBlockParticles.CONFIG.terrain.isRestOnFloor())
            posY += scale;

        var smoothRotation = new Vector3d();

        if (FancyBlockParticles.CONFIG.terrain.getRotationMultiplier() > 0.0F) {
            smoothRotation.y = this.rotation.y;
            smoothRotation.z = this.rotation.z;

            if (!FancyBlockParticles.CONFIG.terrain.isRandomRotation())
                smoothRotation.x = this.rotation.x;

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (FancyBlockParticles.CONFIG.terrain.isRandomRotation()) {
                    smoothRotation.y = Mth.lerp(partialTick, this.lastRotation.y, this.rotation.y);
                    smoothRotation.z = Mth.lerp(partialTick, this.lastRotation.z, this.rotation.z);
                } else
                    smoothRotation.x = Mth.lerp(partialTick, this.lastRotation.x, this.rotation.x);
            }
        }

        FBPRenderHelper.renderCubeShaded(buffer, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, (float) posX, (float) posY, (float) posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    private void calculateYAngle() {
        var sin = Math.toDegrees(Math.asin(this.xd / Math.sqrt(this.xd * this.xd + this.zd * this.zd)));

        this.rotation.y = this.zd > 0.0D ? -sin : sin;
    }

    private double getMultiplier() {
        return Math.sqrt(this.xd * this.xd + this.zd * this.zd) * (FancyBlockParticles.CONFIG.terrain.isRandomRotation() ? 200.0D : 500.0D) * FancyBlockParticles.CONFIG.terrain.getRotationMultiplier();
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<BlockParticleOption> {
        private final BlockPos pos;
        private final float scale;
        @Nullable
        private final Direction side;
        @Nullable
        private final TextureAtlasSprite sprite;
        private final float rCol;
        private final float gCol;
        private final float bCol;

        @Nullable
        @Override
        public Particle createParticle(BlockParticleOption type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new FBPTerrainParticle(level, x, y, z, xd, yd, zd, this.scale, this.rCol, this.gCol, this.bCol, this.pos, type.getState(), this.side, this.sprite);
        }
    }
}
