package hantonik.fbp.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemPickupParticle.class)
public abstract class MixinItemPickupParticle extends Particle {
    protected MixinItemPickupParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks, CallbackInfo callback) {
        callback.cancel();

        // temporary solution
    }
}
