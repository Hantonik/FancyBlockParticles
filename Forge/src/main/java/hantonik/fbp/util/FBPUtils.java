package hantonik.fbp.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.fml.ModList;
import sereneseasons.season.SeasonHooks;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPUtils {
    public static Biome.Precipitation getPrecipitationLevelRenderer(Holder<Biome> biome) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.getLevelRendererPrecipitation(biome) : biome.value().getPrecipitation();
    }

    public static boolean warmEnoughToRainLevelRenderer(Holder<Biome> biome, BlockPos pos, Level level) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooks.warmEnoughToRainHook(biome, pos, level) : biome.value().warmEnoughToRain(pos);
    }
}
