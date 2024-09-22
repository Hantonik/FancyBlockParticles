package hantonik.fbp.screen.category;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.Util;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.Locale;

public class FBPOverlayScreen extends FBPAbstractOptionsScreen {
    public FBPOverlayScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.overlay"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var colorBox = new EditBox(this.font, 150, 20, Component.translatable("widget.fbp.overlay.freeze_effect_color"));

        colorBox.setValue("#" + String.format("%06X", this.config.overlay.getFreezeEffectColor()));
        colorBox.setEditable(!this.config.global.isLocked() && this.config.overlay.isFreezeEffectOverlay());
        colorBox.setFilter(text -> text.toUpperCase(Locale.ENGLISH).matches("^#[0-F.]{0,6}$"));
        colorBox.setFormatter((text, pos) -> FormattedCharSequence.forward(text, text.length() == 7 ? Style.EMPTY.withColor(Util.getOrThrow(TextColor.parseColor(text), IllegalStateException::new)) : Style.EMPTY));
        colorBox.setResponder(text -> {
            if (text.length() == 7)
                this.config.overlay.setFreezeEffectColor(Integer.parseInt(text.substring(1), 16));
        });

        this.list.addBig(
                new FBPToggleButton(310, 20, Component.translatable("button.fbp.overlay.freeze_effect_overlay"), this.config.overlay::isFreezeEffectOverlay, button -> {
                    this.config.overlay.setFreezeEffectOverlay(!this.config.overlay.isFreezeEffectOverlay());

                    this.rebuildWidgets();
                }, Tooltip.create(Component.translatable("tooltip.fbp.overlay.freeze_effect_overlay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.overlay.isFreezeEffectOverlay()))))
        );

        this.list.addSmall(
                new StringWidget(150, 21, Component.literal(" ").append(Component.translatable("widget.fbp.overlay.freeze_effect_color")).append(":"), this.font).alignLeft(),
                colorBox
        );
    }

    @Override
    protected void resetConfig() {
        this.config.overlay.reset();
    }
}
