package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.List;

public class FBPRainParticle extends WaterDropParticle implements IKillableParticle {
    private final double angleY;

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
        super(level, x, y, z);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.sprite = sprite;

        this.lifetime = FBPConstants.RANDOM.nextInt(50, 70);

        this.targetSize = Math.max(FBPConstants.RANDOM.nextFloat(FancyBlockParticles.CONFIG.rain.getSizeMultiplier() - 0.1F, FancyBlockParticles.CONFIG.rain.getSizeMultiplier() + 0.1F) * 4.0F, 0.1F) * (FancyBlockParticles.CONFIG.rain.isRandomSize() ? FBPConstants.RANDOM.nextFloat(0.7F, 1.0F) : 1.0F);
        this.quadSize = 0.0F;
        this.gravity = 0.025F * FancyBlockParticles.CONFIG.rain.getGravityMultiplier();

        this.alpha = 0.0F;

        this.hasPhysics = true;

        this.angleY = FBPConstants.RANDOM.nextDouble() * 45.0D;

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

                        if (this.alpha < 0.6F) {
                            this.alpha += 0.085F * this.multiplier;

                            if (this.alpha > 0.6F)
                                this.alpha = 0.6F;
                        }
                    } else
                        this.remove();
                }

                var state = this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z));

                if (state.getBlock() instanceof LiquidBlock) {
                    this.remove();

                    if (state.getFluidState().is(FluidTags.LAVA) || state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state))
                        Minecraft.getInstance().particleEngine.add(new FBPSmokeParticle.Provider(this.quadSize / 2.0F).createParticle(ParticleTypes.SMOKE, this.level, this.x, this.y, this.z, 0.0D, 0.05D, 0.0D));
                }

                this.yd -= 0.04D * this.gravity;

                this.move(this.xd, this.yd, this.zd);

                this.yd *= 1.00025D;

                if (this.onGround) {
                    this.xd = 0.0D;
                    this.yd = -0.25D;
                    this.zd = 0.0D;

                    if (this.height > 0.075F)
                        this.height *= 0.725F;

                    if (this.quadSize < this.targetSize) {
                        this.quadSize += this.targetSize / 10.0F;

                        if (this.quadSize > this.targetSize)
                            this.quadSize = this.targetSize;
                    }

                    if (this.quadSize >= this.targetSize / 2.0F) {
                        this.alpha *= 0.75F * this.multiplier;

                        if (this.alpha <= 0.001F)
                            this.remove();
                    }
                }
            }
        }

        var color = this.level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), 0.0F);

        this.rCol = (float) color.x;
        this.gCol = (float) Mth.clamp(color.y + 0.1D, 0.1D, 1.0D);
        this.bCol = (float) Mth.clamp(color.y + 0.5D, 0.5D, 1.0D);

        if (Minecraft.getInstance().cameraEntity.position().distanceTo(new Vec3(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) > Math.min(FancyBlockParticles.CONFIG.rain.getSimulationDistance(), Minecraft.getInstance().options.simulationDistance().get()) * 16)
            this.remove();

        this.visible = Minecraft.getInstance().cameraEntity.position().distanceTo(new Vec3(this.x, Minecraft.getInstance().cameraEntity.getY(), this.z)) <= Math.min(FancyBlockParticles.CONFIG.rain.getRenderDistance(), Minecraft.getInstance().options.renderDistance().get()) * 16;
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
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    protected int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);
        var j = 0;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(pos);

        return i == 0 ? j : i;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        if (!this.visible)
            return;

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

        var width = Mth.lerp(partialTick, this.lastSize, this.quadSize) / 10.0F;
        var height = Mth.lerp(partialTick, this.lastHeight, this.height) / 10.0F;

        var alpha = Mth.lerp(partialTick, this.lastAlpha, this.alpha);

        var light = this.getLightColor(partialTick);

        var smoothRotation = new Vector3d(0.0D, this.angleY, 0.0D);

        FBPRenderHelper.renderCubeShaded(buffer, new Vector2f[] { new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, posX, posY + height, posZ, width, height, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.global.isCartoonMode());
    }

    @RequiredArgsConstructor
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            if (FancyBlockParticles.CONFIG.global.isFreezeEffect())
                return null;

            return new FBPRainParticle(level, x, y, z, 0.1D, -FBPConstants.RANDOM.nextDouble(0.75D, 0.99D), 0.1D, Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WATER.defaultBlockState()));
        }
    }
}
