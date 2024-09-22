package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public interface IClientHelper {
    default Biome.Precipitation getPrecipitation(Biome biome) {
        return biome.getPrecipitation();
    }

    default float getBiomeTemperature(Biome biome, BlockPos pos, Level level) {
        return biome.getTemperature(pos);
    }

    void renderBlock(ClientLevel level, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource.BufferSource bufferSource);

    default ShaderInstance getParticleTranslucentShader() {
        return GameRenderer.getParticleShader();
    }

    default ShaderInstance getBlockTranslucentShader() {
        return GameRenderer.getRendertypeTranslucentMovingBlockShader();
    }
}
