package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.List;

public class FBPCampfireSmokeParticle extends CampfireSmokeParticle implements IKillableParticle {
    private final Vector3d[] rotatedCube;

    private final float uo;
    private final float vo;

    private final float multiplier;

    private final float targetAlpha;
    private final float scaleAlpha;
    private float lastAlpha;

    private final float targetSize;
    private float lastSize;

    private boolean killToggle;

    protected FBPCampfireSmokeParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, boolean isSignal, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, isSignal);

        this.sprite = sprite;

        this.rCol = 0.62F;
        this.gCol = 0.59F;
        this.bCol = 0.56F;

        this.targetAlpha = FancyBlockParticles.CONFIG.campfireSmoke.getTransparency();
        this.alpha = 0.0F;

        this.targetSize = FancyBlockParticles.CONFIG.campfireSmoke.getSizeMultiplier() * 6.0F * (FancyBlockParticles.CONFIG.campfireSmoke.isRandomSize() ? this.quadSize : 1.0F);
        this.scaleAlpha = this.targetSize * 0.85F;
        this.quadSize = 0.0F;

        this.hasPhysics = true;

        this.rotatedCube = new Vector3d[FBPConstants.CUBE.length];

        var angleY = this.random.nextFloat();

        for (var i = 0; i < FBPConstants.CUBE.length; i++)
            this.rotatedCube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

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
                    this.xd *= 1.1D;
                    this.zd *= 1.1D;
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
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_PARTICLE_RENDER;
    }

    @Override
    protected int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);
        var j = 0;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
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

        var scale = Mth.lerp(partialTick, this.lastSize, this.quadSize);
        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        var cube = new Vector3d[this.rotatedCube.length];

        for (var i = 0; i < cube.length; i++) {
            var corner = new Vector3d();

            corner.x = this.rotatedCube[i].x;
            corner.y = this.rotatedCube[i].y;
            corner.z = this.rotatedCube[i].z;

            corner.mul(scale / 20.0F);
            corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        this.putCube(buffer, cube, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    private void putCube(VertexConsumer buffer, Vector3d[] cube, Vector2f[] uv, int light, float rCol, float gCol, float bCol, float alpha, boolean cartoon) {
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

            brightness *= 0.875F;

            if (cartoon) {
                this.addVertex(buffer, vec0, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(buffer, vec1, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(buffer, vec2, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(buffer, vec3, uv[0].x, uv[0].y, light, red, green, blue, alpha);
            } else {
                this.addVertex(buffer, vec0, uv[0].x, uv[0].y, light, red, green, blue, alpha);
                this.addVertex(buffer, vec1, uv[1].x, uv[1].y, light, red, green, blue, alpha);
                this.addVertex(buffer, vec2, uv[2].x, uv[2].y, light, red, green, blue, alpha);
                this.addVertex(buffer, vec3, uv[3].x, uv[3].y, light, red, green, blue, alpha);
            }
        }
    }

    private void addVertex(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float rCol, float gCol, float bCol, float alpha) {
        buffer.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final boolean isSignal;

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.campfireSmoke.isSpawnWhileFrozen())
                return null;

            return new FBPCampfireSmokeParticle(level, x, y, z, xd, yd, zd, this.isSignal, Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.QUARTZ_BLOCK.defaultBlockState()));
        }
    }
}
