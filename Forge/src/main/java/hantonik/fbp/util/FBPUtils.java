package hantonik.fbp.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.fml.ModList;
import sereneseasons.season.SeasonHooksClient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPUtils {
    public static Biome.Precipitation getPrecipitationAtLevelRenderer(Holder<Biome> biome, BlockPos pos) {
        return ModList.get().isLoaded("sereneseasons") ? SeasonHooksClient.getPrecipitationAtLevelRendererHook(biome, pos) : biome.value().getPrecipitationAt(pos);
    }
}
