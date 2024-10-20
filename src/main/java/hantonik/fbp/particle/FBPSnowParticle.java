package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RainParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class FBPSnowParticle extends RainParticle implements IKillableParticle {
    private Vector3d rotation;
    private Vector3d rotationStep;
    private Vector3d lastRotation;

    private final float multiplier;

    private final float scaleAlpha;

    private final float targetSize;

    private final float uo;
    private final float vo;

    private float lastAlpha;
    private float lastSize;

    private double lastXSpeed;
    private double lastZSpeed;

    private boolean wasFrozen;
    private boolean killToggle;
    private boolean visible;

    public FBPSnowParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.sprite = sprite;

        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.snow.getMinLifetime(), FancyBlockParticles.CONFIG.snow.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.snow.getMinLifetime(), FancyBlockParticles.CONFIG.snow.getMaxLifetime()) + 0.5D);

        this.quadSize = Math.max((float) FBPConstants.RANDOM.nextDouble(FancyBlockParticles.CONFIG.snow.getSizeMultiplier() - 0.1D, FancyBlockParticles.CONFIG.snow.getSizeMultiplier() + 0.1D), 0.1F) * (FancyBlockParticles.CONFIG.snow.isRandomSize() ? (float) FBPConstants.RANDOM.nextDouble(0.7D, 1.0D) : 1.0F);
        this.targetSize = this.quadSize;

        this.scale(1.0F);

        this.gravity = FancyBlockParticles.CONFIG.snow.getGravityMultiplier();

        this.hasPhysics = true;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.alpha = 0.0F;

        double rx = FBPConstants.RANDOM.nextDouble();
        double ry = FBPConstants.RANDOM.nextDouble();
        double rz = FBPConstants.RANDOM.nextDouble();

        this.rotationStep = new Vector3d(rx > 0.5D ? 1.0D : -1.0D, ry > 0.5D ? 1.0D : -1.0D, rz > 0.5D ? 1.0D : -1.0D);

        this.lastRotation = Vector3d.ZERO;
        this.rotation = this.rotationStep;

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.scaleAlpha = this.quadSize * 0.75F;
        this.quadSize = 0.0F;

        this.multiplier = FancyBlockParticles.CONFIG.snow.isRandomFadingSpeed() ? (float) FBPConstants.RANDOM.nextDouble(0.8D, 1.0D) : 1.0F;
    }

    @Override
    public Particle scale(float scale) {
        super.scale(scale);

        float size = this.quadSize / 10.0F;

        this.setBoundingBox(new AxisAlignedBB(this.x - size, this.y, this.z - size, this.x + size, this.y + 2 * size, this.z + size));

        return this;
    }

    @Override
    public void tick() {
        if (FancyBlockParticles.CONFIG.snow.isBounceOffWalls()) {
            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (!Minecraft.getInstance().isPaused() && this.age > 0) {
                    if (!this.wasFrozen && (Math.abs(this.xd) > 0.00001D || Math.abs(this.zd) > 0.00001D)) {
                        if (this.xo == this.x)
                            this.xd = -this.lastXSpeed * 0.625D;
                        if (this.zo == this.z)
                            this.zd = -this.lastZSpeed * 0.625D;
                    } else
                        this.wasFrozen = false;
                }
            } else
                this.wasFrozen = true;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastSize = this.quadSize;

        this.lastRotation = this.rotation;

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.snow.isEnabled())
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                if (this.y < Minecraft.getInstance().player.getY() - (Minecraft.getInstance().options.renderDistance * 16.0D))
                    this.remove();

                if (!FancyBlockParticles.CONFIG.snow.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                    this.age++;

                this.rotation = this.rotation.add(this.rotationStep.scale(FancyBlockParticles.CONFIG.snow.getRotationMultiplier() * 5.0F));

                BlockPos pos = new BlockPos(this.x, this.y, this.z);
                Biome biome = this.level.getBiome(pos);

                if (this.age >= this.lifetime || FancyBlockParticles.getBiomeTemperature(biome, pos, this.level) >= 0.15F) {
                    this.quadSize *= 0.75F * this.multiplier;

                    if (this.alpha >= 0.01F && this.quadSize <= this.scaleAlpha)
                        this.alpha *= 0.65F * this.multiplier;

                    if (this.alpha < 0.01F) {
                        this.remove();

                        if (FancyBlockParticles.getBiomeTemperature(biome, pos, this.level) >= 0.15F)
                            Minecraft.getInstance().particleEngine.add(new FBPRainParticle.Provider().createParticle(ParticleTypes.RAIN.getType(), this.level, x, y, z, 0.0D, 0.0D, 0.0D));
                    }
                } else {
                    if (this.quadSize < this.targetSize) {
                        this.quadSize += 0.075F * this.multiplier;

                        if (this.quadSize > this.targetSize)
                            this.quadSize = this.targetSize;
                    }

                    if (this.alpha < 1.0F) {
                        this.alpha += 0.045F * this.multiplier;

                        if (this.alpha > 1.0F)
                            this.alpha = 1.0F;
                    }
                }

                BlockState state = this.level.getBlockState(pos);

                if (state.getBlock() instanceof FlowingFluidBlock) {
                    this.remove();

                    if (state.getFluidState().is(FluidTags.LAVA) || state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state))
                        Minecraft.getInstance().particleEngine.add(new FBPSmokeParticle.Provider(this.quadSize / 5.0F).createParticle(ParticleTypes.SMOKE, this.level, this.x, this.y, this.z, 0.0D, 0.05D, 0.0D));
                }

                if (!this.onGround)
                    this.yd -= 0.04D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                if (Math.abs(this.xd) > 0.00001D)
                    this.lastXSpeed = this.xd;
                if (Math.abs(this.zd) > 0.00001D)
                    this.lastZSpeed = this.zd;

                if (this.onGround && FancyBlockParticles.CONFIG.snow.isRestOnFloor())
                    this.rotation = new Vector3d(Math.round(this.rotation.x / 90.0D) * 90.0D, this.rotation.y, Math.round(this.rotation.z / 90.0D) * 90.0D);

                this.xd *= 0.98D;

                if (this.yd < -0.2D)
                    this.yd *= 0.75D;

                this.zd *= 0.98D;

                if (this.onGround) {
                    if (FancyBlockParticles.CONFIG.snow.isLowTraction()) {
                        this.xd *= 0.932D;
                        this.zd *= 0.932D;
                    } else {
                        this.xd *= 0.665D;
                        this.zd *= 0.665D;
                    }

                    this.rotationStep = this.rotationStep.scale(0.85D);

                    if (!FancyBlockParticles.CONFIG.snow.isInfiniteDuration() && !FancyBlockParticles.CONFIG.global.isInfiniteDuration())
                        this.age += 2;
                }
            }
        }

        if (Minecraft.getInstance().cameraEntity.position().distanceTo(new Vector3d(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) > FancyBlockParticles.CONFIG.snow.getSimulationDistance() * 16)
            this.remove();

        this.visible = Minecraft.getInstance().cameraEntity.position().distanceTo(new Vector3d(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) <= Math.min(FancyBlockParticles.CONFIG.snow.getRenderDistance(), Minecraft.getInstance().options.renderDistance) * 16;
    }

    @Override
    public void killParticle() {
        this.killToggle = true;
    }

    @Override
    public void move(double x, double y, double z) {
        double xo = x;
        double yo = y;
        double zo = z;

        if ((x != 0.0D || y != 0.0D || z != 0.0D) && x * x + y * y + z * z < MathHelper.square(100.0F)) {
            Vector3d vec = Entity.collideBoundingBoxHeuristically(null, new Vector3d(x, y, z), this.getBoundingBox(), this.level, ISelectionContext.empty(), new ReuseableStream<>(Stream.empty()));

            x = vec.x;
            y = vec.y;
            z = vec.z;
        }

        if (x != 0.0D || y != 0.0D || z != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            this.setLocationFromBoundingbox();
        }

        this.onGround = y != yo && yo < 0.0D;

        if (!FancyBlockParticles.CONFIG.snow.isLowTraction() && !FancyBlockParticles.CONFIG.snow.isBounceOffWalls()) {
            if (x != xo)
                this.xd *= 0.7D;

            if (z != zo)
                this.zd *= 0.7D;
        }
    }

    @Override
    public IParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    protected int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        int j = 0;

        BlockPos pos = new BlockPos(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getRawBrightness(pos, 0);

        return i == 0 ? j : i;
    }

    @Override
    public void render(IVertexBuilder builder, ActiveRenderInfo info, float partialTick) {
        if (!this.visible)
            return;

        float u0 = 0.0F;
        float v0 = 0.0F;

        if (!FancyBlockParticles.CONFIG.global.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F * 16.0F);
            v0 = this.sprite.getV(this.vo / 4.0F * 16.0F);
        }

        float u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F * 16.0F);
        float v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F * 16.0F);

        double posX = MathHelper.lerp(partialTick, this.xo, this.x) - info.getPosition().x;
        double posY = MathHelper.lerp(partialTick, this.yo, this.y) - info.getPosition().y;
        double posZ = MathHelper.lerp(partialTick, this.zo, this.z) - info.getPosition().z;

        float scale = MathHelper.lerp(partialTick, this.lastSize, this.quadSize) / 10.0F;
        float alpha = MathHelper.lerp(partialTick, this.lastAlpha, this.alpha);

        int light = this.getLightColor(partialTick);

        if (FancyBlockParticles.CONFIG.snow.isRestOnFloor())
            posY += scale;

        Vector3d smoothRotation = Vector3d.ZERO;

        if (FancyBlockParticles.CONFIG.snow.getRotationMultiplier() > 0.0F) {
            smoothRotation = new Vector3d(smoothRotation.x, this.rotation.y, this.rotation.z);

            if (!FancyBlockParticles.CONFIG.snow.isRandomRotation())
                smoothRotation = new Vector3d(this.rotation.x, smoothRotation.y, smoothRotation.z);

            if (!FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
                Vector3d vec = new Vector3d(
                        MathHelper.lerp(partialTick, this.lastRotation.x, this.rotation.x),
                        MathHelper.lerp(partialTick, this.lastRotation.y, this.rotation.y),
                        MathHelper.lerp(partialTick, this.lastRotation.z, this.rotation.z)
                );
                
                if (FancyBlockParticles.CONFIG.snow.isRandomRotation())
                    smoothRotation = new Vector3d(smoothRotation.x, vec.y, vec.z);
                else
                    smoothRotation = new Vector3d(vec.x, smoothRotation.y, smoothRotation.z);
            }
        }

        FBPRenderHelper.renderCubeShaded(builder, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, posX, posY, posZ, scale, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    @RequiredArgsConstructor
    public static class Provider implements IParticleFactory<BasicParticleType> {
        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect())
                return null;

            return new FBPSnowParticle(level, x, y, z, FBPConstants.RANDOM.nextDouble(-0.5D, 0.5D), -FBPConstants.RANDOM.nextDouble(0.25D, 1.0D), FBPConstants.RANDOM.nextDouble(-0.5D, 0.5D), Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.SNOW.defaultBlockState()));
        }
    }
}
