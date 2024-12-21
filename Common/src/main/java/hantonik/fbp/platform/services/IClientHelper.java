package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface IClientHelper {
    float getShade(float normalX, float normalY, float normalZ, boolean shade);

    void renderBlock(ClientLevel level, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource bufferSource);

    default CompiledShaderProgram getParticleTranslucentShader() {
        return Minecraft.getInstance().getShaderManager().getProgram(CoreShaders.PARTICLE);
    }

    default CompiledShaderProgram getBlockTranslucentShader() {
        return Minecraft.getInstance().getShaderManager().getProgram(CoreShaders.RENDERTYPE_TRANSLUCENT_MOVING_BLOCK);
    }
}
