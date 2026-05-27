package hantonik.fbp.particle;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.particle.api.IFBPParticleRenderer;
import hantonik.fbp.renderer.state.FBPTerrainParticleRenderState;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class FBPRainParticle extends WaterDropParticle implements IFBPParticleRenderer, IKillableParticle {
    private final float rotationY;

    private final float uo;
    private final float vo;

    private final float multiplier;

    private final float targetSize;

    private float height;

    private float lastAlpha;
    private float lastSize;
    private float lastHeight;

    private boolean killToggle;
    private boolean visible;

    public FBPRainParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.lifetime = FBPConstants.RANDOM.nextInt(50, 70);

        this.targetSize = Math.max(FBPConstants.RANDOM.nextFloat(FancyBlockParticles.CONFIG.rain.getSizeMultiplier() - 0.1F, FancyBlockParticles.CONFIG.rain.getSizeMultiplier() + 0.1F) * 4.0F, 0.1F) * (FancyBlockParticles.CONFIG.rain.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.7F, 1.0F) : 1.0F);
        this.quadSize = 0.0F;
        this.gravity = 0.025F * FancyBlockParticles.CONFIG.rain.getGravityMultiplier();

        int color = this.level.environmentAttributes().getValue(EnvironmentAttributes.SKY_COLOR, Minecraft.getInstance().gameRenderer.getMainCamera().position());

        this.rCol = ARGB.redFloat(color);
        this.gCol = Mth.clamp(ARGB.greenFloat(color) + 0.1F, 0.1F, 1.0F);
        this.bCol = Mth.clamp(ARGB.blueFloat(color) + 0.5F, 0.5F, 1.0F);

        this.alpha = FancyBlockParticles.CONFIG.rain.getTransparency();

        this.hasPhysics = true;

        this.rotationY = (float) Math.toRadians(FBPConstants.RANDOM.nextDouble() * 45.0D);

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.multiplier = FancyBlockParticles.CONFIG.rain.isRandomFadingSpeed() ? FBPConstants.RANDOM.nextFloat(0.85F, 1.0F) : 1.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;
        this.lastHeight = this.height;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.rain.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                this.age++;

                if (this.y < Minecraft.getInstance().player.getY() - (Minecraft.getInstance().options.renderDistance().get() * 9.0D))
                    this.remove();

                if (!this.onGround) {
                    if (this.age < this.lifetime) {
                        var max = FancyBlockParticles.CONFIG.rain.getSizeMultiplier() * 0.5F;

                        if (this.quadSize < max) {
                            this.quadSize += 0.05F * this.multiplier;

                            if (this.quadSize > max)
                                this.quadSize = max;

                            this.height = this.quadSize;
                        }
                    } else
                        this.remove();
                }

                this.yd -= 0.04D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                this.yd *= 1.00025D;

                if (this.onGround) {
                    this.xd = 0.0D;
                    this.zd = 0.0D;

                    if (FancyBlockParticles.CONFIG.rain.isPuddle()) {
                        this.yd = -0.25D;

                        if (this.height > 0.075F)
                            this.height *= 0.725F;

                        if (this.quadSize < this.targetSize) {
                            this.quadSize += this.targetSize / 10.0F;

                            if (this.quadSize > this.targetSize)
                                this.quadSize = this.targetSize;
                        }

                        if (this.quadSize >= this.targetSize / 2.0F) {
                            this.alpha *= 0.75F * this.multiplier;

                            if (this.alpha < 0.01F)
                                this.remove();
                        } else
                            this.alpha = FancyBlockParticles.CONFIG.rain.getTransparency();
                    } else {
                        this.quadSize *= 0.85F * this.multiplier;
                        this.height = this.quadSize;

                        if (this.alpha >= 0.01F)
                            this.alpha *= 0.75F * this.multiplier;

                        if (this.alpha < 0.01F)
                            this.remove();
                    }
                } else
                    this.alpha = FancyBlockParticles.CONFIG.rain.getTransparency();

                var state = this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z).relative(Direction.DOWN));

                if (this.isInLava(this.getBoundingBox()) || ((state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state)) && this.onGround)) {
                    this.remove();

                    Minecraft.getInstance().particleEngine.add(new FBPSmokeParticle.Provider(this.quadSize / 5.0F).createParticle(ParticleTypes.SMOKE, this.level, this.x, this.y, this.z, 0.0D, 0.05D, 0.0D, this.random));
                }
            }
        }

        if (Minecraft.getInstance().getCameraEntity().position().distanceTo(new Vec3(this.x, Minecraft.getInstance().getCameraEntity().getY(), this.z)) > Math.min(FancyBlockParticles.CONFIG.rain.getSimulationDistance(), Minecraft.getInstance().options.simulationDistance().get()) * 16)
            this.remove();

        this.visible = Minecraft.getInstance().getCameraEntity().position().distanceTo(new Vec3(this.x, Minecraft.getInstance().getCameraEntity().getY(), this.z)) <= Math.min(FancyBlockParticles.CONFIG.rain.getRenderDistance(), Minecraft.getInstance().options.renderDistance().get()) * 16;
    }

    private boolean isInLava(AABB box) {
        if (!this.touchingUnloadedChunk()) {
            box = box.deflate(0.001D);

            var minX = Mth.floor(box.minX);
            var maxX = Mth.ceil(box.maxX);
            var minY = Mth.floor(box.minY);
            var maxY = Mth.ceil(box.maxY);
            var minZ = Mth.floor(box.minZ);
            var maxZ = Mth.ceil(box.maxZ);

            for (var x = minX; x < maxX; x++) {
                for (var y = minY; y < maxY; y++) {
                    for (var z = minZ; z < maxZ; z++) {
                        var pos = BlockPos.containing(x, y, z);
                        var fluidState = this.level.getFluidState(pos);

                        if (fluidState.is(FluidTags.LAVA))
                            if (fluidState.getHeight(this.level, pos) + y >= box.minY)
                                return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean touchingUnloadedChunk() {
        var center = this.getBoundingBox().inflate(1.0D).getCenter();

        return !this.level.isLoaded(BlockPos.containing(center.x, center.y, center.z));
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
    public ParticleRenderType getGroup() {
        return FBPConstants.FBP_PARTICLE_RENDER;
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
    public void extract(FBPTerrainParticleRenderState renderState, Camera info, float partialTick) {
        if (!this.visible)
            return;

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

        var width = Mth.lerp(partialTick, this.lastSize, this.quadSize) / 10.0F;
        var height = Mth.lerp(partialTick, this.lastHeight, this.height) / 10.0F;

        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightCoords(partialTick);

        FBPRenderHelper.renderCubeShaded(renderState, this.getLayer(), (float) posX, (float) posY + height, (float) posZ, width, height, new Vector3f(0.0F, this.rotationY, 0.0F), u0, u1, v0, v1, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    public record Provider() implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect())
                return null;

            return new FBPRainParticle(level, x, y, z, 0.1D, -FBPConstants.RANDOM.nextDouble(0.65D, 0.85D), 0.1D, Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(Blocks.WATER.defaultBlockState()).sprite());
        }
    }
}
