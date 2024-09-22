package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ShaderKey;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class FabricClientHelper implements IClientHelper {
    @Override
    public void renderBlock(ClientLevel level, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource.BufferSource bufferSource) {
        var renderer = Minecraft.getInstance().getBlockRenderer();

        renderer.getModelRenderer().tesselateBlock(level, renderer.getBlockModel(state), state, pos, stack, bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(state, FancyBlockParticles.CONFIG.global.isCullParticles())), false, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY);
    }

    @Override
    public ShaderInstance getParticleTranslucentShader() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            var pipeline = Iris.getPipelineManager().getPipelineNullable();

            if (pipeline instanceof CoreWorldRenderingPipeline shaderPipeline) {
                var shader = shaderPipeline.getShaderMap().getShader(ShaderKey.PARTICLES);

                if (shader != null)
                    return shader;
            }
        }

        return IClientHelper.super.getParticleTranslucentShader();
    }

    @Override
    public ShaderInstance getBlockTranslucentShader() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            var pipeline = Iris.getPipelineManager().getPipelineNullable();

            if (pipeline instanceof CoreWorldRenderingPipeline shaderPipeline) {
                var shader = shaderPipeline.getShaderMap().getShader(ShaderKey.MOVING_BLOCK);

                if (shader != null)
                    return shader;
            }
        }

        return IClientHelper.super.getBlockTranslucentShader();
    }
}
