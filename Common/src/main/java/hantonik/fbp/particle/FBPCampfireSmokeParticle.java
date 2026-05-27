package hantonik.fbp.particle;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class FBPCampfireSmokeParticle extends CampfireSmokeParticle implements IKillableParticle {
    private final Vector3f[] rotatedNormal;
    private final Quaternionf rotation;

    private final float uo;
    private final float vo;

    private final float multiplier;

    private double xdo;
    private double zdo;

    private final float targetAlpha;
    private final float scaleAlpha;
    private float lastAlpha;

    private final float targetSize;
    private float lastSize;

    private boolean blocked;

    private boolean killToggle;

    protected FBPCampfireSmokeParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, boolean isSignal, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, isSignal, sprite);

        this.rCol = 0.62F;
        this.gCol = 0.59F;
        this.bCol = 0.56F;

        this.targetAlpha = FancyBlockParticles.CONFIG.campfireSmoke.getTransparency();
        this.alpha = 0.0F;

        this.targetSize = FancyBlockParticles.CONFIG.campfireSmoke.getSizeMultiplier() * 6.0F * (FancyBlockParticles.CONFIG.campfireSmoke.isRandomSize() ? this.quadSize : 1.0F);
        this.scaleAlpha = this.targetSize * 0.85F;
        this.quadSize = 0.0F;

        this.hasPhysics = true;

        this.rotatedNormal = new Vector3f[FBPConstants.CUBE_NORMALS.length];
        this.rotation = new Quaternionf().rotateY(this.random.nextFloat());

        for (var i = 0; i < FBPConstants.CUBE_NORMALS.length; i++)
            this.rotatedNormal[i] = FBPConstants.CUBE_NORMALS[i].rotate(this.rotation, new Vector3f());

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.multiplier = FancyBlockParticles.CONFIG.campfireSmoke.isRandomFadingSpeed() ? Mth.clamp(FBPConstants.RANDOM.nextFloat(0.85F, 1.1F), 0.95F, 1.0F) : 0.95F;

        this.scale(1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.campfireSmoke.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                this.age++;

                if (this.age >= this.lifetime) {
                    this.quadSize *= 0.98F * this.multiplier;

                    if (this.alpha >= 0.001D && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.96F * this.multiplier;

                    if (this.alpha < 0.001D)
                        this.remove();
                } else {
                    if (this.quadSize < this.targetSize) {
                        this.quadSize += 0.17F * this.multiplier;

                        if (this.quadSize > this.targetSize)
                            this.quadSize = this.targetSize;
                    }

                    if (this.alpha < this.targetAlpha) {
                        this.alpha += 0.1F * this.multiplier;

                        if (this.alpha > this.targetAlpha)
                            this.alpha = this.targetAlpha;
                    }
                }

                this.xd = this.xd + this.random.nextDouble() * (this.random.nextBoolean() ? 1.0D : -1.0D) / 5000.0D;
                this.yd = this.yd - this.gravity;
                this.zd = this.zd + this.random.nextDouble() * (this.random.nextBoolean() ? 1.0D : -1.0D) / 5000.0D;

                this.move(this.xd, this.yd, this.zd);

                if (this.y == this.yo) {
                    if (!this.blocked) {
                        this.xdo = this.xd;
                        this.zdo = this.zd;
                    }

                    this.blocked = true;

                    if (Math.abs(this.xd) < Math.abs(this.xdo) + 0.035D)
                        this.xd *= 1.1D;

                    if (Math.abs(this.zd) < Math.abs(this.zdo) + 0.035D)
                        this.zd *= 1.1D;
                } else {
                    if (this.blocked) {
                        this.blocked = false;

                        this.xd = Mth.lerp(FBPConstants.RANDOM.nextDouble(0.5D, 0.9D), this.xd, this.xdo);
                        this.zd = Mth.lerp(FBPConstants.RANDOM.nextDouble(0.5D, 0.9D), this.zd, this.zdo);
                    }
                }

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
    public Layer getLayer() {
        return Layer.TRANSLUCENT_TERRAIN;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        var i = super.getLightCoords(partialTick);
        var j = 0;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera info, float partialTick) {
        var u0 = 0.0F;
        var v0 = 0.0F;

        if (!FancyBlockParticles.CONFIG.global.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F);
            v0 = this.sprite.getV(this.vo / 4.0F);
        }

        var u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F);
        var v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F);

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.position().x;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.position().y;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.position().z;

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize);
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightCoords(partialTick);

        this.putCube(renderState, (float) posX, (float) posY, (float) posZ, scale / 20.0F, this.rotatedNormal, this.rotation, u0, u1, v0, v1, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    private void putCube(QuadParticleRenderState renderState, float x, float y, float z, float scale, Vector3f[] rotatedNormal, Quaternionf rotation, float u0, float u1, float v0, float v1, int light, float rCol, float gCol, float bCol, float alpha, boolean isCartoon) {
        var brightness = 1.0F;

        float red;
        float green;
        float blue;

        for (var i = 0; i < rotatedNormal.length; i++) {
            var normal = rotatedNormal[i];
            var face = new Vector3f(normal).mul(scale).add(x, y, z);
            var faceRotation = new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, 1.0F), normal);

            if (i < 2)
                rotation.mul(faceRotation, faceRotation);

            red = rCol * brightness;
            green = gCol * brightness;
            blue = bCol * brightness;

            brightness *= 0.875F;

            if (isCartoon)
                renderState.add(this.getLayer(), face.x, face.y, face.z, faceRotation.x, faceRotation.y, faceRotation.z, faceRotation.w, scale, u1, u1, v1, v1, ARGB.colorFromFloat(alpha, red, green, blue), light);
            else
                renderState.add(this.getLayer(), face.x, face.y, face.z, faceRotation.x, faceRotation.y, faceRotation.z, faceRotation.w, scale, u0, u1, v0, v1, ARGB.colorFromFloat(alpha, red, green, blue), light);
        }
    }

    public record Provider(boolean isSignal) implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.campfireSmoke.isSpawnWhileFrozen())
                return null;

            return new FBPCampfireSmokeParticle(level, x, y, z, xd, yd, zd, this.isSignal, Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(Blocks.QUARTZ_BLOCK.defaultBlockState()).sprite());
        }
    }
}
