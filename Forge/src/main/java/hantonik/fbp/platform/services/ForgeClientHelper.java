package hantonik.fbp.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.ModList;
import sereneseasons.season.SeasonHooks;

import java.util.Random;

public final class ForgeClientHelper implements IClientHelper {
    @Override
    public void renderBlock(ClientLevel level, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, MultiBufferSource.BufferSource bufferSource) {
        var type = ItemBlockRenderTypes.getMovingBlockRenderType(state);
        var buffer = bufferSource.getBuffer(type);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, model, state, pos, stack, buffer, false, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
    }

    @Override
    public Biome.Precipitation getPrecipitation(Biome biome) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.getLevelRendererPrecipitation(biome) : IClientHelper.super.getPrecipitation(biome);
    }

    @Override
    public boolean isColdEnoughToSnow(Biome biome, BlockPos pos, Level level) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.isColdEnoughToSnowHook(biome, pos, level) : IClientHelper.super.isColdEnoughToSnow(biome, pos, level);
    }
}
