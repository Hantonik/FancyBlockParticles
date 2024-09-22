package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class NeoForgeClientHelper implements IClientHelper {
    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return Minecraft.getInstance().level.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public void renderBlock(ClientLevel level, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource.BufferSource bufferSource) {
        for (var type : model.getRenderTypes(state, RandomSource.create(state.getSeed(pos)), ModelData.EMPTY)) {
            var buffer = bufferSource.getBuffer(RenderTypeHelper.getMovingBlockRenderType(type));

            Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, model, state, pos, stack, buffer, false, RandomSource.create(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, type);
        }
    }

    @Override
    public ShaderInstance getParticleTranslucentShader() {
        return ModList.get().isLoaded("iris") || ModList.get().isLoaded("oculus") ? ShaderAccess.getParticleTranslucentShader() : IClientHelper.super.getParticleTranslucentShader();
    }

    @Override
    public ShaderInstance getBlockTranslucentShader() {
        if (ModList.get().isLoaded("iris") || ModList.get().isLoaded("oculus")) {
            var pipeline = Iris.getPipelineManager().getPipelineNullable();

            if (pipeline instanceof ShaderRenderingPipeline shaderPipeline) {
                var shader = shaderPipeline.getShaderMap().getShader(ShaderKey.MOVING_BLOCK);

                if (shader != null)
                    return shader;
            }
        }

        return IClientHelper.super.getBlockTranslucentShader();
    }
}
