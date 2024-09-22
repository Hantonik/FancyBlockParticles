package hantonik.fbp.mixin;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.particle.*;
import hantonik.fbp.platform.Services;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine {
    @Shadow
    protected ClientLevel level;

    @Final
    @Shadow
    private Random random;

    @Shadow
    public abstract void add(Particle particle);

    @Inject(at = @At("RETURN"), method = "makeParticle", cancellable = true)
    private <T extends ParticleOptions> void makeParticle(T particleData, double x, double y, double z, double xd, double yd, double zd, CallbackInfoReturnable<Particle> callback) {
        if (!FancyBlockParticles.CONFIG.global.isEnabled())
            return;

        if (FancyBlockParticles.CONFIG.flame.isEnabled() && !(callback.getReturnValue() instanceof FBPFlameParticle)) {
            if (particleData.getType() == ParticleTypes.FLAME || particleData.getType() == ParticleTypes.SOUL_FIRE_FLAME)
                callback.setReturnValue(new FBPFlameParticle.Provider(particleData.getType() == ParticleTypes.SOUL_FIRE_FLAME).createParticle((SimpleParticleType) particleData, this.level, x, y, z, xd, yd, zd));
            if (particleData.getType() == ParticleTypes.SMALL_FLAME)
                callback.setReturnValue(new FBPFlameParticle.SmallFlameProvider().createParticle((SimpleParticleType) particleData, this.level, x, y, z, xd, yd, zd));

            if (particleData.getType() == ParticleTypes.LAVA)
                callback.setReturnValue(new FBPLavaParticle.Provider().createParticle((SimpleParticleType) particleData, this.level, x, y, z, xd, yd, zd));
        }

        if (FancyBlockParticles.CONFIG.smoke.isEnabled() && !(callback.getReturnValue() instanceof FBPSmokeParticle))
            if (particleData.getType() == ParticleTypes.SMOKE || particleData.getType() == ParticleTypes.LARGE_SMOKE)
                callback.setReturnValue(new FBPSmokeParticle.Provider(((SingleQuadParticle) callback.getReturnValue()).getQuadSize(1)).createParticle((SimpleParticleType) particleData, this.level, x, y, z, xd, yd, zd));

        if ((FancyBlockParticles.CONFIG.rain.isEnabled() || FancyBlockParticles.CONFIG.snow.isEnabled()) && !(callback.getReturnValue() instanceof FBPRainParticle) && !(callback.getReturnValue() instanceof FBPSnowParticle)) {
            if (particleData.getType() == ParticleTypes.RAIN) {
                var pos = new BlockPos(x, y, z);
                var biome = this.level.getBiome(pos);
                var precipitation = Services.CLIENT.getPrecipitation(biome);

                if (precipitation != Biome.Precipitation.NONE) {
                    if (Services.CLIENT.warmEnoughToRain(biome, pos, this.level))
                        callback.setReturnValue(new FBPRainParticle.Provider().createParticle((SimpleParticleType) particleData, this.level, x, y, z, xd, yd, zd));
                    else
                        callback.setReturnValue(new FBPSnowParticle.Provider().createParticle((SimpleParticleType) particleData, this.level, x, y, z, xd, yd, zd));
                }
            }
        }

        if (FancyBlockParticles.CONFIG.drip.isEnabled() && !(callback.getReturnValue() instanceof FBPDripParticle)) {
            if (callback.getReturnValue() instanceof DripParticle original) {
                if (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.drip.isSpawnWhileFrozen())
                    callback.setReturnValue(null);
                else {
                    var state = original.type == Fluids.EMPTY ? Blocks.SNOW_BLOCK.defaultBlockState() : original.type.defaultFluidState().createLegacyBlock();
                    SoundEvent sound = null;
                    var rCol = original.rCol;
                    var gCol = original.gCol;
                    var bCol = original.bCol;
                    var alpha = 1.0F;
                    var lightLevel = -1;

                    if (particleData == ParticleTypes.DRIPPING_WATER || particleData == ParticleTypes.DRIPPING_DRIPSTONE_WATER) {
                        alpha = FancyBlockParticles.CONFIG.rain.getTransparency(); // Small exception:)

                        var color = this.level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), 0.0F);

                        rCol = (float) color.x;
                        gCol = (float) Mth.clamp(color.y + 0.1D, 0.1D, 1.0D);
                        bCol = (float) Mth.clamp(color.y + 0.5D, 0.5D, 1.0D);
                    }

                    if (particleData == ParticleTypes.DRIPPING_DRIPSTONE_WATER)
                        sound = SoundEvents.POINTED_DRIPSTONE_DRIP_WATER;

                    if (particleData == ParticleTypes.DRIPPING_DRIPSTONE_LAVA)
                        sound = SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA;

                    if (particleData == ParticleTypes.DRIPPING_HONEY) {
                        state = Blocks.HONEY_BLOCK.defaultBlockState();
                        sound = SoundEvents.BEEHIVE_DRIP;
                    }

                    if (particleData == ParticleTypes.DRIPPING_OBSIDIAN_TEAR)
                        lightLevel = 10;

                    callback.setReturnValue(new FBPDripParticle.Provider(state, sound, rCol, gCol, bCol, alpha, lightLevel).createParticle((SimpleParticleType) particleData, this.level, x, y, z, xd, yd, zd));
                }
            }
        }

        if (FancyBlockParticles.CONFIG.terrain.isFancyBreakingParticles() && !(callback.getReturnValue() instanceof FBPTerrainParticle)) {
            if (particleData.getType() == ParticleTypes.BLOCK) {
                if (callback.getReturnValue() instanceof TerrainParticle original) {
                    callback.setReturnValue(null);

                    if (this.level.getFluidState(original.pos).isEmpty())
                        if (FancyBlockParticles.CONFIG.isBlockParticlesEnabled(((BlockParticleOption) particleData).getState().getBlock()) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))
                            callback.setReturnValue(new FBPTerrainParticle.Provider(original.pos, original.getQuadSize(1) * 5.0F, null, original.sprite, original.rCol, original.gCol, original.bCol).createParticle((BlockParticleOption) particleData, this.level, x, y, z, 0, 0, 0));
                }
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V"), method = "tickParticle")
    private void tickParticle(Particle particle, CallbackInfo callback) {
        if (!Minecraft.getInstance().isPaused() && FBPKeyMappings.KILL_PARTICLES.isDown())
            if (particle instanceof IKillableParticle)
                ((IKillableParticle) particle).killParticle();
    }

    @Inject(at = @At("HEAD"), method = "destroy", cancellable = true)
    public void destroy(BlockPos pos, BlockState state, CallbackInfo callback) {
        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.terrain.isFancyBreakingParticles() || !FancyBlockParticles.CONFIG.isBlockParticlesEnabled(state.getBlock()))
            return;

        callback.cancel();

        if (!state.isAir()) {
            var shape = state.getShape(this.level, pos);
            var sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state);

            var particlesPerAxis = FancyBlockParticles.CONFIG.terrain.getParticlesPerAxis();

            if (!(state.getBlock() instanceof LiquidBlock) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen())) {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    var dx = Math.min(1.0D, maxX - minX);
                    var dy = Math.min(1.0D, maxY - minY);
                    var dz = Math.min(1.0D, maxZ - minZ);

                    var particlesPerX = Math.max(2, Mth.ceil(dx * particlesPerAxis));
                    var particlesPerY = Math.max(2, Mth.ceil(dy * particlesPerAxis));
                    var particlesPerZ = Math.max(2, Mth.ceil(dz * particlesPerAxis));

                    for (var i = 0; i < particlesPerX; i++) {
                        for (var j = 0; j < particlesPerY; j++) {
                            for (var k = 0; k < particlesPerZ; k++) {
                                var x = ((i + 0.5D) / particlesPerX) * dx + minX;
                                var y = ((j + 0.5D) / particlesPerY) * dy + minY;
                                var z = ((k + 0.5D) / particlesPerZ) * dz + minZ;

                                this.add(new FBPTerrainParticle(this.level, pos.getX() + x, pos.getY() + y, pos.getZ() + z, x - 0.5D, -0.001D, z - 0.5D, FBPConstants.RANDOM.nextFloat(0.75F, 1.0F), 1.0F, 1.0F, 1.0F, pos, state, null, sprite));
                            }
                        }
                    }
                });
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "crack", cancellable = true)
    public void crack(BlockPos pos, Direction side, CallbackInfo callback) {
        var state = this.level.getBlockState(pos);

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.terrain.isFancyCrackingParticles() || !FancyBlockParticles.CONFIG.isBlockParticlesEnabled(state.getBlock()))
            return;

        callback.cancel();

        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            var posX = pos.getX();
            var posY = pos.getY();
            var posZ = pos.getZ();

            var bounds = state.getShape(this.level, pos).bounds();
            var hit = Minecraft.getInstance().hitResult;

            if (hit == null)
                hit = new BlockHitResult(new Vec3(posX + 0.5D, posY + 0.5D, posZ + 0.5D), null, pos, false);

            double x;
            double y;
            double z;

            if (FancyBlockParticles.CONFIG.terrain.isSmartBreaking() && (!(state.getBlock() instanceof LiquidBlock) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))) {
                x = hit.getLocation().x + FBPConstants.RANDOM.nextDouble(-0.21D, 0.21D) * Math.abs(bounds.maxX - bounds.minX);
                y = hit.getLocation().y + FBPConstants.RANDOM.nextDouble(-0.21D, 0.21D) * Math.abs(bounds.maxY - bounds.minY);
                z = hit.getLocation().z + FBPConstants.RANDOM.nextDouble(-0.21D, 0.21D) * Math.abs(bounds.maxZ - bounds.minZ);
            } else {
                x = posX + this.random.nextDouble() * (bounds.maxX - bounds.minX - 0.2D) + 0.1D + bounds.minX;
                y = posY + this.random.nextDouble() * (bounds.maxY - bounds.minY - 0.2D) + 0.1D + bounds.minY;
                z = posZ + this.random.nextDouble() * (bounds.maxZ - bounds.minZ - 0.2D) + 0.1D + bounds.minZ;
            }

            switch (side) {
                case NORTH -> z = posZ + bounds.minZ - 0.1D;
                case EAST -> x = posX + bounds.maxX + 0.1D;
                case SOUTH -> z = posZ + bounds.maxZ + 0.1D;
                case WEST -> x = posX + bounds.minX - 0.1D;
                case UP -> y = posY + bounds.maxY + 0.1D;
                case DOWN -> y = posY + bounds.minY - 0.1D;
            }

            if ((!(state.getBlock() instanceof LiquidBlock) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))) {
                var destroyingBlocks = Minecraft.getInstance().levelRenderer.destroyingBlocks;

                var damage = 0;

                if (!destroyingBlocks.isEmpty()) {
                    for (var progress : destroyingBlocks.values()) {
                        if (progress.getPos() == pos) {
                            damage = progress.getProgress();

                            break;
                        }
                    }
                }

                var particle = new FBPTerrainParticle(this.level, x, y, z, 0.0D, 0.0D, 0.0D, 2.0F, 1.0F, 1.0F, 1.0F, pos, state, side, null);

                if (FancyBlockParticles.CONFIG.terrain.isSmartBreaking()) {
                    particle.setPower(side == Direction.UP ? 0.7F : 0.15F);
                    particle.scale(0.325F + (damage / 10.0F) * 0.5F);
                } else {
                    particle.setPower(0.2F);
                    particle.scale(0.6F);
                }

                this.add(particle);
            }
        }
    }
}
