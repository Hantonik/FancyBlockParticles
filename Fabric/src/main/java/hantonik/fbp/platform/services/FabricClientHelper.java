package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricClientHelper implements IClientHelper {
    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        var constantAmbientLight = Minecraft.getInstance().level.effects().constantAmbientLight();

        if (shade) {
            return Math.min(normalX * normalX * 0.6F + normalY * normalY * (constantAmbientLight ? 0.9F : (3.0F + normalY) / 4.0F) + normalZ * normalZ * 0.8F, 1.0F);
        } else {
            return constantAmbientLight ? 0.9F : 1.0F;
        }
    }

    @Override
    public void renderBlock(ClientLevel level, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource.BufferSource bufferSource) {
        var renderer = Minecraft.getInstance().getBlockRenderer();

        renderer.getModelRenderer().tesselateBlock(level, renderer.getBlockModel(state), state, pos, stack, bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(state, FancyBlockParticles.CONFIG.global.isCullParticles())), false, RandomSource.create(), state.getSeed(pos), OverlayTexture.NO_OVERLAY);
    }

    @Override
    public ShaderInstance getParticleTranslucentShader() {
        return FabricLoader.getInstance().isModLoaded("iris") ? ShaderAccess.getParticleTranslucentShader() : IClientHelper.super.getParticleTranslucentShader();
    }

    @Override
    public ShaderInstance getBlockTranslucentShader() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
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
