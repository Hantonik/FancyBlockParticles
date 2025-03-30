package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IClientHelper {
    float getShade(float normalX, float normalY, float normalZ, boolean shade);

    void renderBlock(ClientLevel level, List<BlockModelPart> list, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource bufferSource);
}
