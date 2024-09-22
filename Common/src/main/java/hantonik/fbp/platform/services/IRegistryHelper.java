package hantonik.fbp.platform.services;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public interface IRegistryHelper {
    default ResourceLocation getBlockKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    default Block getBlock(ResourceLocation id) {
        return BuiltInRegistries.BLOCK.get(id);
    }
}
