package hantonik.fbp.mixin;

import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.particle.IKillableParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleGroup.class)
public abstract class MixinParticleGroup {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V"), method = "tickParticle")
    private void tickParticle(Particle particle, CallbackInfo callback) {
        if (!Minecraft.getInstance().isPaused() && FBPKeyMappings.KILL_PARTICLES.isDown())
            if (particle instanceof IKillableParticle killableParticle)
                killableParticle.killParticle();
    }
}
