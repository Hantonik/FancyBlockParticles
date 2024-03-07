package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class FBPFlameParticle extends FlameParticle {
    private float startScale;
    private float lastScale;

    private float lastAlpha;
    private float scaleAlpha;

    private float multiplier;

    private boolean spawnAnother;

    private final Vector3f startPos;
    private final Vector3f[] cube;

    public FBPFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, boolean spawnAnother) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.spawnAnother = spawnAnother;
        this.startPos = new Vector3f((float) x, (float) y, (float) z);

        this.quadSize *= (float) (FancyBlockParticles.CONFIG.getScaleMultiplier() * 25.0F);
        this.lifetime = FBPConstants.RANDOM.nextInt(3, 5);

        this.yd = -0.00085D;
        this.gravity = -0.05F;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 0.0F;

        this.alpha = 1.0F;

        this.sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WHITE_CONCRETE.defaultBlockState());

        this.cube = new Vector3f[FBPConstants.CUBE.length];

        var angleY = this.random.nextFloat();

        for (var i = 0; i < FBPConstants.CUBE.length; i++)
            this.cube[i] = FBPRenderHelper.rotate(FBPConstants.CUBE[i], 0, angleY, 0);

        this.multiplier = 1.0F;

        if (FancyBlockParticles.CONFIG.isRandomFadingSpeed())
            this.multiplier *= FBPConstants.RANDOM.nextFloat(0.9875F, 1.0F);

        this.scale(1);
    }

    @Override
    public Particle scale(float scale) {
        var particle = super.scale(scale);

        this.startScale = this.quadSize;
        this.scaleAlpha = this.quadSize * 0.35F;

        var s = this.quadSize / 80.0F;

        this.setBoundingBox(new AABB(this.x - s, this.y - s, this.z - s, this.x + s, this.y + s, this.z + s));

        return particle;
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        var s = (this.age + pScaleFactor) / this.lifetime;
        return this.quadSize * (1.0F - s * s * 0.5F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastScale = this.quadSize;

        if (!FancyBlockParticles.CONFIG.isFancyFlame())
            this.removed = true;

        if (++this.age >= this.lifetime) {
            this.quadSize *= 0.95F * this.multiplier;

            if (this.alpha > 0.01D && this.quadSize <= this.scaleAlpha)
                this.alpha *= 0.9F * this.multiplier;

            var state = this.level.getBlockState(new BlockPos(this.x, this.y, this.z));

            if (this.alpha <= 0.01D)
                this.remove();
            else if (this.alpha <= 0.325D && this.spawnAnother && (state.is(Blocks.TORCH) || state.getBlock() instanceof CandleBlock)) {
                this.spawnAnother = false;

                Minecraft.getInstance().particleEngine.add(new FBPFlameParticle(this.level, this.startPos.x(), this.startPos.y(), this.startPos.z(), 0, 0, 0, this.spawnAnother));
            }
        }

        this.yd -= 0.02D * this.gravity;

        this.move(0, this.yd, 0);

        this.yd *= 0.95D;

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
        if (!FancyBlockParticles.CONFIG.isEnabled())
            this.lifetime = 0;

        var u = this.sprite.getU(1.1F / 4.0F);
        var v = this.sprite.getV(1.1F / 4.0F);

        var posX = Mth.lerp(partialTicks, this.xo, this.x) - info.getPosition().x;
        var posY = Mth.lerp(partialTicks, this.yo, this.y) - info.getPosition().y;
        var posZ = Mth.lerp(partialTicks, this.zo, this.z) - info.getPosition().z;

        var scale = this.lastScale + (this.quadSize - this.lastScale) * partialTicks;

        var light = this.getLightColor(partialTicks);

        var alpha = this.lastAlpha + (this.alpha - this.lastAlpha) * partialTicks;

        if (this.age >= this.lifetime)
            this.gCol = scale / this.startScale;

        var cube = new Vector3f[this.cube.length];

        for (var i = 0; i < cube.length; i++) {
            var corner = new Vector3f(
                    this.cube[i].x(),
                    this.cube[i].y(),
                    this.cube[i].z()
            );

            corner.mul(scale / 80);
            corner.add((float) posX, (float) posY, (float) posZ);

            cube[i] = corner;
        }

        this.putCube(buffer, cube, u, v, light, this.rCol, this.gCol, this.bCol, alpha);
    }

    private void putCube(VertexConsumer buffer, Vector3f[] cube, float u, float v, int light, float r, float g, float b, float a) {
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

            brightness *= 0.95F;

            this.addVertex(buffer, vec0, u, v, light, red, green, blue, a);
            this.addVertex(buffer, vec1, u, v, light, red, green, blue, a);
            this.addVertex(buffer, vec2, u, v, light, red, green, blue, a);
            this.addVertex(buffer, vec3, u, v, light, red, green, blue, a);
        }
    }

    private void addVertex(VertexConsumer buffer, Vector3f pos, float u, float v, int light, float red, float green, float blue, float alpha) {
        buffer.vertex(pos.x(), pos.y(), pos.z()).uv(u, v).color(red, green, blue, alpha).uv2(light).endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        var s = (this.age + partialTick) / this.lifetime;

        s = Mth.clamp(s, 0.0F, 1.0F);

        var i = super.getLightColor(partialTick);
        var j = i & 255;
        var k = i >> 16 & 255;

        j += (int) (s * 15.0F * 16.0F);
        j = Math.min(j, 240);

        i = j | k << 16;

        var pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            var state = level.getBlockState(new BlockPos(x, y, z));

            if (state == Blocks.TORCH.defaultBlockState() || state.getBlock() instanceof CandleBlock)
                y += 0.04D;

            return new FBPFlameParticle(level, x, y - 0.06D, z, xSpeed, FBPConstants.RANDOM.nextDouble() * 0.025D, zSpeed, !state.is(Blocks.TORCH) && !(state.getBlock() instanceof CandleBlock));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SmallFlameProvider implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            var state = level.getBlockState(new BlockPos(x, y, z));

            if (state == Blocks.TORCH.defaultBlockState() || state.getBlock() instanceof CandleBlock)
                y += 0.04D;

            var particle = new FBPFlameParticle(level, x, y - 0.06D, z, xSpeed, FBPConstants.RANDOM.nextDouble() * 0.025D, zSpeed, !state.is(Blocks.TORCH) && !(state.getBlock() instanceof CandleBlock));

            particle.quadSize *= 0.5F;

            return particle;
        }
    }
}
