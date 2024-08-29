package hantonik.fbp.platform.services;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.fml.ModList;
import sereneseasons.season.SeasonHooks;

public final class ForgeClientHelper implements IClientHelper {
    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return Minecraft.getInstance().level.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public Biome.Precipitation getPrecipitation(Holder<Biome> biome) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.getLevelRendererPrecipitation(biome) : IClientHelper.super.getPrecipitation(biome);
    }

    @Override
    public boolean warmEnoughToRain(Holder<Biome> biome, BlockPos pos, Level level) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.warmEnoughToRainHook(biome, pos, level) : IClientHelper.super.warmEnoughToRain(biome, pos, level);
    }

    @Override
    public boolean coldEnoughToSnow(Holder<Biome> biome, BlockPos pos, Level level) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.coldEnoughToSnowHook(biome.value(), pos, level) : IClientHelper.super.coldEnoughToSnow(biome, pos, level);
    }
}
