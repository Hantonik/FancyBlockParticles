package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
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
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FBPSnowParticle extends WaterDropParticle {
    private final float uo;
    private final float vo;

    private final float scaleAlpha;

    private float lastScale;
    private float lastAlpha;

    private float multiplier;

    private final Vector3f rotation;
    private final Vector3f rotationStep;
    private final Vector3f lastRotation;

    public FBPSnowParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z);

        this.rotation = new Vector3f();
        this.lastRotation = new Vector3f();

        var rx = FBPConstants.RANDOM.nextDouble();
        var ry = FBPConstants.RANDOM.nextDouble();
        var rz = FBPConstants.RANDOM.nextDouble();

        this.rotationStep = new Vector3f(rx > 0.5F ? 1.0F : -1.0F, ry > 0.5F ? 1.0F : -1.0F, rz > 0.5F ? 1.0F : -1.0F);
        this.rotation.load(this.rotationStep);

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.sprite = sprite;

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.gravity = 1.0F;

        this.quadSize *= (float) FBPConstants.RANDOM.nextDouble(FancyBlockParticles.CONFIG.getScaleMultiplier() - 0.25D, FancyBlockParticles.CONFIG.getScaleMultiplier() + 0.25D);
        this.lifetime = FBPConstants.RANDOM.nextInt(250, 300);

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.scaleAlpha = this.quadSize * 0.75F;

        this.alpha = 0.0F;
        this.quadSize = 0.0F;

        this.hasPhysics = true;

        this.multiplier = 1.0F;

        if (FancyBlockParticles.CONFIG.isRandomFadingSpeed())
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
        this.lastRotation.load(this.rotation);

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastScale = this.quadSize;

        if (!Minecraft.getInstance().isPaused()) {
            this.age++;

            if (this.y < Minecraft.getInstance().player.getY() - (Minecraft.getInstance().options.renderDistance().get() * 16.0D))
                this.remove();

            var step = this.rotationStep.copy();
            step.mul((float) FancyBlockParticles.CONFIG.getRotationMultiplier() * 5.0F);

            this.rotation.add(step);

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

            if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).getBlock() instanceof LiquidBlock)
                this.remove();

            this.yd -= 0.04D * this.gravity;

            this.move(this.xd, this.yd, this.zd);

            if (this.onGround && FancyBlockParticles.CONFIG.isRestOnFloor())
                this.rotation.set(Math.round(this.rotation.x() / 90.0F) * 90.0F, this.rotation.y(), Math.round(this.rotation.z() / 90.0F) * 90.0F);

            this.xd *= 0.98D;

            if (this.yd < -0.2D)
                this.yd *= 0.75D;

            this.zd *= 0.98D;

            if (this.onGround) {
                this.xd *= 0.68D;
                this.zd *= 0.68D;

                this.rotationStep.mul(0.85F);

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

        if (!FancyBlockParticles.CONFIG.isLowTraction() && !FancyBlockParticles.CONFIG.isBounceOffWalls()) {
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
        if (!FancyBlockParticles.CONFIG.isEnabled())
            this.lifetime = 0;

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

        var light = this.getLightColor(partialTicks);

        var alpha = Mth.lerp(partialTicks, this.lastAlpha, this.alpha);
        var scale = Mth.lerp(partialTicks, this.lastScale, this.quadSize) / 10.0F;

        if (FancyBlockParticles.CONFIG.isRestOnFloor())
            posY += scale;

        var smoothRotation = new Vector3f();

        if (FancyBlockParticles.CONFIG.getRotationMultiplier() > 0.0D) {
            smoothRotation.set(smoothRotation.x(), this.rotation.y(), this.rotation.z());

            if (!FancyBlockParticles.CONFIG.isRandomRotation())
                smoothRotation.set(this.rotation.x(), smoothRotation.y(), smoothRotation.z());

            if (!FancyBlockParticles.CONFIG.isFrozen()) {
                var vec = this.rotation.copy();
                vec.lerp(this.lastRotation, partialTicks);

                if (FancyBlockParticles.CONFIG.isRandomRotation())
                    smoothRotation.set(smoothRotation.x(), vec.y(), vec.z());
                else
                    smoothRotation.set(vec.x(), smoothRotation.y(), smoothRotation.z());
            }
        }

        FBPRenderHelper.renderCubeShaded(buffer, new Vec2[]{ new Vec2(u1, v1), new Vec2(u1, v0), new Vec2(u0, v0), new Vec2(u0, v1) }, posX, posY, posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.isCartoonMode());
    }

    @Override
    public int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);

        if (!FancyBlockParticles.CONFIG.isFancySmoke())
            return i;

        var j = 0;

        var pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }
}
