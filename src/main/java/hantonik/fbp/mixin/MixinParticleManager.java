package hantonik.fbp.mixin;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.particle.*;
import hantonik.fbp.util.FBPConstants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager {
    @Shadow
    protected ClientWorld level;

    @Final
    @Shadow
    private Random random;

    @Shadow
    public abstract void add(Particle particle);

    @Inject(at = @At("RETURN"), method = "makeParticle", cancellable = true)
    private <T extends IParticleData> void makeParticle(T particleData, double x, double y, double z, double xd, double yd, double zd, CallbackInfoReturnable<Particle> callback) {
        if (!FancyBlockParticles.CONFIG.global.isEnabled())
            return;

        if (FancyBlockParticles.CONFIG.flame.isEnabled() && !(callback.getReturnValue() instanceof FBPFlameParticle)) {
            if (particleData.getType() == ParticleTypes.FLAME || particleData.getType() == ParticleTypes.SOUL_FIRE_FLAME)
                callback.setReturnValue(new FBPFlameParticle.Provider(particleData.getType() == ParticleTypes.SOUL_FIRE_FLAME).createParticle((BasicParticleType) particleData, this.level, x, y, z, xd, yd, zd));

            if (particleData.getType() == ParticleTypes.LAVA)
                callback.setReturnValue(new FBPLavaParticle.Provider().createParticle((BasicParticleType) particleData, this.level, x, y, z, xd, yd, zd));
        }

        if (FancyBlockParticles.CONFIG.smoke.isEnabled() && !(callback.getReturnValue() instanceof FBPSmokeParticle))
            if (particleData.getType() == ParticleTypes.SMOKE || particleData.getType() == ParticleTypes.LARGE_SMOKE)
                callback.setReturnValue(new FBPSmokeParticle.Provider(((TexturedParticle) callback.getReturnValue()).getQuadSize(1)).createParticle((BasicParticleType) particleData, this.level, x, y, z, xd, yd, zd));

        if ((FancyBlockParticles.CONFIG.rain.isEnabled() || FancyBlockParticles.CONFIG.snow.isEnabled()) && !(callback.getReturnValue() instanceof FBPRainParticle) && !(callback.getReturnValue() instanceof FBPSnowParticle)) {
            if (particleData.getType() == ParticleTypes.RAIN) {
                BlockPos pos = new BlockPos(x, y, z);
                Biome biome = this.level.getBiome(pos);

                if (biome.getPrecipitation() != Biome.RainType.NONE) {
                    if (biome.getTemperature(pos) >= 0.15F)
                        callback.setReturnValue(new FBPRainParticle.Provider().createParticle((BasicParticleType) particleData, this.level, x, y, z, xd, yd, zd));
                    else
                        callback.setReturnValue(new FBPSnowParticle.Provider().createParticle((BasicParticleType) particleData, this.level, x, y, z, xd, yd, zd));
                }
            }
        }

        if (FancyBlockParticles.CONFIG.terrain.isFancyBreakingParticles() && !(callback.getReturnValue() instanceof FBPDiggingParticle)) {
            if (particleData.getType() == ParticleTypes.BLOCK) {
                if (callback.getReturnValue() instanceof DiggingParticle) {
                    DiggingParticle original = (DiggingParticle) callback.getReturnValue();

                    callback.setReturnValue(null);

                    if (this.level.getFluidState(original.pos).isEmpty())
                        if (FancyBlockParticles.CONFIG.isBlockParticlesEnabled(((BlockParticleData) particleData).getState().getBlock()) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))
                            callback.setReturnValue(new FBPDiggingParticle.Provider(original.pos, original.getQuadSize(1) * 5.0F, null, original.sprite, original.rCol, original.gCol, original.bCol).createParticle((BlockParticleData) particleData, this.level, x, y, z, 0, 0, 0));
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
            VoxelShape shape = state.getShape(this.level, pos);
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state);

            int particlesPerAxis = FancyBlockParticles.CONFIG.terrain.getParticlesPerAxis();

            if (!(state.getBlock() instanceof FlowingFluidBlock) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen())) {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    double dx = Math.min(1.0D, maxX - minX);
                    double dy = Math.min(1.0D, maxY - minY);
                    double dz = Math.min(1.0D, maxZ - minZ);

                    int particlesPerX = Math.max(2, MathHelper.ceil(dx * particlesPerAxis));
                    int particlesPerY = Math.max(2, MathHelper.ceil(dy * particlesPerAxis));
                    int particlesPerZ = Math.max(2, MathHelper.ceil(dz * particlesPerAxis));

                    for (int i = 0; i < particlesPerX; i++) {
                        for (int j = 0; j < particlesPerY; j++) {
                            for (int k = 0; k < particlesPerZ; k++) {
                                double x = ((i + 0.5D) / particlesPerX) * dx + minX;
                                double y = ((j + 0.5D) / particlesPerY) * dy + minY;
                                double z = ((k + 0.5D) / particlesPerZ) * dz + minZ;

                                this.add(new FBPDiggingParticle(this.level, pos.getX() + x, pos.getY() + y, pos.getZ() + z, x - 0.5D, -0.001D, z - 0.5D, (float) FBPConstants.RANDOM.nextDouble(0.75D, 1.0D), 1.0F, 1.0F, 1.0F, pos, state, null, sprite));
                            }
                        }
                    }
                });
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "crack", cancellable = true)
    public void crack(BlockPos pos, Direction side, CallbackInfo callback) {
        BlockState state = this.level.getBlockState(pos);

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.terrain.isFancyCrackingParticles() || !FancyBlockParticles.CONFIG.isBlockParticlesEnabled(state.getBlock()))
            return;

        callback.cancel();

        if (state.getRenderShape() != BlockRenderType.INVISIBLE) {
            int posX = pos.getX();
            int posY = pos.getY();
            int posZ = pos.getZ();

            AxisAlignedBB bounds = state.getShape(this.level, pos).bounds();
            RayTraceResult hit = Minecraft.getInstance().hitResult;

            if (hit == null)
                hit = new BlockRayTraceResult(new Vector3d(posX + 0.5D, posY + 0.5D, posZ + 0.5D), null, pos, false);

            double x;
            double y;
            double z;

            if (FancyBlockParticles.CONFIG.terrain.isSmartBreaking() && (!(state.getBlock() instanceof FlowingFluidBlock) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))) {
                x = hit.getLocation().x + FBPConstants.RANDOM.nextDouble(-0.21D, 0.21D) * Math.abs(bounds.maxX - bounds.minX);
                y = hit.getLocation().y + FBPConstants.RANDOM.nextDouble(-0.21D, 0.21D) * Math.abs(bounds.maxY - bounds.minY);
                z = hit.getLocation().z + FBPConstants.RANDOM.nextDouble(-0.21D, 0.21D) * Math.abs(bounds.maxZ - bounds.minZ);
            } else {
                x = posX + this.random.nextDouble() * (bounds.maxX - bounds.minX - 0.2D) + 0.1D + bounds.minX;
                y = posY + this.random.nextDouble() * (bounds.maxY - bounds.minY - 0.2D) + 0.1D + bounds.minY;
                z = posZ + this.random.nextDouble() * (bounds.maxZ - bounds.minZ - 0.2D) + 0.1D + bounds.minZ;
            }

            switch (side) {
                case NORTH: {
                    z = posZ + bounds.minZ - 0.1D;
                    break;
                }

                case EAST: {
                    x = posX + bounds.maxX + 0.1D;
                    break;
                }

                case SOUTH: {
                    z = posZ + bounds.maxZ + 0.1D;
                    break;
                }

                case WEST: {
                    x = posX + bounds.minX - 0.1D;
                    break;
                }

                case UP: {
                    y = posY + bounds.maxY + 0.1D;
                    break;
                }

                case DOWN: {
                    y = posY + bounds.minY - 0.1D;
                    break;
                }
            }

            if ((!(state.getBlock() instanceof FlowingFluidBlock) && !(FancyBlockParticles.CONFIG.global.isFreezeEffect() && !FancyBlockParticles.CONFIG.terrain.isSpawnWhileFrozen()))) {
                Int2ObjectMap<DestroyBlockProgress> destroyingBlocks = Minecraft.getInstance().levelRenderer.destroyingBlocks;

                int damage = 0;

                if (!destroyingBlocks.isEmpty()) {
                    for (DestroyBlockProgress progress : destroyingBlocks.values()) {
                        if (progress.getPos() == pos) {
                            damage = progress.getProgress();

                            break;
                        }
                    }
                }

                FBPDiggingParticle particle = new FBPDiggingParticle(this.level, x, y, z, 0.0D, 0.0D, 0.0D, 2.0F, 1.0F, 1.0F, 1.0F, pos, state, side, null);

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
