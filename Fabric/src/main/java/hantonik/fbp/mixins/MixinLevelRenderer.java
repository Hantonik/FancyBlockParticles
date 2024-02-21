package hantonik.fbp.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.particle.FBPRainParticle;
import hantonik.fbp.particle.FBPSnowParticle;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    @Nullable
    private ClientLevel level;

    @Inject(at = @At("HEAD"), method = "tickRain")
    private void tickRain(Camera camera, CallbackInfo callback) {
        if (!FancyBlockParticles.CONFIG.isEnabled())
            return;

        if (FancyBlockParticles.CONFIG.isFancyRain() || FancyBlockParticles.CONFIG.isFancySnow()) {
            if (this.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F) <= 0.0F)
                return;

            var xd = (float) this.minecraft.player.getDeltaMovement().x * 26.0F;
            var zd = (float) this.minecraft.player.getDeltaMovement().z * 26.0F;

            var td = Mth.sqrt(xd * xd + zd * zd) / 25.0F;

            for (var i = 0; i < 16 * FancyBlockParticles.CONFIG.getWeatherParticleDensity(); i++) {
                var angle = FBPConstants.RANDOM.nextDouble() * Math.PI * 2.0D;
                var radius = Mth.sqrt(FBPConstants.RANDOM.nextFloat()) * 35.0F;

                var x = this.minecraft.player.getX() + xd + radius * Math.cos(angle);
                var z = this.minecraft.player.getZ() + zd + radius * Math.sin(angle);

                if (this.minecraft.player.distanceToSqr(x, this.minecraft.player.getY(), z) > this.minecraft.options.renderDistance * 32.0D)
                    continue;

                var pos = new BlockPos(x, this.minecraft.player.getY(), z);
                var biome = this.level.getBiome(pos).value();
                var surfaceHeight = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY();

                var y = (int) (this.minecraft.player.getY() + 15.0D + FBPConstants.RANDOM.nextDouble() * 10.0D + (this.minecraft.player.getDeltaMovement().y * 6.0D));

                if (y <= surfaceHeight + 2)
                    y = surfaceHeight + 10;

                if (biome.getPrecipitation() != Biome.Precipitation.NONE) {
                    if (biome.warmEnoughToRain(pos)) {
                        if (FancyBlockParticles.CONFIG.isFancyRain())
                            this.minecraft.particleEngine.add(new FBPRainParticle(this.level, x, y, z, 0.1D, -(FBPConstants.RANDOM.nextDouble(0.75D, 0.99D) + td / 2.0D), 0.1D, this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WATER.defaultBlockState())));
                    } else {
                        if (FancyBlockParticles.CONFIG.isFancySnow())
                            if (i % 2 == 0)
                                this.minecraft.particleEngine.add(new FBPSnowParticle(this.level, x, y, z, FBPConstants.RANDOM.nextDouble(-0.5D, 0.5D), -(FBPConstants.RANDOM.nextDouble(0.25D, 1.0D) + td * 1.5D), FBPConstants.RANDOM.nextDouble(-0.5D, 0.5D), this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.SNOW.defaultBlockState())));
                    }
                }
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), method = "tickRain")
    private void addParticle(ClientLevel instance, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        if (!FancyBlockParticles.CONFIG.isEnabled() || particleData.getType() != ParticleTypes.RAIN || (!FancyBlockParticles.CONFIG.isFancyRain() && !FancyBlockParticles.CONFIG.isFancySnow()))
            instance.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitation()Lnet/minecraft/world/level/biome/Biome$Precipitation;"), method = "renderSnowAndRain", cancellable = true)
    private void renderSnowAndRain(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo callback, @Local BlockPos.MutableBlockPos mutableBlockPos, @Local(ordinal = 4) int i1) {
        if (FancyBlockParticles.CONFIG.isEnabled()) {
            var biome = this.level.getBiome(mutableBlockPos).value();
            var rain = biome.warmEnoughToRain(mutableBlockPos);

            if (biome.getPrecipitation() != Biome.Precipitation.NONE) {
                if (rain && FancyBlockParticles.CONFIG.isFancyRain())
                    callback.cancel();

                if ((!rain || biome.coldEnoughToSnow(mutableBlockPos)) && FancyBlockParticles.CONFIG.isFancySnow())
                    callback.cancel();
            }
        }

        if (callback.isCancelled()) {
            if (i1 >= 0)
                Tesselator.getInstance().end();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            lightTexture.turnOffLightLayer();
        }
    }
}
