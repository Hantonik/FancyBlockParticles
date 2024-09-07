package hantonik.fbp.mixin;

import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {
    protected MixinOptionsScreen(ITextComponent title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/screen/OptionsScreen;addButton(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 11), method = "init")
    protected void init(CallbackInfo callback) {
        this.addButton(
                new Button(
                        this.width / 2 - 155,
                        this.height / 6 + 146 - 6,
                        150,
                        20,
                        new TranslationTextComponent("key.fbp.category").append("..."),
                        button -> this.minecraft.setScreen(new FBPOptionsScreen((OptionsScreen) (Object) this))
                )
        );
    }
}
