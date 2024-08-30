package hantonik.fbp.screen.category;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class FBPAnimationsScreen extends FBPAbstractOptionsScreen {
    public FBPAnimationsScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslatableComponent("screen.fbp.category.animations"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var animationBlacklistButton = this.openScreenButton(new TranslatableComponent("screen.fbp.terrain.animation_blacklist").append("..."), () -> null /*new FBPAnimationBlacklistScreen(this, this.config)*/, new TranslatableComponent("tooltip.fbp.common.option_unsupported"));
        animationBlacklistButton.setWidth(310);
        animationBlacklistButton.active = false;

        this.list.addBig(
                new FBPToggleButton(310, 20, new TranslatableComponent("button.fbp.animations.fancy_placing_animation"), this.config.animations::isFancyPlacingAnimation, button -> this.config.animations.setFancyPlacingAnimation(!this.config.animations.isFancyPlacingAnimation()), new TranslatableComponent("tooltip.fbp.animations.fancy_placing_animation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isFancyPlacingAnimation()))),
                new FBPToggleButton(310, 20, new TranslatableComponent("button.fbp.animations.smooth_animation_lighting"), this.config.animations::isSmoothAnimationLighting, button -> this.config.animations.setSmoothAnimationLighting(!this.config.animations.isSmoothAnimationLighting()), new TranslatableComponent("tooltip.fbp.animations.smooth_animation_lighting").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isSmoothAnimationLighting()))),

                animationBlacklistButton
        );
    }

    @Override
    protected void resetConfig() {
        this.config.animations.reset();
    }
}
