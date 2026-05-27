package hantonik.fbp.mixin;

import hantonik.fbp.platform.Services;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Missing sampler with Iris workaround
@Mixin(targets = "com.mojang.blaze3d.opengl.GlRenderPass")
public abstract class MixinGlRenderPass {
    @Final
    @Mutable
    @Shadow
    public static boolean VALIDATION;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo callback) {
        try {
            Services.class.getClassLoader().loadClass("net.irisshaders.iris.api.v0.IrisApi");

            VALIDATION = false;
        } catch (ClassNotFoundException e) {
            VALIDATION = true;
        }

        LogManager.getLogger("GlRenderPass").info("Setting Blaze3d's GlRenderPass.VALIDATION to [{}]", VALIDATION);
    }
}
