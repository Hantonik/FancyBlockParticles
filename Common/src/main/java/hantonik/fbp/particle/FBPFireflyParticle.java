package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

public class FBPFireflyParticle extends FireflyParticle implements IKillableParticle {
    private float angleY;
    private float lastAngleY;

    private float lastAlpha;
    private float lastSize;

    private boolean moved;
    private boolean dimming;

    private int cooldown;

    private final float multiplier;

    private boolean killToggle;

    public FBPFireflyParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);

        this.yd = 0.00085D;

        this.gravity = 0.001F;
        this.friction = 0.985F;

        this.rCol = 1.0F;
        this.gCol = Mth.lerp(this.random.nextFloat(), 0.8F, 0.95F);
        this.bCol = Mth.lerp(this.random.nextFloat(), 0.45F, 0.76F) * this.gCol;

        this.alpha = 0.01F;

        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();
        this.quadSize = FancyBlockParticles.CONFIG.firefly.getSizeMultiplier() * (FancyBlockParticles.CONFIG.firefly.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.7F, 1.0F) : 1.0F);
        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.firefly.getMinLifetime(), FancyBlockParticles.CONFIG.firefly.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.firefly.getMinLifetime(), FancyBlockParticles.CONFIG.firefly.getMaxLifetime()) + 0.5F);

        this.rotateRandom(180.0);

        this.cooldown = (int) Mth.lerp(this.random.nextFloat(), 20.0F, 50.0F);

        this.multiplier = FancyBlockParticles.CONFIG.firefly.isRandomDimmingSpeed() ? FBPConstants.RANDOM.nextFloat(0.87F, 1.0F) : 1.0F;

        this.scale(1.0F);
    }

    @Override
    public FBPFireflyParticle scale(float scale) {
        super.scale(scale);

        var size = this.quadSize / 40.0F;
        this.setBoundingBox(new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

        return this;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAngleY = this.angleY;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.firefly.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!FancyBlockParticles.CONFIG.firefly.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                if (this.dimming) {
                    this.alpha *= this.multiplier * 0.95F;

                    if (this.age >= this.lifetime || this.onGround) {
                        if (this.alpha < 0.001F)
                            this.remove();
                    } else {
                        if (this.alpha < 0.01F) {
                            this.dimming = false;

                            this.cooldown = (int) Mth.lerp(this.random.nextFloat(), 20.0F, 50.0F);
                        }
                    }
                } else {
                    if (this.alpha < 1.0F) {
                        this.alpha /= this.multiplier * 0.95F;

                        if (this.alpha > 1.0F)
                            this.alpha = 1.0F;
                    }
                }

                if (this.cooldown <= 0) {
                    if (this.random.nextFloat() > 0.98F)
                        this.dimming = true;
                } else
                    this.cooldown--;

                if (this.onGround) {
                    this.dimming = true;

                    this.xd *= 0.9D;
                    this.zd *= 0.9D;
                } else {
                    if (this.random.nextFloat() > 0.99F || !this.moved) {
                        this.moved = true;

                        if (this.random.nextFloat() > 0.96F) {
                            this.rotateRandom(125.0D);

                            this.xd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D) * FancyBlockParticles.CONFIG.firefly.getSpeedMultiplier();
                            this.yd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D) * FancyBlockParticles.CONFIG.firefly.getSpeedMultiplier();
                            this.zd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D) * FancyBlockParticles.CONFIG.firefly.getSpeedMultiplier();
                        } else {
                            this.rotateRandom(35.0D);

                            this.xd = FBPConstants.RANDOM.nextDouble(-0.03D, 0.03D) * FancyBlockParticles.CONFIG.firefly.getSpeedMultiplier();
                            this.yd = FBPConstants.RANDOM.nextDouble(-0.03D, 0.03D) * FancyBlockParticles.CONFIG.firefly.getSpeedMultiplier();
                            this.zd = FBPConstants.RANDOM.nextDouble(-0.03D, 0.03D) * FancyBlockParticles.CONFIG.firefly.getSpeedMultiplier();
                        }

                        var speed = FBPRenderHelper.rotate(new Vector3d(this.xd, this.yd, this.zd), 0.0F, this.angleY, 0.0F);

                        this.xd = speed.x;
                        this.yd = speed.y;
                        this.zd = speed.z;
                    }
                }

                this.yd -= 0.015D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                this.xd *= this.friction;
                this.yd *= this.friction;
                this.zd *= this.friction;
            }
        }
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

        if (x != xo)
            this.xd = 0.0D;

        if (z != zo)
            this.zd = 0.0D;
    }

    private void rotateRandom(double maxDeg) {
        this.angleY = (float) Math.toRadians(FBPConstants.RANDOM.nextDouble(-1.0D, 1.0D) * maxDeg);
    }

    @Override
    protected void setLocationFromBoundingbox() {
        super.setLocationFromBoundingbox();

        var box = this.getBoundingBox();
        this.y = (box.minY + box.maxY) / 2.0D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);
        var j = 0;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return (int) Mth.lerp(this.calculateAlpha(partialTick), i == 0 ? j : i, 255.0F);
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        var u = this.sprite.getU(1.1F / 4.0F);
        var v = this.sprite.getV(1.1F / 4.0F);

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize);
        var alpha = this.calculateAlpha(partialTick);

        var light = this.getLightColor(partialTick);

        var cube = new Vector3d[FBPConstants.CUBE.length];

        for (var i = 0; i < cube.length; i++) {
            var corner = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0.0F, Mth.lerp(this.easeInOutCubic(partialTick), this.lastAngleY, this.angleY), 0.0F);

            corner.mul(scale / 40.0F);
            corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        this.putCube(buffer, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private float easeInOutCubic(float value) {
        return value < 0.5F ? (4.0F * (float) Math.pow(value, 3.0D)) : (1.0F - (float) Math.pow(-2.0D * value + 2.0D, 3.0D) / 2.0F);
    }

    private float calculateAlpha(float partialTick) {
        return FancyBlockParticles.CONFIG.firefly.getTransparency() * Mth.sqrt(1.0F - Mth.square(Mth.lerp(partialTick, this.lastAlpha, this.alpha) - 1.0F));
    }

    private void putCube(VertexConsumer buffer, Vector3d[] cube, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        var brightness = 1.0F;

        float red;
        float green;
        float blue;

        for (var i = 0; i < cube.length; i += 4) {
            var vec0 = cube[i];
            var vec1 = cube[i + 1];
            var vec2 = cube[i + 2];
            var vec3 = cube[i + 3];

            red = rCol * brightness;
            green = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.97F;

            this.addVertex(buffer, vec0, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec1, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec2, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec3, u, v, light, red, green, blue, alpha);
        }
    }

    private void addVertex(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        buffer.addVertex((float) pos.x, (float) pos.y, (float) pos.z).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setLight(light);
    }

    public record Provider() implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.firefly.isSpawnWhileFrozen())
                return null;

            return new FBPFireflyParticle(level, x, y, z, xd, yd, zd);
        }
    }
}
