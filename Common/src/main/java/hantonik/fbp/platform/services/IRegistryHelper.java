package hantonik.fbp.platform.services;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface IRegistryHelper {
    default ResourceLocation getBlockKey(Block block) {
        return Registry.BLOCK.getKey(block);
    }

    default Block getBlock(ResourceLocation id) {
        return Registry.BLOCK.get(id);
    }

    default List<Block> getBlocks() {
        return Registry.BLOCK.stream().toList();
    }
}
