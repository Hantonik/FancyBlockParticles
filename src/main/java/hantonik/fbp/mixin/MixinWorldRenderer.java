package hantonik.fbp.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.particle.FBPRainParticle;
import hantonik.fbp.particle.FBPSnowParticle;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements IResourceManagerReloadListener, AutoCloseable {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private ClientWorld level;

    @Inject(at = @At("HEAD"), method = "tickRain")
    private void tickRain(ActiveRenderInfo info, CallbackInfo callback) {
        if (FancyBlockParticles.CONFIG.global.isEnabled() && !FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
            if (FancyBlockParticles.CONFIG.rain.isEnabled() || FancyBlockParticles.CONFIG.snow.isEnabled()) {
                if (this.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F) <= 0.0F)
                    return;

                float rainDensity = FancyBlockParticles.CONFIG.rain.getParticleDensity() * 4.0F * FancyBlockParticles.CONFIG.rain.getSimulationDistance() / 2;
                float snowDensity = FancyBlockParticles.CONFIG.snow.getParticleDensity() * 4.0F * FancyBlockParticles.CONFIG.snow.getSimulationDistance() / 2;

                float density = Math.max(rainDensity, snowDensity);

                for (int i = 0; i < density; i++) {
                    double angle = FBPConstants.RANDOM.nextDouble() * Math.PI * 2.0D;
                    double radius = MathHelper.sqrt((float) FBPConstants.RANDOM.nextDouble()) * Math.max(FancyBlockParticles.CONFIG.rain.getSimulationDistance(), FancyBlockParticles.CONFIG.snow.getSimulationDistance()) / 2 * 16.0F;

                    double x = this.minecraft.cameraEntity.getX() + radius * Math.cos(angle);
                    double y = this.minecraft.cameraEntity.getY();
                    double z = this.minecraft.cameraEntity.getZ() + radius * Math.sin(angle);

                    BlockPos pos = new BlockPos(x, y, z);
                    int surfaceHeight = this.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, pos).getY();

                    Biome biome = this.level.getBiome(pos);
                    Biome.RainType precipitation = FancyBlockParticles.getBiomePrecipitation(this.level, biome);

                    if (precipitation != Biome.RainType.NONE) {
                        if (this.minecraft.cameraEntity.position().distanceTo(new Vector3d(x, y, z)) > (precipitation == Biome.RainType.RAIN ? FancyBlockParticles.CONFIG.rain.getSimulationDistance() : FancyBlockParticles.CONFIG.snow.getSimulationDistance()) * 16.0F)
                            continue;

                        y = (int) (y + 25.0D + FBPConstants.RANDOM.nextDouble() * 10.0D);

                        if (y <= surfaceHeight + 2)
                            y = surfaceHeight + 10;

                        if (FancyBlockParticles.getBiomeTemperature(biome, pos, this.level) >= 0.15F) {
                            if (FancyBlockParticles.CONFIG.rain.isEnabled())
                                if (i < rainDensity)
                                    this.minecraft.particleEngine.add(new FBPRainParticle.Provider().createParticle(ParticleTypes.RAIN.getType(), this.level, x, y, z, 0.0D, 0.0D, 0.0D));
                        } else {
                            if (FancyBlockParticles.CONFIG.snow.isEnabled())
                                if (i < snowDensity)
                                    this.minecraft.particleEngine.add(new FBPSnowParticle.Provider().createParticle(ParticleTypes.RAIN.getType(), this.level, x, y, z, 0.0D, 0.0D, 0.0D));
                        }
                    }
                }
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"), method = "tickRain")
    private void addParticle(ClientWorld instance, IParticleData particleOptions, double x, double y, double z, double xd, double yd, double zd) {
        if (FancyBlockParticles.CONFIG.global.isEnabled()) {
            if (particleOptions.getType() == ParticleTypes.SMOKE && FancyBlockParticles.CONFIG.smoke.isEnabled())
                return;

            if (particleOptions.getType() == ParticleTypes.RAIN && FancyBlockParticles.CONFIG.rain.isEnabled())
                return;
        }

        instance.addParticle(particleOptions, x, y, z, xd, yd, zd);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;"), method = "renderSnowAndRain")
    private Biome getBiome(World instance, BlockPos pos) {
        Biome biome = instance.getBiome(pos);
        Biome.RainType precipitation = biome.getPrecipitation();

        if (FancyBlockParticles.CONFIG.global.isEnabled()) {
            if (precipitation == Biome.RainType.RAIN && FancyBlockParticles.CONFIG.rain.isEnabled())
                return BiomeRegistry.THE_VOID;
            else if (precipitation == Biome.RainType.SNOW && FancyBlockParticles.CONFIG.snow.isEnabled())
                return BiomeRegistry.THE_VOID;
        }

        return biome;
    }

    @Inject(at = @At("HEAD"), method = "renderHitOutline", cancellable = true)
    private void renderHitOutline(MatrixStack stack, IVertexBuilder consumer, Entity entity, double camX, double camY, double camZ, BlockPos pos, BlockState state, CallbackInfo callback) {
        if (!FancyBlockParticles.CONFIG.animations.isRenderOutline() && FBPPlacingAnimationManager.isHidden(pos))
            callback.cancel();
    }
}
