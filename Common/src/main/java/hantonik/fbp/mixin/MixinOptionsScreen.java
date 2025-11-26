package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen {
    @Shadow
    protected abstract Button openScreenButton(Component title, Supplier<Screen> screen);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/SpacerElement;height(I)Lnet/minecraft/client/gui/layouts/SpacerElement;", ordinal = 0), method = "init")
    protected SpacerElement height(int height) {
        return SpacerElement.height(FancyBlockParticles.CONFIG.global.isFastSettings() ? height - 15 : height);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;"), method = "init")
    protected void init(CallbackInfo callback, @Local GridLayout.RowHelper helper) {
        if (FancyBlockParticles.CONFIG.global.isFastSettings())
            helper.addChild(this.openScreenButton(Component.translatable("key.fbp.category").append("..."), () -> new FBPOptionsScreen((OptionsScreen) (Object) this)));
    }
}
