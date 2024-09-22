package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public interface IClientHelper {
    float getShade(float normalX, float normalY, float normalZ, boolean shade);

    void renderBlock(ClientLevel level, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource.BufferSource bufferSource);

    default ShaderInstance getParticleTranslucentShader() {
        return GameRenderer.getParticleShader();
    }

    default ShaderInstance getBlockTranslucentShader() {
        return GameRenderer.getRendertypeTranslucentMovingBlockShader();
    }

    default Biome.Precipitation getPrecipitationAt(Holder<Biome> biome, BlockPos pos) {
        return biome.value().getPrecipitationAt(pos);
    }
}
