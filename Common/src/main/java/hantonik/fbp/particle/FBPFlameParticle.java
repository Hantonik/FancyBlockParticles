package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

public class FBPFlameParticle extends FlameParticle implements IKillableParticle {
    private final Vector3d startPos;
    private final Vector3d[] rotatedCube;

    private final boolean isSoulFire;

    private final float multiplier;

    private boolean hasChild;

    private float startSize;
    private float scaleAlpha;
    private float lastAlpha;
    private float lastSize;

    private boolean killToggle;

    public FBPFlameParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, boolean isSoulFire, boolean hasChild) {
        super(level, x, y, z, xd, yd, zd);

        this.yd = -0.00085D;
        this.gravity = -0.05F;

        this.isSoulFire = isSoulFire;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 0.0F;

        if (this.isSoulFire) {
            this.rCol = 0.2F;
            this.bCol = 0.9F;
        }

        this.alpha = 1.0F;

        this.sprite = FBPConstants.FBP_PARTICLE_SPRITE.get();
        this.quadSize = FancyBlockParticles.CONFIG.flame.getSizeMultiplier() * (FancyBlockParticles.CONFIG.flame.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.6F, 1.0F) : 1.0F) * 2.5F;
        this.lifetime = (int) FBPConstants.RANDOM.nextFloat(Math.min(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime()) + 0.5F);

        this.startPos = new Vector3d(x, y, z);
        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        var angleY = this.random.nextFloat();

        for (var i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.hasChild = hasChild;

        this.multiplier = FancyBlockParticles.CONFIG.flame.isRandomFadingSpeed() ? FBPConstants.RANDOM.nextFloat(0.9875F, 1.0F) : 1.0F;

        this.scale(1.0F);
    }

    @Override
    public FBPFlameParticle scale(float scale) {
        super.scale(scale);

        this.startSize = this.quadSize;
        this.scaleAlpha = this.quadSize * 0.35F;

        var size = this.quadSize / 80.0F;
        this.setBoundingBox(new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));

        return this;
    }

    @Override
    public float getQuadSize(float scale) {
        var factor = (this.age + scale) / this.lifetime;

        return this.quadSize * (1.0F - factor * factor * 0.5F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.flame.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!FancyBlockParticles.CONFIG.flame.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                if (this.age >= this.lifetime) {
                    this.quadSize *= this.multiplier * 0.95F;

                    if (this.alpha >= 0.01D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= this.multiplier * 0.95F;

                    var state = this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z));

                    if (this.alpha < 0.01D)
                        this.remove();
                    else if (this.alpha <= 0.325D && this.hasChild && (state.getBlock() instanceof TorchBlock || state.getBlock() instanceof CandleBlock)) {
                        this.hasChild = false;

                        Minecraft.getInstance().particleEngine.add(new FBPFlameParticle(this.level, this.startPos.x, this.startPos.y, this.startPos.z, 0, 0, 0, this.isSoulFire, false));
                    }
                }

                this.yd -= 0.02D * this.gravity;

                this.move(0.0D, this.yd, 0.0D);

                this.yd *= 0.95D;

                if (this.onGround) {
                    this.xd *= 0.9D;
                    this.zd *= 0.9D;
                }
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

    @Override
    protected void setLocationFromBoundingbox() {
        super.setLocationFromBoundingbox();

        var box = this.getBoundingBox();
        this.y = (box.minY + box.maxY) / 2.0D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_PARTICLE_RENDER;
    }

    @Override
    public int getLightColor(float partialTick) {
        var factor = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);

        var i = super.getLightColor(partialTick);
        var j = i & 255;
        var k = i >> 16 & 255;

        j = Math.min((int) (factor * 15.0F * 16.0F) + j, 240);

        i = j | k << 16;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return  i == 0 ? j : i;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        var u = this.sprite.getU(1.1F / 4.0F);
        var v = this.sprite.getV(1.1F / 4.0F);

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize);
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        if (this.age >= this.lifetime) {
            this.gCol = this.isSoulFire ? Math.min(1.0F, (scale / this.startSize) * 1.2F) : scale / this.startSize;

            if (this.isSoulFire)
                this.bCol = Math.min(1.0F, (scale / this.startSize) * 1.2F);
        }

        var cube = new Vector3d[this.rotatedCube.length];

        for (var i = 0; i < cube.length; i++) {
            var corner = new Vector3d();

            corner.x = this.rotatedCube[i].x;
            corner.y = this.rotatedCube[i].y;
            corner.z = this.rotatedCube[i].z;

            corner.mul(scale / 80.0F);
            corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        this.putCube(buffer, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
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

            brightness *= 0.95F;

            this.addVertex(buffer, vec0, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec1, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec2, u, v, light, red, green, blue, alpha);
            this.addVertex(buffer, vec3, u, v, light, red, green, blue, alpha);
        }
    }

    private void addVertex(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        buffer.addVertex((float) pos.x, (float) pos.y, (float) pos.z).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setLight(light);
    }

    @Nullable
    private static FBPFlameParticle create(ClientLevel level, double x, double y, double z, double xd, double zd, float scale, boolean isSoulFire) {
        if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.flame.isSpawnWhileFrozen())
            return null;

        var state = level.getBlockState(BlockPos.containing(x, y, z));

        if (state.getBlock() instanceof TorchBlock || state.getBlock() instanceof CandleBlock)
            y += 0.04D;

        return new FBPFlameParticle(level, x, y - 0.06D, z, xd, FBPConstants.RANDOM.nextDouble() * 0.025D, zd, isSoulFire, !(state.getBlock() instanceof TorchBlock) && !(state.getBlock() instanceof CandleBlock)).scale(scale);
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final boolean isSoulFire;

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return create(level, x, y, z, xd, zd, 1.0F, this.isSoulFire);
        }
    }

    @RequiredArgsConstructor
    public static class SmallFlameProvider implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return create(level, x, y, z, xd, zd, 0.5F, false);
        }
    }
}
