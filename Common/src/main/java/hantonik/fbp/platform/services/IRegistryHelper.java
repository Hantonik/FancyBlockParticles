package hantonik.fbp.platform.services;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface IRegistryHelper {
    default ResourceLocation getBlockKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    default Block getBlock(ResourceLocation id) {
        return BuiltInRegistries.BLOCK.get(id);
    }

    default List<Block> getBlocks() {
        return BuiltInRegistries.BLOCK.stream().toList();
    }
}
