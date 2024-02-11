package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@OnlyIn(Dist.CLIENT)
public class FBPSmokeParticle extends SmokeParticle {
    private double lastScale;

    private double lastAlpha;
    private double scaleAlpha;

    private double multiplier;

    private final Vector3d[] cube;

    public FBPSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float scale) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, scale, new SpriteSet() {
            @Override
            public TextureAtlasSprite get(int age, int lifetime) {
                return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WHITE_CONCRETE.defaultBlockState());
            }

            @Override
            public TextureAtlasSprite get(RandomSource random) {
                return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WHITE_CONCRETE.defaultBlockState());
            }
        });

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WHITE_CONCRETE.defaultBlockState());

        this.quadSize = scale * 10.0F;
        this.scaleAlpha = this.quadSize * 0.85F;

        var state = level.getBlockState(BlockPos.containing(x, y, z));

        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            this.quadSize *= 0.65F;
            this.gravity *= 0.25F;

            this.xd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);
            this.yd = FBPConstants.RANDOM.nextDouble() * 0.5D;
            this.zd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);

            this.yd *= 0.35F;

            this.scaleAlpha = this.quadSize * 0.5D;

            this.lifetime = FBPConstants.RANDOM.nextInt(7, 18);
        } else if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH) || state.getBlock() instanceof CandleBlock) {
            this.quadSize *= 0.45F;

            this.xd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);
            this.yd = FBPConstants.RANDOM.nextDouble() * 0.5D;
            this.zd = FBPConstants.RANDOM.nextDouble(-0.05D, 0.05D);

            this.xd *= 0.925F;
            this.yd = 0.005F;
            this.zd *= 0.925F;

            this.rCol = 0.275F;
            this.gCol = 0.275F;
            this.bCol = 0.275F;

            this.scaleAlpha = this.quadSize * 0.75D;

            this.lifetime = FBPConstants.RANDOM.nextInt(5, 10);
        } else {
            this.quadSize = scale;
            this.yd *= 0.935D;
        }

        this.quadSize *= (float) FancyBlockParticles.RENDER_CONFIG.getScaleMultiplier();

        this.cube = new Vector3d[FBPConstants.CUBE.length];

        var angleY = this.random.nextFloat();

        for (var i = 0; i < FBPConstants.CUBE.length; i++)
            this.cube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.alpha = 1.0F;

        this.multiplier = 0.75D;

        if (FancyBlockParticles.PHYSICS_CONFIG.isRandomFadingSpeed())
            this.multiplier = Mth.clamp(FBPConstants.RANDOM.nextDouble(0.425D, 1.15D), 0.5432D, 1.0D);

        this.scale(1);
    }

    @Override
    public Particle scale(float scale) {
        var particle = super.scale(scale);

        var s = this.quadSize / 20.0F;
        this.setBoundingBox(new AABB(this.x - s, this.y - s, this.z - s, this.x + s, this.y + s, this.z + s));

        return particle;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastScale = this.quadSize;

        if (++this.age >= this.lifetime) {
            this.quadSize *= (float) (0.9F * this.multiplier);

            if (this.alpha > 0.01D && this.quadSize <= this.scaleAlpha)
                this.alpha *= (float) (0.76F * this.multiplier);

            if (this.alpha <= 0.01D)
                this.remove();
        }

        this.yd += 0.004D;

        this.move(this.xd, this.yd, this.zd);

        if (this.y == this.yo) {
            this.xd *= 1.1D;
            this.zd *= 1.1D;
        }

        this.xd *= 0.95D;
        this.yd *= 0.95D;
        this.zd *= 0.95D;

        if (this.onGround) {
            this.xd *= 0.9D;
            this.zd *= 0.9D;
        }
    }

    @Override
    public void move(double x, double y, double z) {
        var yo = y;

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

        this.onGround = y != yo;
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
        return FBPConstants.FBP_PARTICLE_RENDER;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTicks) {
        if (!FancyBlockParticles.RENDER_CONFIG.isEnabled())
            this.lifetime = 0;

        var u = this.sprite.getU(1.1F / 4.0F);
        var v = this.sprite.getV(1.1F / 4.0F);

        var posX = Mth.lerp(partialTicks, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTicks, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTicks, this.zo, this.z) - info.getPosition().z;

        var scale = (float) Mth.lerp(partialTicks, this.lastScale, this.quadSize);

        var light = this.getLightColor(partialTicks);

        var alpha = (float) Mth.lerp(partialTicks, this.lastAlpha, this.alpha);

        var cube = new Vector3d[this.cube.length];

        for (var i = 0; i < cube.length; i++) {
            var corner = new Vector3d();

            corner.x = this.cube[i].x;
            corner.y = this.cube[i].y;
            corner.z = this.cube[i].z;

            corner.mul(scale / 20);
            corner.add(posX, posY, posZ);

            cube[i] = corner;
        }

        this.putCube(buffer, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(VertexConsumer buffer, Vector3d[] cube, float u, float v, int light, float r, float g, float b, float a) {
        var brightness = 1.0F;

        float red;
        float green;
        float blue;

        for (var i = 0; i < cube.length; i += 4) {
            var vec0 = cube[i];
            var vec1 = cube[i + 1];
            var vec2 = cube[i + 2];
            var vec3 = cube[i + 3];

            red = r * brightness;
            green = g * brightness;
            blue = b * brightness;

            brightness *= 0.875F;

            this.addVector(buffer, vec0, u, v, light, red, green, blue, a);
            this.addVector(buffer, vec1, u, v, light, red, green, blue, a);
            this.addVector(buffer, vec2, u, v, light, red, green, blue, a);
            this.addVector(buffer, vec3, u, v, light, red, green, blue, a);
        }
    }

    private void addVector(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float red, float green, float blue, float alpha) {
        buffer.vertex(pos.x, pos.y, pos.z).uv(u, v).color(red, green, blue, alpha).uv2(light).endVertex();
    }

    @Override
    protected int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);

        if (!FancyBlockParticles.PHYSICS_CONFIG.isFancySmoke())
            return i;

        var j = 0;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @OnlyIn(Dist.CLIENT)
    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final float scale;

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            var particle = new FBPSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.scale);

            particle.setColor(Mth.clamp(particle.rCol + 0.1F, 0.1F, 1), Mth.clamp(particle.gCol + 0.1F, 0.1F, 1), Mth.clamp(particle.bCol + 0.1F, 0.1F, 1));

            return particle;
        }
    }
}
