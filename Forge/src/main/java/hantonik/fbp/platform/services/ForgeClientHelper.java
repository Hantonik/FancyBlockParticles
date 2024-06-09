package hantonik.fbp.platform.services;

import net.minecraft.client.Minecraft;

public final class ForgeClientHelper implements IClientHelper {
    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return Minecraft.getInstance().level.getShade(normalX, normalY, normalZ, shade);
    }
}
