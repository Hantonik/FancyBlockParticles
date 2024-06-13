package hantonik.fbp.platform.services;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.fml.ModList;
import sereneseasons.season.SeasonHooks;

public final class ForgeClientHelper implements IClientHelper {
    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return Minecraft.getInstance().level.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public Biome.Precipitation getPrecipitationAt(Holder<Biome> biome, BlockPos pos) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.getPrecipitationAtLevelRendererHook(biome, pos) : IClientHelper.super.getPrecipitationAt(biome, pos);
    }
}
