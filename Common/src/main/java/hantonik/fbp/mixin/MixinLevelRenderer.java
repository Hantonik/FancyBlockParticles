package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.particle.FBPRainParticle;
import hantonik.fbp.particle.FBPSnowParticle;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
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
        if (FancyBlockParticles.CONFIG.global.isEnabled() && !FancyBlockParticles.CONFIG.global.isFreezeEffect()) {
            if (FancyBlockParticles.CONFIG.rain.isEnabled() || FancyBlockParticles.CONFIG.snow.isEnabled()) {
                if (this.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F) <= 0.0F)
                    return;

                var rainDensity = FancyBlockParticles.CONFIG.rain.getParticleDensity() * 4.0F * FancyBlockParticles.CONFIG.rain.getSimulationDistance() / 2;
                var snowDensity = FancyBlockParticles.CONFIG.snow.getParticleDensity() * 4.0F * FancyBlockParticles.CONFIG.snow.getSimulationDistance() / 2;

                var density = Math.max(rainDensity, snowDensity);

                for (var i = 0; i < density; i++) {
                    var angle = FBPConstants.RANDOM.nextDouble() * Math.PI * 2.0D;
                    var radius = Mth.sqrt(FBPConstants.RANDOM.nextFloat()) * Math.max(FancyBlockParticles.CONFIG.rain.getSimulationDistance(), FancyBlockParticles.CONFIG.snow.getSimulationDistance()) / 2 * 16.0F;

                    var x = this.minecraft.cameraEntity.getX() + radius * Math.cos(angle);
                    var y = this.minecraft.cameraEntity.getY();
                    var z = this.minecraft.cameraEntity.getZ() + radius * Math.sin(angle);

                    var pos = BlockPos.containing(x, y, z);
                    var surfaceHeight = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY();

                    var precipitation = this.level.getBiome(pos).value().getPrecipitationAt(pos);

                    if (this.minecraft.cameraEntity.position().distanceTo(new Vec3(x, y, z)) > (precipitation == Biome.Precipitation.RAIN ? FancyBlockParticles.CONFIG.rain.getSimulationDistance() : FancyBlockParticles.CONFIG.snow.getSimulationDistance()) * 16.0F)
                        continue;

                    y = (int) (y + 25.0D + FBPConstants.RANDOM.nextDouble() * 10.0D);

                    if (y <= surfaceHeight + 2)
                        y = surfaceHeight + 10;

                    if (precipitation == Biome.Precipitation.RAIN) {
                        if (FancyBlockParticles.CONFIG.rain.isEnabled())
                            if (i < rainDensity)
                                this.minecraft.particleEngine.add(new FBPRainParticle.Provider().createParticle(ParticleTypes.RAIN.getType(), this.level, x, y, z, 0.0D, 0.0D, 0.0D));
                    } else if (precipitation == Biome.Precipitation.SNOW) {
                        if (FancyBlockParticles.CONFIG.snow.isEnabled())
                            if (i < snowDensity)
                                this.minecraft.particleEngine.add(new FBPSnowParticle.Provider().createParticle(ParticleTypes.RAIN.getType(), this.level, x, y, z, 0.0D, 0.0D, 0.0D));
                    }
                }
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), method = "tickRain")
    private void addParticle(ClientLevel instance, ParticleOptions particleOptions, double x, double y, double z, double xd, double yd, double zd) {
        if (FancyBlockParticles.CONFIG.global.isEnabled()) {
            if (particleOptions.getType() == ParticleTypes.SMOKE && FancyBlockParticles.CONFIG.smoke.isEnabled())
                return;

            if (particleOptions.getType() == ParticleTypes.RAIN && FancyBlockParticles.CONFIG.rain.isEnabled())
                return;
        }

        instance.addParticle(particleOptions, x, y, z, xd, yd, zd);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"), method = "renderSnowAndRain")
    private Biome.Precipitation getPrecipitationAt(Biome instance, BlockPos pos, Operation<Biome.Precipitation> original) {
        if (FancyBlockParticles.CONFIG.global.isEnabled()) {
            if (original.call(instance, pos) == Biome.Precipitation.RAIN && FancyBlockParticles.CONFIG.rain.isEnabled())
                return Biome.Precipitation.NONE;

            if (original.call(instance, pos) == Biome.Precipitation.SNOW && FancyBlockParticles.CONFIG.snow.isEnabled())
                return Biome.Precipitation.NONE;
        }

        return original.call(instance, pos);
    }

    @Inject(at = @At("HEAD"), method = "renderHitOutline", cancellable = true)
    private void renderHitOutline(PoseStack stack, VertexConsumer consumer, Entity entity, double camX, double camY, double camZ, BlockPos pos, BlockState state, CallbackInfo callback) {
        if (!FancyBlockParticles.CONFIG.animations.isRenderOutline() && FBPPlacingAnimationManager.isHidden(pos))
            callback.cancel();
    }
}
