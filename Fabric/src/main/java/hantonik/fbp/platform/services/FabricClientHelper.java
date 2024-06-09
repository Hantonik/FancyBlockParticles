package hantonik.fbp.platform.services;

import net.minecraft.client.Minecraft;

public final class FabricClientHelper implements IClientHelper {
    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        var constantAmbientLight = Minecraft.getInstance().level.effects().constantAmbientLight();

        if (shade) {
            return Math.min(normalX * normalX * 0.6F + normalY * normalY * (constantAmbientLight ? 0.9F : (3.0F + normalY) / 4.0F) + normalZ * normalZ * 0.8F, 1.0F);
        } else {
            return constantAmbientLight ? 0.9F : 1.0F;
        }
    }
}
