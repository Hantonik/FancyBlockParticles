package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;

@OnlyIn(Dist.CLIENT)
public class FBPTerrainParticle extends TerrainParticle {
    private final BlockState state;
    private final Direction side;

    private final double startY;

    private final double scaleAlpha;

    private double lastAlpha;
    private double lastScale;

    private double lastXSpeed;
    private double lastZSpeed;

    private double multiplier;

    private final boolean destroyed;
    private boolean wasFrozen;

    private boolean killToggle;
    private boolean modeDebounce;

    private final Vector3d rotation;
    private final Vector3d rotationStep;
    private final Vector3d lastRotation;

    public FBPTerrainParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float scale, float red, float green, float blue, BlockPos pos, BlockState state, @Nullable Direction side, @Nullable TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, state);

        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;

        this.state = state;

        this.rotation = new Vector3d();
        this.lastRotation = new Vector3d();

        this.rotationStep = new Vector3d(FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1, FBPConstants.RANDOM.nextDouble() > 0.5D ? 1 : -1);

        this.side = side;

        this.pos = pos;

        this.rotation.set(this.rotationStep);

        if (scale > -1.0D)
            this.quadSize = scale;

        if (scale < -1.0D) {
            if (side != null) {
                if (side == Direction.UP && FancyBlockParticles.PHYSICS_CONFIG.isSmartBreaking()) {
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

        this.modeDebounce = !FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation();

        if (this.modeDebounce) {
            this.rotation.zero();
            this.calculateYAngle();
        }

        this.gravity *= (float) FancyBlockParticles.RENDER_CONFIG.getGravityMultiplier();
        this.quadSize = (float) (FancyBlockParticles.RENDER_CONFIG.getScaleMultiplier() * (FancyBlockParticles.PHYSICS_CONFIG.isRandomScale() ? this.quadSize : 1.0F));
        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.RENDER_CONFIG.getMinLifetime(), FancyBlockParticles.RENDER_CONFIG.getMaxLifetime()), Math.max(FancyBlockParticles.RENDER_CONFIG.getMinLifetime(), FancyBlockParticles.RENDER_CONFIG.getMaxLifetime()) + 0.5F);

        this.scaleAlpha = this.quadSize * 0.82D;

        this.startY = this.y;

        this.destroyed = (side == null);

        if (sprite == null) {
            if (!this.destroyed) {
                var quads = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(this.state).getQuads(this.state, side, this.random);

                if (!quads.isEmpty())
                    this.sprite = quads.get(0).getSprite();
            }

            if (this.sprite.atlasLocation() == MissingTextureAtlasSprite.getLocation())
                this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(this.state, this.level, this.pos));
        } else
            this.sprite = sprite;

        this.multiplier = 0.75D;

        if (FancyBlockParticles.PHYSICS_CONFIG.isRandomFadingSpeed())
            this.multiplier = Mth.clamp(FBPConstants.RANDOM.nextDouble(0.5D, 0.9D), 0.55D, 0.8D);

        this.scale(1);
        this.multiplyColor(this.pos);
    }

    @Override
    public Particle scale(float scale) {
        var particle = super.scale(scale);

        var s = this.quadSize / 10.0F;

        if (FancyBlockParticles.PHYSICS_CONFIG.isRestOnFloor() && this.destroyed)
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

        if (!FancyBlockParticles.RENDER_CONFIG.isFrozen() && FancyBlockParticles.PHYSICS_CONFIG.isBounceOffWalls() && !Minecraft.getInstance().isPaused() && this.age > 0) {
            if (!this.wasFrozen && allowedToMove) {
                if (this.xo == this.x)
                    this.xd = -this.lastXSpeed * 0.625F;
                if (this.zo == this.z)
                    this.zd = -this.lastZSpeed * 0.625F;

                if (!FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation() && (this.xo == this.x || this.zo == this.z))
                    this.calculateYAngle();
            } else
                this.wasFrozen = false;
        }

        if (FancyBlockParticles.RENDER_CONFIG.isFrozen() && FancyBlockParticles.PHYSICS_CONFIG.isBounceOffWalls() && !this.wasFrozen)
            this.wasFrozen = true;

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastRotation.set(this.rotation);

        this.lastAlpha = this.alpha;
        this.lastScale = this.quadSize;

        if (!Minecraft.getInstance().isPaused() && (!FancyBlockParticles.RENDER_CONFIG.isFrozen() || this.killToggle)) {
            if (!this.killToggle) {
                if (!FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation()) {
                    if (!this.modeDebounce) {
                        this.modeDebounce = true;

                        this.rotation.z = 0.0D;

                        this.calculateYAngle();
                    }

                    if (allowedToMove) {
                        var x = Mth.abs((float) (this.rotationStep.x * this.getMultiplier()));

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
                    }
                } else {
                    if (this.modeDebounce) {
                        this.modeDebounce = false;

                        this.rotation.z = FBPConstants.RANDOM.nextDouble(30.0D, 400.0D);
                    }

                    if (allowedToMove)
                        this.rotation.add(this.rotationStep.mul(this.getMultiplier(), new Vector3d()));
                }
            }

            if (!FancyBlockParticles.RENDER_CONFIG.isInfiniteDuration())
                this.age++;

            if (this.age >= this.lifetime || this.killToggle) {
                this.quadSize *= (float) (0.9F * this.multiplier);

                if (this.alpha > 0.01D && this.quadSize <= this.scaleAlpha)
                    this.alpha *= (float) (0.7F * this.multiplier);

                if (this.alpha <= 0.01D)
                    this.remove();
            }

            if (!this.killToggle) {
                if (!this.onGround)
                    this.yd -= 0.04D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                if (this.onGround && FancyBlockParticles.PHYSICS_CONFIG.isRestOnFloor()) {
                    this.rotation.x = Math.round(this.rotation.x / 10.0D) * 10.0D;
                    this.rotation.z = Math.round(this.rotation.z / 10.0D) * 10.0D;
                }

                if (Mth.abs((float) this.xd) > 0.00001D)
                    this.lastXSpeed = this.xd;
                if (Mth.abs((float) this.zd) > 0.00001D)
                    this.lastZSpeed = this.zd;

                if (allowedToMove) {
                    this.xd *= 0.98D;
                    this.zd *= 0.98D;
                }

                this.yd *= 0.98D;

                if (FancyBlockParticles.PHYSICS_CONFIG.isEntityCollision()) {
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

                                if (!FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation())
                                    this.calculateYAngle();

                                if (!FancyBlockParticles.RENDER_CONFIG.isFrozen())
                                    this.onGround = false;
                            }
                        }
                    }
                }

                // Water movement

                if (this.onGround) {
                    if (FancyBlockParticles.PHYSICS_CONFIG.isLowTraction()) {
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

        if (!FancyBlockParticles.PHYSICS_CONFIG.isLowTraction() && !FancyBlockParticles.PHYSICS_CONFIG.isBounceOffWalls()) {
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
        if (!FancyBlockParticles.RENDER_CONFIG.isEnabled())
            this.lifetime = 0;

        if (FBPKeyMappings.KILL_PARTICLES.get().isDown())
            this.killToggle = true;

        var u0 = 0.0F;
        var v0 = 0.0F;

        if (!FancyBlockParticles.RENDER_CONFIG.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F);
            v0 = this.sprite.getV(this.vo / 4.0F);
        }

        var u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F);
        var v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F);

        var posX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - info.getPosition().x);
        var posY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - info.getPosition().y);
        var posZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - info.getPosition().z);

        var scale = Mth.lerp(partialTicks, this.lastScale, this.quadSize) / 10.0F;

        var light = this.getLightColor(partialTicks);

        var alpha = (float) Mth.lerp(partialTicks, this.lastAlpha, this.alpha);

        if (FancyBlockParticles.PHYSICS_CONFIG.isRestOnFloor())
            posY += (float) scale;

        var smoothRotation = new Vector3d(0.0D, 0.0D, 0.0D);

        if (FancyBlockParticles.RENDER_CONFIG.getRotationMultiplier() > 0.0F) {
            smoothRotation.y = this.rotation.y;
            smoothRotation.z = this.rotation.z;

            if (!FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation())
                smoothRotation.x = this.rotation.x;

            if (!FancyBlockParticles.RENDER_CONFIG.isFrozen()) {
                if (FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation()) {
                    smoothRotation.y = Mth.lerp(partialTicks, this.lastRotation.y, this.rotation.y);
                    smoothRotation.z = Mth.lerp(partialTicks, this.lastRotation.z, this.rotation.z);
                } else
                    smoothRotation.x = Mth.lerp(partialTicks, this.lastRotation.x, this.rotation.x);
            }
        }

        FBPRenderHelper.renderCubeShaded(buffer, new Vector2f[]{ new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, posX, posY, posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.RENDER_CONFIG.isCartoonMode());
    }

    @Override
    public int getLightColor(float partialTick) {
        var box = this.getBoundingBox();

        return this.level.hasChunkAt(BlockPos.containing(this.x, this.y, this.z)) ? LevelRenderer.getLightColor(this.level, this.state, BlockPos.containing(this.x, this.y + ((box.maxY - box.minY) * 0.66D) + 0.01D - (FancyBlockParticles.PHYSICS_CONFIG.isRestOnFloor() ? this.quadSize / 10.0D : 0.0D), this.z)) : 0;
    }

    private double getMultiplier() {
        return Math.sqrt(this.xd * this.xd + this.zd * this.zd) * (FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation() ? 200.0D : 500.0D) * FancyBlockParticles.RENDER_CONFIG.getRotationMultiplier();
    }

    private void calculateYAngle() {
        var sin = Math.toDegrees(Math.asin(this.xd / Math.sqrt(this.xd * this.xd + this.zd * this.zd)));
        this.rotation.y = this.zd > 0.0D ? -sin : sin;
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
