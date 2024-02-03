package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import org.joml.Vector2f;
import org.joml.Vector3d;

@Environment(EnvType.CLIENT)
public class FBPSnowParticle extends WaterDropParticle {
    private final float uo;
    private final float vo;

    private final double scaleAlpha;

    private double lastScale;
    private double lastAlpha;

    private float multiplier;

    private final Vector3d rotation;
    private final Vector3d rotationStep;
    private final Vector3d lastRotation;

    public FBPSnowParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z);

        this.rotation = new Vector3d();
        this.lastRotation = new Vector3d();

        var rx = FBPConstants.RANDOM.nextDouble();
        var ry = FBPConstants.RANDOM.nextDouble();
        var rz = FBPConstants.RANDOM.nextDouble();

        this.rotationStep = new Vector3d(rx > 0.5D ? 1.0D : -1.0D, ry > 0.5D ? 1.0D : -1.0D, rz > 0.5D ? 1.0D : -1.0D);
        this.rotation.set(this.rotationStep);

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.sprite = sprite;

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.gravity = 1.0F;

        this.quadSize *= (float) FBPConstants.RANDOM.nextDouble(FancyBlockParticles.RENDER_CONFIG.getScaleMultiplier() - 0.25D, FancyBlockParticles.RENDER_CONFIG.getScaleMultiplier() + 0.25D);
        this.lifetime = FBPConstants.RANDOM.nextInt(250, 300);

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.scaleAlpha = this.quadSize * 0.75F;

        this.alpha = 0.0F;
        this.quadSize = 0.0F;

        this.hasPhysics = true;

        this.multiplier = 1.0F;

        if (FancyBlockParticles.PHYSICS_CONFIG.isRandomFadingSpeed())
            this.multiplier *= FBPConstants.RANDOM.nextFloat(0.7F, 1.0F);

        this.scale(1);
    }

    @Override
    public Particle scale(float scale) {
        var particle = super.scale(scale);

        var s = this.quadSize / 10.0F;

        this.setBoundingBox(new AABB(this.x - s, this.y, this.z - s, this.x + s, this.y + 2 * s, this.z + s));

        return particle;
    }

    @Override
    public void tick() {
        this.lastRotation.set(this.rotation);

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastScale = this.quadSize;

        if (!Minecraft.getInstance().isPaused()) {
            this.age++;

            if (this.y < Minecraft.getInstance().player.getY() - (Minecraft.getInstance().options.renderDistance().get() * 16.0D))
                this.remove();

            this.rotation.add(this.rotationStep.mul(FancyBlockParticles.RENDER_CONFIG.getRotationMultiplier() * 5.0D, new Vector3d()));

            if (this.age >= this.lifetime) {
                this.quadSize *= 0.75F * this.multiplier;

                if (this.alpha > 0.01F && this.quadSize <= this.scaleAlpha)
                    this.alpha *= 0.65F * this.multiplier;

                if (this.alpha < 0.01F)
                    this.remove();
            } else {
                if (this.quadSize < 1.0F) {
                    this.quadSize += 0.075F * this.multiplier;

                    if (this.quadSize > 1.0F)
                        this.quadSize = 1.0F;
                }

                if (this.alpha < 1.0F) {
                    this.alpha += 0.045F * this.multiplier;

                    if (this.alpha > 1.0F)
                        this.alpha = 1.0F;
                }
            }

            if (this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).getBlock() instanceof LiquidBlock)
                this.remove();

            this.yd -= 0.04D * this.gravity;

            this.move(this.xd, this.yd, this.zd);

            if (this.onGround && FancyBlockParticles.PHYSICS_CONFIG.isRestOnFloor()) {
                this.rotation.x = Math.round(this.rotation.x / 90.0D) * 90.0D;
                this.rotation.z = Math.round(this.rotation.z / 90.0D) * 90.0D;
            }

            this.xd *= 0.98D;

            if (this.yd < -0.2D)
                this.yd *= 0.75D;

            this.zd *= 0.98D;

            if (this.onGround) {
                this.xd *= 0.68D;
                this.zd *= 0.68D;

                this.rotationStep.mul(0.85D);

                this.age += 2;
            }
        }
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
                this.xd *= 0.699D;
            if (z != zo)
                this.zd *= 0.699D;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTicks) {
        if (!FancyBlockParticles.RENDER_CONFIG.isEnabled())
            this.lifetime = 0;

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

        var light = this.getLightColor(partialTicks);

        var alpha = (float) Mth.lerp(partialTicks, this.lastAlpha, this.alpha);
        var scale = (float) Mth.lerp(partialTicks, this.lastScale, this.quadSize) / 10.0F;

        if (FancyBlockParticles.PHYSICS_CONFIG.isRestOnFloor())
            posY += scale;

        var smoothRotation = new Vector3d(0.0D, 0.0D, 0.0D);

        if (FancyBlockParticles.RENDER_CONFIG.getRotationMultiplier() > 0.0D) {
            smoothRotation.y = this.rotation.y;
            smoothRotation.z = this.rotation.z;

            if (!FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation())
                smoothRotation.x = this.rotation.x;

            if (!FancyBlockParticles.RENDER_CONFIG.isFrozen()) {
                var vec = this.rotation.lerp(this.lastRotation, partialTicks, new Vector3d());

                if (FancyBlockParticles.PHYSICS_CONFIG.isRandomRotation()) {
                    smoothRotation.y = vec.y;
                    smoothRotation.z = vec.z;
                } else
                    smoothRotation.x = vec.x;
            }
        }

        FBPRenderHelper.renderCubeShaded(buffer, new Vector2f[]{ new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, posX, posY, posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.RENDER_CONFIG.isCartoonMode());
    }

    @Override
    public int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);

        if (!FancyBlockParticles.PHYSICS_CONFIG.isFancySmoke())
            return i;

        var j = 0;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }
}
