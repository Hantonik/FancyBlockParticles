package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class FabricClientHelper implements IClientHelper {
    @Override
    public void renderBlock(ClientLevel level, List<BlockModelPart> list, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource bufferSource) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, list, state, pos, stack, bufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)), false, OverlayTexture.NO_OVERLAY);
    }
}
