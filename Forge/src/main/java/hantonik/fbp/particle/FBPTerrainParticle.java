package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.init.FBPKeyMappings;
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class FBPTerrainParticle extends TerrainParticle {
    private final BlockState state;
    private final Direction side;

    private final float startY;

    private final float scaleAlpha;

    private double lastXSpeed;
    private double lastZSpeed;

    private float lastAlpha;
    private float lastScale;

    private float multiplier;

    private final boolean destroyed;
    private boolean wasFrozen;

    private boolean killToggle;
    private boolean modeDebounce;

    private final Vector3f rotation;
    private final Vector3f rotationStep;
    private final Vector3f lastRotation;

    public FBPTerrainParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float scale, float red, float green, float blue, BlockPos pos, BlockState state, @Nullable Direction side, @Nullable TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, state);

        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;

        this.state = state;

        this.rotation = new Vector3f();
        this.lastRotation = new Vector3f();

        this.rotationStep = new Vector3f(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1);

        this.side = side;

        this.pos = pos;

        this.rotation.load(this.rotationStep);

        if (scale > -1.0D)
            this.quadSize = scale;

        if (scale < -1.0D) {
            if (side != null) {
                if (side == Direction.UP && FancyBlockParticles.CONFIG.isSmartBreaking()) {
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
        }

        this.modeDebounce = !FancyBlockParticles.CONFIG.isRandomRotation();

        if (this.modeDebounce) {
            this.rotation.set(0.0F, 0.0F, 0.0F);
            this.calculateYAngle();
        }

        this.gravity *= (float) FancyBlockParticles.CONFIG.getGravityMultiplier();
        this.quadSize = (float) (FancyBlockParticles.CONFIG.getScaleMultiplier() * (FancyBlockParticles.CONFIG.isRandomScale() ? this.quadSize : 1.0F));
        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.getMinLifetime(), FancyBlockParticles.CONFIG.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.getMinLifetime(), FancyBlockParticles.CONFIG.getMaxLifetime()) + 0.5F);

        this.scaleAlpha = this.quadSize * 0.82F;

        this.startY = (float) this.y;

        this.destroyed = (side == null);

        if (sprite == null) {
            if (!this.destroyed) {
                var quads = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(this.state).getQuads(this.state, side, this.random);

                if (!quads.isEmpty())
                    this.sprite = quads.get(0).getSprite();
            }

            if (this.sprite.atlas().location() == MissingTextureAtlasSprite.getLocation())
                this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(this.state));
        } else
            this.sprite = sprite;

        this.multiplier = 0.75F;

        if (FancyBlockParticles.CONFIG.isRandomFadingSpeed())
            this.multiplier = Mth.clamp(FBPConstants.RANDOM.nextFloat(0.5F, 0.9F), 0.55F, 0.8F);

        this.scale(1);
        this.multiplyColor(this.pos);
    }

    @Override
    public Particle scale(float scale) {
        var particle = super.scale(scale);

        var s = this.quadSize / 10.0F;

        if (FancyBlockParticles.CONFIG.isRestOnFloor() && this.destroyed)
            this.y = this.startY - s;

        this.yo = this.y;

        this.setBoundingBox(new AABB(this.x - s, this.y, this.z - s, this.x + s, this.y + 2 * s, this.z + s));

        return particle;
    }

    @Override
    public Particle setPower(float multiplier) {
        this.xd *= multiplier;
        this.yd = (this.yd - 0.1D) * (multiplier / 2.0D) + 0.1D;
        this.zd *= multiplier;

        return this;
    }

    public void multiplyColor(BlockPos pos) {
        if (this.state.is(Blocks.GRASS_BLOCK) && this.side != Direction.UP)
            return;

        var i = Minecraft.getInstance().getBlockColors().getColor(this.state, this.level, pos, 0);

        this.rCol = (i >> 16 & 255) / 255.0F;
        this.gCol = (i >> 8 & 255) / 255.0F;
        this.bCol = (i & 255) / 255.0F;
    }

    @Override
    public void tick() {
        var allowedToMove = Mth.abs((float) this.xd) > 0.0001D || Mth.abs((float) this.zd) > 0.0001D;

        if (!FancyBlockParticles.CONFIG.isFrozen() && FancyBlockParticles.CONFIG.isBounceOffWalls() && !Minecraft.getInstance().isPaused() && this.age > 0) {
            if (!this.wasFrozen && allowedToMove) {
                if (this.xo == this.x)
                    this.xd = -this.lastXSpeed * 0.625F;
                if (this.zo == this.z)
                    this.zd = -this.lastZSpeed * 0.625F;

                if (!FancyBlockParticles.CONFIG.isRandomRotation() && (this.xo == this.x || this.zo == this.z))
                    this.calculateYAngle();
            } else
                this.wasFrozen = false;
        }

        if (FancyBlockParticles.CONFIG.isFrozen() && FancyBlockParticles.CONFIG.isBounceOffWalls() && !this.wasFrozen)
            this.wasFrozen = true;

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastRotation.load(this.rotation);

        this.lastAlpha = this.alpha;
        this.lastScale = this.quadSize;

        if (!Minecraft.getInstance().isPaused() && (!FancyBlockParticles.CONFIG.isFrozen() || this.killToggle)) {
            if (!this.killToggle) {
                if (!FancyBlockParticles.CONFIG.isRandomRotation()) {
                    if (!this.modeDebounce) {
                        this.modeDebounce = true;

                        this.rotation.set(this.rotation.x(), this.rotation.y(), 0.0F);

                        this.calculateYAngle();
                    }

                    if (allowedToMove) {
                        var x = Mth.abs((float) (this.rotationStep.x() * this.getMultiplier()));

                        if (this.xd > 0.0D) {
                            if (this.zd > 0.0D)
                                this.rotation.add(-x, 0.0F, 0.0F);
                            else if (this.zd < 0.0D)
                                this.rotation.add(x, 0.0F, 0.0F);
                        } else if (this.xd < 0.0D) {
                            if (this.zd < 0.0D)
                                this.rotation.add(x, 0.0F, 0.0F);
                            else if (this.zd > 0.0D)
                                this.rotation.add(-x, 0.0F, 0.0F);
                        }
                    }
                } else {
                    if (this.modeDebounce) {
                        this.modeDebounce = false;

                        this.rotation.set(this.rotation.x(), this.rotation.y(), FBPConstants.RANDOM.nextFloat(30.0F, 400.0F));
                    }

                    if (allowedToMove) {
                        var step = this.rotationStep.copy();
                        step.mul((float) this.getMultiplier());

                        this.rotation.add(step);
                    }
                }
            }

            if (!FancyBlockParticles.CONFIG.isInfiniteDuration())
                this.age++;

            if (this.age >= this.lifetime || this.killToggle) {
                this.quadSize *= 0.9F * this.multiplier;

                if (this.alpha > 0.01D && this.quadSize <= this.scaleAlpha)
                    this.alpha *= 0.7F * this.multiplier;

                if (this.alpha <= 0.01D)
                    this.remove();
            }

            if (!this.killToggle) {
                if (!this.onGround)
                    this.yd -= 0.04D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                if (this.onGround && FancyBlockParticles.CONFIG.isRestOnFloor())
                    this.rotation.set(Math.round(this.rotation.x() / 10.0F) * 10.0F, this.rotation.y(), Math.round(this.rotation.z() / 10.0F) * 10.0F);

                if (Mth.abs((float) this.xd) > 0.00001D)
                    this.lastXSpeed = this.xd;
                if (Mth.abs((float) this.zd) > 0.00001D)
                    this.lastZSpeed = this.zd;

                if (allowedToMove) {
                    this.xd *= 0.98D;
                    this.zd *= 0.98D;
                }

                this.yd *= 0.98D;

                if (FancyBlockParticles.CONFIG.isEntityCollision()) {
                    var entities = this.level.getEntities(null, this.getBoundingBox());

                    for (var entity : entities) {
                        if (!entity.noPhysics) {
                            var x = this.x - entity.position().x;
                            var z = this.z - entity.position().z;

                            var distance = Mth.absMax(x, z);

                            if (distance >= 0.01D) {
                                distance = Math.sqrt(distance);

                                x /= distance;
                                z /= distance;

                                var d = Math.min(1.0D / distance, 1.0D);

                                this.xd += x * d / 20.0D;
                                this.zd += z * d / 20.0D;

                                if (!FancyBlockParticles.CONFIG.isRandomRotation())
                                    this.calculateYAngle();

                                if (!FancyBlockParticles.CONFIG.isFrozen())
                                    this.onGround = false;
                            }
                        }
                    }
                }

                // Water movement

                if (this.onGround) {
                    if (FancyBlockParticles.CONFIG.isLowTraction()) {
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

    private boolean isInWater() {
        var scale = this.quadSize / 20.0F;

        var minX = Mth.floor(this.x - scale);
        var maxX = Mth.ceil(this.x + scale);

        var minY = Mth.floor(this.y - scale);
        var maxY = Mth.ceil(this.y + scale);

        var minZ = Mth.floor(this.z - scale);
        var maxZ = Mth.ceil(this.z + scale);

        for (var x = minX; x < maxX; x++) {
            for (var y = minY; y < maxY; y++) {
                for (var z = minZ; z < maxZ; z++) {
                    var pos = new BlockPos(x, y, z);
                    var state = this.level.getBlockState(pos).getFluidState();

                    if (!state.isEmpty())
                        if (this.y < y - state.getHeight(this.level, pos))
                            return true;
                }
            }
        }

        return false;
    }

    @Override
    public void move(double x, double y, double z) {
        var xo = x;
        var yo = y;
        var zo = z;

        var collisions = this.level.getCollisions(null, this.getBoundingBox().expandTowards(x, y, z));

        for (var shape : collisions)
            x = shape.collide(Direction.Axis.X, this.getBoundingBox(), x);

        this.setBoundingBox(this.getBoundingBox().move(x, 0.0D, 0.0D));

        for (var shape : collisions)
            y = shape.collide(Direction.Axis.Y, this.getBoundingBox(), y);

        this.setBoundingBox(this.getBoundingBox().move(0.0D, y, 0.0D));

        for (var shape : collisions)
            z = shape.collide(Direction.Axis.Z, this.getBoundingBox(), z);

        this.setBoundingBox(this.getBoundingBox().move(0.0D, 0.0D, z));

        this.setLocationFromBoundingbox();

        this.onGround = y != yo && yo < 0.0D;

        if (!FancyBlockParticles.CONFIG.isLowTraction() && !FancyBlockParticles.CONFIG.isBounceOffWalls()) {
            if (x != xo)
                this.xd *= 0.7D;

            if (z != zo)
                this.zd *= 0.7D;
        }
    }

    @Override
    protected void setLocationFromBoundingbox() {
        var box = this.getBoundingBox();

        this.x = (box.minX + box.maxX) / 2.0D;
        this.y = (box.minY + box.maxY) / 2.0D;
        this.z = (box.minZ + box.maxZ) / 2.0D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTicks) {
        if (!FancyBlockParticles.CONFIG.isEnabled())
            this.lifetime = 0;

        if (FBPKeyMappings.KILL_PARTICLES.isDown())
            this.killToggle = true;

        var u0 = 0.0F;
        var v0 = 0.0F;

        if (!FancyBlockParticles.CONFIG.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F * 16.0F);
            v0 = this.sprite.getV(this.vo / 4.0F * 16.0F);
        }

        var u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F * 16.0F);
        var v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F * 16.0F);

        var posX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - info.getPosition().x);
        var posY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - info.getPosition().y);
        var posZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - info.getPosition().z);

        var scale = Mth.lerp(partialTicks, this.lastScale, this.quadSize) / 10.0F;

        var light = this.getLightColor(partialTicks);

        var alpha = Mth.lerp(partialTicks, this.lastAlpha, this.alpha);

        if (FancyBlockParticles.CONFIG.isRestOnFloor())
            posY += scale;

        var smoothRotation = new Vector3f();

        if (FancyBlockParticles.CONFIG.getRotationMultiplier() > 0.0D) {
            smoothRotation.set(smoothRotation.x(), this.rotation.y(), this.rotation.z());

            if (!FancyBlockParticles.CONFIG.isRandomRotation())
                smoothRotation.set(this.rotation.x(), smoothRotation.y(), smoothRotation.z());

            if (!FancyBlockParticles.CONFIG.isFrozen()) {
                if (FancyBlockParticles.CONFIG.isRandomRotation())
                    smoothRotation.set(smoothRotation.x(), Mth.lerp(partialTicks, this.lastRotation.y(), this.rotation.y()), Mth.lerp(partialTicks, this.lastRotation.z(), this.rotation.z()));
                else
                    smoothRotation.set(Mth.lerp(partialTicks, this.lastRotation.x(), this.rotation.x()), smoothRotation.y(), smoothRotation.z());
            }
        }

        FBPRenderHelper.renderCubeShaded(buffer, new Vec2[]{ new Vec2(u1, v1), new Vec2(u1, v0), new Vec2(u0, v0), new Vec2(u0, v1) }, posX, posY, posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.isCartoonMode());
    }

    @Override
    public int getLightColor(float partialTick) {
        var box = this.getBoundingBox();

        return this.level.hasChunkAt(new BlockPos(this.x, this.y, this.z)) ? LevelRenderer.getLightColor(this.level, this.state, new BlockPos(this.x, this.y + ((box.maxY - box.minY) * 0.66D) + 0.01D - (FancyBlockParticles.CONFIG.isRestOnFloor() ? this.quadSize / 10.0D : 0.0D), this.z)) : 0;
    }

    private double getMultiplier() {
        return Math.sqrt(this.xd * this.xd + this.zd * this.zd) * (FancyBlockParticles.CONFIG.isRandomRotation() ? 200.0D : 500.0D) * FancyBlockParticles.CONFIG.getRotationMultiplier();
    }

    private void calculateYAngle() {
        var sin = (float) Math.toDegrees(Math.asin(this.xd / Math.sqrt(this.xd * this.xd + this.zd * this.zd)));
        this.rotation.set(this.rotation.x(), this.zd > 0.0F ? -sin : sin, this.rotation.z());
    }

    @OnlyIn(Dist.CLIENT)
    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<BlockParticleOption> {
        private final float scale;
        @Nullable
        private final Direction side;
        private final TextureAtlasSprite sprite;
        private final float red;
        private final float green;
        private final float blue;

        @Nullable
        @Override
        public Particle createParticle(BlockParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FBPTerrainParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.scale, this.red, this.green, this.blue, type.getPos(), type.getState(), this.side, this.sprite);
        }
    }
}
