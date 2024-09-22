package hantonik.fbp.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeRegistryHelper implements IRegistryHelper {
    @Override
    public ResourceLocation getBlockKey(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    @Override
    public Block getBlock(ResourceLocation id) {
        return ForgeRegistries.BLOCKS.getValue(id);
    }
}
