package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.ModList;
import sereneseasons.season.SeasonHooks;

public final class ForgeClientHelper implements IClientHelper {
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
    public Biome.Precipitation getPrecipitationAt(Holder<Biome> biome, BlockPos pos) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.getPrecipitationAtLevelRendererHook(biome, pos) : IClientHelper.super.getPrecipitationAt(biome, pos);
    }
}
