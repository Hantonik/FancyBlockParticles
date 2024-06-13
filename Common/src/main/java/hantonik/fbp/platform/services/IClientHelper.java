package hantonik.fbp.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface IClientHelper {
    float getShade(float normalX, float normalY, float normalZ, boolean shade);

    default Biome.Precipitation getPrecipitationAt(Holder<Biome> biome, BlockPos pos) {
        return biome.value().getPrecipitationAt(pos);
    }
}
