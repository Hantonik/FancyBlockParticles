package hantonik.fbp.screen.category;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

public class FBPAnimationsScreen extends FBPAbstractOptionsScreen {
    public FBPAnimationsScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.animations"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        Button animationBlacklistButton = this.openScreenButton(new TranslationTextComponent("screen.fbp.terrain.animation_blacklist").append("..."), () -> null /*new FBPAnimationBlacklistScreen(this, this.config)*/, new TranslationTextComponent("tooltip.fbp.common.option_unsupported"));
        animationBlacklistButton.setWidth(310);
        animationBlacklistButton.active = false;

        this.list.addBig(
                new FBPToggleButton(310, 20, new TranslationTextComponent("button.fbp.animations.fancy_placing_animation"), this.config.animations::isFancyPlacingAnimation, button -> this.config.animations.setFancyPlacingAnimation(!this.config.animations.isFancyPlacingAnimation()), new TranslationTextComponent("tooltip.fbp.animations.fancy_placing_animation").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isFancyPlacingAnimation()))),
                new FBPToggleButton(310, 20, new TranslationTextComponent("button.fbp.animations.smooth_animation_lighting"), this.config.animations::isSmoothAnimationLighting, button -> this.config.animations.setSmoothAnimationLighting(!this.config.animations.isSmoothAnimationLighting()), new TranslationTextComponent("tooltip.fbp.animations.smooth_animation_lighting").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isSmoothAnimationLighting()))),

                animationBlacklistButton
        );
    }

    @Override
    protected void resetConfig() {
        this.config.animations.reset();
    }
}
