package hantonik.fbp.mixin;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.particle.*;
import hantonik.fbp.util.BlacklistMode;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine {
    @Shadow
    protected ClientLevel level;

    @Shadow
    @Final
    private RandomSource random;

    @Inject(at = @At("RETURN"), method = "makeParticle", cancellable = true)
    private <T extends ParticleOptions> void makeParticle(T options, double x, double y, double z, double xd, double yd, double zd, CallbackInfoReturnable<Particle> callback) {
        if (!FancyBlockParticles.CONFIG.global.isEnabled())
            return;

        if (FancyBlockParticles.CONFIG.flame.isEnabled() && !(callback.getReturnValue() instanceof FBPFlameParticle)) {
            if (options instanceof SimpleParticleType type) {
                if (callback.getReturnValue() instanceof FlameParticle) {
                    if (options.getType() == ParticleTypes.FLAME || options.getType() == ParticleTypes.SOUL_FIRE_FLAME)
                        callback.setReturnValue(new FBPFlameParticle.Provider(options.getType() == ParticleTypes.SOUL_FIRE_FLAME).createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
                    if (options.getType() == ParticleTypes.SMALL_FLAME)
                        callback.setReturnValue(new FBPFlameParticle.SmallFlameProvider().createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
                }

                if (callback.getReturnValue() instanceof LavaParticle)
                    if (options.getType() == ParticleTypes.LAVA)
                        callback.setReturnValue(new FBPLavaParticle.Provider().createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
            }
        }

        if (FancyBlockParticles.CONFIG.smoke.isEnabled() && !(callback.getReturnValue() instanceof FBPSmokeParticle)) {
            if (options instanceof SimpleParticleType type) {
                if (callback.getReturnValue() instanceof SmokeParticle original)
                    if (options.getType() == ParticleTypes.SMOKE || options.getType() == ParticleTypes.LARGE_SMOKE)
                        callback.setReturnValue(new FBPSmokeParticle.Provider(original.getQuadSize(1)).createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));

                if (callback.getReturnValue() instanceof WhiteSmokeParticle original)
                    if (options.getType() == ParticleTypes.WHITE_SMOKE)
                        callback.setReturnValue(new FBPWhiteSmokeParticle.Provider(original.getQuadSize(1)).createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
            }
        }

        if (FancyBlockParticles.CONFIG.campfireSmoke.isEnabled() && !(callback.getReturnValue() instanceof FBPCampfireSmokeParticle)) {
            if (options instanceof SimpleParticleType type && callback.getReturnValue() instanceof CampfireSmokeParticle) {
                if (options.getType() == ParticleTypes.CAMPFIRE_COSY_SMOKE)
                    callback.setReturnValue(new FBPCampfireSmokeParticle.Provider(false).createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));

                if (options.getType() == ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)
                    callback.setReturnValue(new FBPCampfireSmokeParticle.Provider(true).createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
            }
        }

        if (FancyBlockParticles.CONFIG.trail.isEnabled() && !(callback.getReturnValue() instanceof FBPTrailParticle)) {
            if (options instanceof TrailParticleOption type && callback.getReturnValue() instanceof TrailParticle)
                if (options.getType() == ParticleTypes.TRAIL)
                    callback.setReturnValue(new FBPTrailParticle.Provider().createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
        }

        if ((FancyBlockParticles.CONFIG.rain.isEnabled() || FancyBlockParticles.CONFIG.snow.isEnabled()) && !(callback.getReturnValue() instanceof FBPRainParticle) && !(callback.getReturnValue() instanceof FBPSnowParticle)) {
            if (options instanceof SimpleParticleType type && callback.getReturnValue() instanceof WaterDropParticle) {
                if (options.getType() == ParticleTypes.RAIN) {
                    var pos = BlockPos.containing(x, y, z);
                    var precipitation = this.level.getBiome(pos).value().getPrecipitationAt(pos, this.level.getSeaLevel());

                    if (precipitation == Biome.Precipitation.SNOW)
                        callback.setReturnValue(new FBPSnowParticle.Provider().createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
                    else
                        callback.setReturnValue(new FBPRainParticle.Provider().createParticle(type, this.level, x, y, z, xd, yd, zd, this.random));
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

                    if (options == ParticleTypes.DRIPPING_WATER || options == ParticleTypes.DRIPPING_DRIPSTONE_WATER) {
                        alpha = FancyBlockParticles.CONFIG.rain.getTransparency(); // Small exception:)

                        var color = this.level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), 0.0F);

                        rCol = ARGB.redFloat(color);
                        gCol = Mth.clamp(ARGB.greenFloat(color) + 0.1F, 0.1F, 1.0F);
                        bCol = Mth.clamp(ARGB.blueFloat(color) + 0.5F, 0.5F, 1.0F);
                    }

                    if (options == ParticleTypes.DRIPPING_DRIPSTONE_WATER)
                        sound = SoundEvents.POINTED_DRIPSTONE_DRIP_WATER;

                    if (options == ParticleTypes.DRIPPING_LAVA || options == ParticleTypes.DRIPPING_DRIPSTONE_LAVA) {
                        rCol = 1.0F;
                        gCol = 0.75F;
                        bCol = 0.75F;
                    }

                    if (options == ParticleTypes.DRIPPING_DRIPSTONE_LAVA)
                        sound = SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA;

                    if (options == ParticleTypes.DRIPPING_HONEY) {
                        state = Blocks.HONEY_BLOCK.defaultBlockState();
                        sound = SoundEvents.BEEHIVE_DRIP;
                    }

                    if (options == ParticleTypes.DRIPPING_OBSIDIAN_TEAR)
                        lightLevel = 10;

                    callback.setReturnValue(new FBPDripParticle.Provider(state, sound, rCol, gCol, bCol, alpha, lightLevel).createParticle(options, this.level, x, y, z, xd, yd, zd, this.random));
                }
            }
        }

        if (FancyBlockParticles.CONFIG.terrain.isFancyBreakingParticles() && !(callback.getReturnValue() instanceof FBPTerrainParticle)) {
            if (options instanceof BlockParticleOption type && callback.getReturnValue() instanceof TerrainParticle original) {
                if (options.getType() == ParticleTypes.BLOCK) {
                    var mode = FancyBlockParticles.CONFIG.getBlockParticlesMode(type.getState().getBlock());

                    if (mode != BlacklistMode.VANILLA)
                        callback.setReturnValue(null);

                    if (mode == BlacklistMode.FANCY) {
                        if (!FancyBlockParticles.CONFIG.global.isFreezeEffect() || FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen())
                            if (this.level.getFluidState(original.pos).isEmpty())
                                callback.setReturnValue(new FBPTerrainParticle.Provider(original.pos, original.getQuadSize(1) * 5.0F, null, original.sprite, original.rCol, original.gCol, original.bCol).createParticle(type, this.level, x, y, z, 0.0D, 0.0D, 0.0D, this.random));
                    }
                }
            } else if (callback.getReturnValue() instanceof SnowflakeParticle original) {
                if (options.getType() == ParticleTypes.SNOWFLAKE) {
                    var mode = FancyBlockParticles.CONFIG.getBlockParticlesMode(Blocks.POWDER_SNOW);

                    if (mode != BlacklistMode.VANILLA) {
                        if (mode == BlacklistMode.BLACKLISTED || (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))
                            callback.setReturnValue(null);
                        else if (mode == BlacklistMode.FANCY)
                            callback.setReturnValue(new FBPTerrainParticle.Provider(BlockPos.containing(x, y, z), original.getQuadSize(1) * 5.0F, null, null, original.rCol, original.gCol, original.bCol).createParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POWDER_SNOW.defaultBlockState()), this.level, x, y, z, 0.0D, 0.0D, 0.0D, this.random));
                    }
                }
            }
        }

        if (callback.getReturnValue() instanceof BreakingItemParticle original) {
            if (options.getType() == ParticleTypes.ITEM_SNOWBALL || options instanceof ItemParticleOption data && data.getItem().is(Items.SNOWBALL)) {
                var mode = FancyBlockParticles.CONFIG.getBlockParticlesMode(Blocks.SNOW);

                if (mode != BlacklistMode.VANILLA)
                    callback.setReturnValue(null);

                if (FancyBlockParticles.CONFIG.misc.isFancySnowballParticles() && mode == BlacklistMode.FANCY)
                    callback.setReturnValue(new FBPTerrainParticle.Provider(BlockPos.containing(x, y, z), FBPConstants.RANDOM.nextFloat(0.35F, 0.6F) * FancyBlockParticles.CONFIG.misc.getSnowballParticleSizeMultiplier(), null, null, original.rCol, original.gCol, original.bCol).createParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState()), this.level, x, y, z, 0.0D, 0.0D, 0.0D, this.random));
            } else if (options.getType() == ParticleTypes.ITEM_SLIME || options instanceof ItemParticleOption data && data.getItem().is(Items.SLIME_BALL)) {
                var mode = FancyBlockParticles.CONFIG.getBlockParticlesMode(Blocks.SLIME_BLOCK);

                if (mode != BlacklistMode.VANILLA)
                    callback.setReturnValue(null);

                if (FancyBlockParticles.CONFIG.misc.isFancySlimeParticles() && mode == BlacklistMode.FANCY)
                    callback.setReturnValue(new FBPTerrainParticle.Provider(BlockPos.containing(x, y, z), FBPConstants.RANDOM.nextFloat(0.35F, 0.6F) * FancyBlockParticles.CONFIG.misc.getSlimeParticleSizeMultiplier(), null, null, original.rCol, original.gCol, original.bCol).createParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()), this.level, x, y, z, 0.0D, 0.0D, 0.0D, this.random));
            } else if (options.getType() == ParticleTypes.ITEM) {
                if (options instanceof ItemParticleOption data && data.getItem().is(Items.SPLASH_POTION)) {
                    var mode = FancyBlockParticles.CONFIG.getBlockParticlesMode(Blocks.GLASS);

                    if (mode != BlacklistMode.VANILLA)
                        callback.setReturnValue(null);

                    if (FancyBlockParticles.CONFIG.misc.isFancyBreakingSplashPotionParticles() && mode == BlacklistMode.FANCY)
                        callback.setReturnValue(new FBPTerrainParticle.Provider(BlockPos.containing(x, y, z), FBPConstants.RANDOM.nextFloat(0.5F, 0.75F) * FancyBlockParticles.CONFIG.misc.getBreakingSplashPotionParticleSizeMultiplier(), null, null, original.rCol, original.gCol, original.bCol).createParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState()), this.level, x, y, z, xd, yd, zd, this.random));
                }
            }
        }

        if (FancyBlockParticles.CONFIG.terrain.isFancyFallingDustParticles() && !(callback.getReturnValue() instanceof FBPTerrainParticle)) {
            if (options instanceof BlockParticleOption type && callback.getReturnValue() instanceof FallingDustParticle original) {
                if (options.getType() == ParticleTypes.FALLING_DUST) {
                    var mode = FancyBlockParticles.CONFIG.getBlockParticlesMode(type.getState().getBlock());

                    if (mode != BlacklistMode.VANILLA) {
                        if (mode == BlacklistMode.BLACKLISTED || (FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))
                            callback.setReturnValue(null);
                        else if (mode == BlacklistMode.FANCY)
                            callback.setReturnValue(new FBPTerrainParticle.Provider(BlockPos.containing(x, y, z), original.getQuadSize(1) * 6.0F, null, null, 1.0F, 1.0F, 1.0F).createParticle(type, this.level, x, y, z, 0.0D, 0.0D, 0.0D, this.random).setPower(0.2F).setYSpeed(0.0D));
                    }
                }
            }
        }
    }
}
