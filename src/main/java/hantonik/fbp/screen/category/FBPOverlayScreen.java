package hantonik.fbp.screen.category;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.FBPStringWidget;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;

public class FBPOverlayScreen extends FBPAbstractOptionsScreen {
    public FBPOverlayScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.overlay"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        TextFieldWidget colorBox = new TextFieldWidget(this.font, 0, 0, 150, 20, new TranslationTextComponent("widget.fbp.overlay.freeze_effect_color"));

        colorBox.setValue("#" + String.format("%06X", this.config.overlay.getFreezeEffectColor()));
        colorBox.setEditable(!this.config.global.isLocked() && this.config.overlay.isFreezeEffectOverlay());
        colorBox.setFilter(text -> text.toUpperCase(Locale.ENGLISH).matches("^#[0-F.]{0,6}$"));
        colorBox.setFormatter((text, pos) -> IReorderingProcessor.forward(text, text.length() == 7 ? Style.EMPTY.withColor(Color.parseColor(text)) : Style.EMPTY));
        colorBox.setResponder(text -> {
            if (text.length() == 7)
                this.config.overlay.setFreezeEffectColor(Integer.parseInt(text.substring(1), 16));
        });

        this.list.addBig(
                new FBPToggleButton(310, 20, new TranslationTextComponent("button.fbp.overlay.freeze_effect_overlay"), this.config.overlay::isFreezeEffectOverlay, button -> {
                    this.config.overlay.setFreezeEffectOverlay(!this.config.overlay.isFreezeEffectOverlay());

                    this.rebuildWidgets();
                }, new TranslationTextComponent("tooltip.fbp.overlay.freeze_effect_overlay").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.overlay.isFreezeEffectOverlay())))
        );

        this.list.addSmall(
                new FBPStringWidget(150, 21, new TranslationTextComponent("widget.fbp.overlay.freeze_effect_color").append(": "), this.font).alignLeft(),
                colorBox
        );
    }

    @Override
    protected void resetConfig() {
        this.config.overlay.reset();
    }
}
