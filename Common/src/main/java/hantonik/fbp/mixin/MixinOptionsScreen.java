package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen {
    @Shadow
    protected abstract Button openScreenButton(Component title, Supplier<Screen> screen);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToContents(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"), method = "init")
    protected void init(CallbackInfo callback, @Local GridLayout.RowHelper helper) {
        helper.addChild(this.openScreenButton(Component.translatable("key.category.fbp.category").append("..."), () -> new FBPOptionsScreen((OptionsScreen) (Object) this)));
    }
}
