package hantonik.fbp.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public interface IClientHelper {
    default Biome.Precipitation getPrecipitation(Holder<Biome> biome) {
        return biome.value().getPrecipitation();
    }

    default boolean warmEnoughToRain(Holder<Biome> biome, BlockPos pos, Level level) {
        return biome.value().warmEnoughToRain(pos);
    }

    default boolean coldEnoughToSnow(Holder<Biome> biome, BlockPos pos, Level level) {
        return biome.value().coldEnoughToSnow(pos);
    }
}
