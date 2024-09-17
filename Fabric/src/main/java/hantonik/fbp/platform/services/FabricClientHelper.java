package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
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
}
