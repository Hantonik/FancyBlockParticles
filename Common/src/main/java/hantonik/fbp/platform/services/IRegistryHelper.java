package hantonik.fbp.platform.services;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public interface IRegistryHelper {
    default ResourceLocation getBlockKey(Block block) {
        return Registry.BLOCK.getKey(block);
    }

    default Block getBlock(ResourceLocation id) {
        return Registry.BLOCK.get(id);
    }
}
