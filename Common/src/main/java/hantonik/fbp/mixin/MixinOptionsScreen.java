package hantonik.fbp.mixin;

import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen {
    @Shadow
    protected abstract Button openScreenButton(Component title, Supplier<Screen> screen);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToContents(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"), locals = LocalCapture.CAPTURE_FAILHARD, method = "init")
    protected void init(CallbackInfo callback, LinearLayout verticalLayout, LinearLayout horizontalLayout, GridLayout grid, GridLayout.RowHelper helper) {
        helper.addChild(this.openScreenButton(Component.translatable("key.fbp.category").append("..."), () -> new FBPOptionsScreen((OptionsScreen) (Object) this)));
    }
}
