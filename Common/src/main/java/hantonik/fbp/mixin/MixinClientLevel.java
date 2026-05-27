package hantonik.fbp.mixin;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.particle.FBPTerrainParticle;
import hantonik.fbp.util.BlacklistMode;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level {
    @Shadow
    @Final
    private Minecraft minecraft;

    protected MixinClientLevel(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeHolder, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeHolder, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(at = @At("HEAD"), method = "addDestroyBlockEffect", cancellable = true)
    public void addDestroyBlockEffect(BlockPos pos, BlockState state, CallbackInfo callback) {
        if (FancyBlockParticles.CONFIG.getBlockParticlesMode(state.getBlock()) != BlacklistMode.VANILLA)
            callback.cancel();

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.terrain.isFancyBreakingParticles() || FancyBlockParticles.CONFIG.getBlockParticlesMode(state.getBlock()) != BlacklistMode.FANCY)
            return;

        if (!state.isAir() && state.shouldSpawnTerrainParticles()) {
            var shape = state.getShape((ClientLevel) (Object) this, pos);
            var sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();

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

                                this.minecraft.particleEngine.add(new FBPTerrainParticle((ClientLevel) (Object) this, pos.getX() + x, pos.getY() + y, pos.getZ() + z, x - 0.5D, -0.001D, z - 0.5D, FBPConstants.RANDOM.nextFloat(0.75F, 1.0F), 1.0F, 1.0F, 1.0F, pos, state, null, sprite));
                            }
                        }
                    }
                });
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addBreakingBlockEffect", cancellable = true)
    public void addBreakingBlockEffect(BlockPos pos, Direction side, CallbackInfo callback) {
        var state = this.getBlockState(pos);

        if (FancyBlockParticles.CONFIG.getBlockParticlesMode(state.getBlock()) != BlacklistMode.VANILLA)
            callback.cancel();

        if (!FancyBlockParticles.CONFIG.global.isEnabled() || !FancyBlockParticles.CONFIG.terrain.isFancyCrackingParticles() || FancyBlockParticles.CONFIG.getBlockParticlesMode(state.getBlock()) != BlacklistMode.FANCY)
            return;

        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            var posX = pos.getX();
            var posY = pos.getY();
            var posZ = pos.getZ();

            var bounds = state.getShape((ClientLevel) (Object) this, pos).bounds();
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

                var particle = new FBPTerrainParticle((ClientLevel) (Object) this, x, y, z, 0.0D, 0.0D, 0.0D, 2.0F, 1.0F, 1.0F, 1.0F, pos, state, side, null);

                if (FancyBlockParticles.CONFIG.terrain.isSmartBreaking()) {
                    particle.setPower(side == Direction.UP ? 0.7F : 0.15F);
                    particle.scale(0.325F + (damage / 10.0F) * 0.5F);
                } else {
                    particle.setPower(0.2F);
                    particle.scale(0.6F);
                }

                this.minecraft.particleEngine.add(particle);
            }
        }
    }
}
