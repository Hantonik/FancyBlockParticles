package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.BlockParticleData;
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
import java.util.List;
import java.util.stream.Stream;

public class FBPDiggingParticle extends DiggingParticle implements IKillableParticle {
    private final BlockState state;

    private Vector3d rotation;
    private Vector3d rotationStep;
    private Vector3d lastRotation;

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

    public FBPDiggingParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, float scale, float rCol, float gCol, float bCol, BlockPos pos, BlockState state, @Nullable Direction side, @Nullable TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, state);

        this.pos = pos;
        this.state = state;

        if (!this.state.is(Blocks.GRASS_BLOCK) || side == Direction.UP) {
            int i = Minecraft.getInstance().getBlockColors().getColor(this.state, this.level, pos, 0);

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

                double speed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);

                double cameraXRot = Minecraft.getInstance().cameraEntity.getLookAngle().x;
                double cameraZRot = Minecraft.getInstance().cameraEntity.getLookAngle().z;

                this.xd = (cameraXRot < 0.0D ? cameraXRot - 0.01D : cameraXRot + 0.01D) * speed;
                this.zd = (cameraZRot < 0.0D ? cameraZRot - 0.01D : cameraZRot + 0.01D) * speed;
            }
        }

        float size = scale > -1.0D ? scale : this.quadSize;
        this.quadSize = FancyBlockParticles.CONFIG.terrain.getSizeMultiplier() * (FancyBlockParticles.CONFIG.terrain.isRandomSize() ? size : 1.0F) / 10.0F;
        this.gravity *= FancyBlockParticles.CONFIG.terrain.getGravityMultiplier();
        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.terrain.getMinLifetime(), FancyBlockParticles.CONFIG.terrain.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.terrain.getMinLifetime(), FancyBlockParticles.CONFIG.terrain.getMaxLifetime()) + 0.5D);

        this.startY = this.y;

        this.scaleAlpha = this.quadSize * 0.82F;

        this.rotationStep = new Vector3d(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1);

        this.lastRotation = Vector3d.ZERO;
        this.rotation = this.rotationStep;

        this.modeDebounce = !FancyBlockParticles.CONFIG.terrain.isRandomRotation();

        if (this.modeDebounce) {
            this.rotation = Vector3d.ZERO;

            this.calculateYAngle();
        }

        this.destroyed = side == null;

        if (sprite == null) {
            if (!this.destroyed) {
                List<BakedQuad> quads = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(this.state).getQuads(this.state, side, this.random);

                if (!quads.isEmpty())
                    this.sprite = quads.get(0).getSprite();
            }

            if (this.sprite.atlas().location() == MissingTextureSprite.getLocation())
                this.sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(this.state);
        } else
            this.sprite = sprite;

        this.multiplier = FancyBlockParticles.CONFIG.terrain.isRandomFadingSpeed() ? MathHelper.clamp((float) FBPConstants.RANDOM.nextDouble(0.5D, 0.9D), 0.55F, 0.8F) : 0.75F;

        this.scale(1.0F);
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        float size = this.quadSize / 10.0F;

        if (FancyBlockParticles.CONFIG.terrain.isRestOnFloor() && this.destroyed)
            this.y = this.startY - size;

        this.yo = this.y;

        this.setBoundingBox(new AxisAlignedBB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

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

        this.lastRotation = this.rotation;

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

                        this.rotation = new Vector3d(this.rotation.x, this.rotation.y, 0.0D);

                        this.calculateYAngle();
                    }

                    double x = Math.abs((this.rotationStep.x * this.getMultiplier()));

                    if (this.xd > 0.0D) {
                        if (this.zd > 0.0D)
                            this.rotation = this.rotation.subtract(x, 0.0D, 0.0D);
                        else if (this.zd < 0.0D)
                            this.rotation = this.rotation.add(x, 0.0D, 0.0D);
                    } else if (this.xd < 0.0D) {
                        if (this.zd < 0.0D)
                            this.rotation = this.rotation.add(x, 0.0D, 0.0D);
                        else if (this.zd > 0.0D)
                            this.rotation = this.rotation.subtract(x, 0.0D, 0.0D);
                    }
                } else {
                    if (this.modeDebounce) {
                        this.modeDebounce = false;

                        this.rotation = new Vector3d(this.rotation.x, this.rotation.y, FBPConstants.RANDOM.nextDouble(30.0D, 400.0D));
                    }

                    this.rotation = this.rotation.add(this.rotationStep.scale(this.getMultiplier()));
                }

                if (!FancyBlockParticles.CONFIG.terrain.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                if (this.age >= this.lifetime) {
                    this.quadSize *= 0.9F * this.multiplier;

                    if (this.alpha > 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.7F * this.multiplier;

                    if (this.alpha <= 0.01D)
                        this.remove();
                }

                this.move(this.xd, this.yd, this.zd);

                if (this.onGround && FancyBlockParticles.CONFIG.terrain.isRestOnFloor())
                    this.rotation = new Vector3d(Math.round(this.rotation.x / 10.0D) * 10.0D, this.rotation.y, Math.round(this.rotation.z / 10.0D) * 10.0D);

                if (Math.abs(this.xd) > 0.00001D)
                    this.lastXSpeed = this.xd;
                if (Math.abs(this.zd) > 0.00001D)
                    this.lastZSpeed = this.zd;

                this.xd *= 0.98D;
                this.zd *= 0.98D;

                this.yd *= 0.98D;

                if (FancyBlockParticles.CONFIG.terrain.isEntityCollision()) {
                    List<Entity> entities = this.level.getEntities(null, this.getBoundingBox());

                    for (Entity entity : entities) {
                        if (!entity.noPhysics) {
                            double x = this.x - entity.position().x;
                            double y = this.y - entity.position().y;
                            double z = this.z - entity.position().z;

                            double distance = MathHelper.absMax(MathHelper.absMax(x, y), z);

                            if (distance >= 0.01D) {
                                distance = Math.sqrt(distance);

                                x /= distance;
                                y /= distance;
                                z /= distance;

                                double d = Math.min(1.0D / distance, 1.0D);

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
//                TODO: Water movement
//                this.handleWaterMovement();

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

    private boolean isInWater(AxisAlignedBB box) {
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
                        FluidState fluid = this.level.getFluidState(pos);

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

        AxisAlignedBB box = this.getBoundingBox();
        this.y = (box.minY + box.maxY) / 2.0D;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    public int getLightColor(float partialTick) {
        AxisAlignedBB box = this.getBoundingBox();

        return this.level.hasChunkAt(new BlockPos(this.x, this.y, this.z)) ? WorldRenderer.getLightColor(this.level, this.state, new BlockPos(this.x, this.y + ((box.maxY - box.minY) * 0.66D) + 0.01D - (FancyBlockParticles.CONFIG.terrain.isRestOnFloor() ? this.quadSize / 10.0D : 0.0D), this.z)) : 0;
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

        if (FancyBlockParticles.CONFIG.terrain.isRestOnFloor())
            posY += scale;

        Vector3d smoothRotation = Vector3d.ZERO;

        if (FancyBlockParticles.CONFIG.terrain.getRotationMultiplier() > 0.0F) {
            smoothRotation = new Vector3d(smoothRotation.x, this.rotation.y, this.rotation.z);

            if (!FancyBlockParticles.CONFIG.terrain.isRandomRotation())
                smoothRotation = new Vector3d(this.rotation.x, smoothRotation.y, smoothRotation.z);

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (FancyBlockParticles.CONFIG.terrain.isRandomRotation())
                    smoothRotation = new Vector3d(smoothRotation.x, MathHelper.lerp(partialTick, this.lastRotation.y, this.rotation.y), MathHelper.lerp(partialTick, this.lastRotation.z, this.rotation.z));
                else
                    smoothRotation = new Vector3d(MathHelper.lerp(partialTick, this.lastRotation.x, this.rotation.x), smoothRotation.y, smoothRotation.z);
            }
        }

        FBPRenderHelper.renderCubeShaded(builder, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, (float) posX, (float) posY, (float) posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    private void calculateYAngle() {
        double sin = Math.toDegrees(Math.asin(this.xd / Math.sqrt(this.xd * this.xd + this.zd * this.zd)));

        this.rotation = new Vector3d(this.rotation.x, this.zd > 0.0D ? -sin : sin, this.rotation.z);
    }

    private double getMultiplier() {
        return Math.sqrt(this.xd * this.xd + this.zd * this.zd) * (FancyBlockParticles.CONFIG.terrain.isRandomRotation() ? 200.0D : 500.0D) * FancyBlockParticles.CONFIG.terrain.getRotationMultiplier();
    }

    @RequiredArgsConstructor
    public static class Provider implements IParticleFactory<BlockParticleData> {
        private final BlockPos pos;
        private final float scale;
        @Nullable
        private final Direction side;
        private final TextureAtlasSprite sprite;
        private final float rCol;
        private final float gCol;
        private final float bCol;

        @Nullable
        @Override
        public Particle createParticle(BlockParticleData type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            return new FBPDiggingParticle(level, x, y, z, xd, yd, zd, this.scale, this.rCol, this.gCol, this.bCol, this.pos, type.getState(), this.side, this.sprite);
        }
    }
}
