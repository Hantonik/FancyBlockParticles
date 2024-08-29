package hantonik.fbp.mixin;

import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {
    protected MixinOptionsScreen(Component title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/screens/OptionsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"/*, shift = At.Shift.AFTER*/, ordinal = 11), method = "init")
    protected void init(CallbackInfo callback) {
        this.addRenderableWidget(
                new Button(
                        this.width / 2 - 155,
                        this.height / 6 + 146 - 6,
                        150,
                        20,
                        Component.translatable("key.fbp.category").append("..."),
                        button -> this.minecraft.setScreen(new FBPOptionsScreen((OptionsScreen) (Object) this))
                )
        );
    }
}
