package hantonik.fbp.screen.category;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FBPAnimationsScreen extends FBPAbstractOptionsScreen {
    public FBPAnimationsScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.animations"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var animationBlacklistButton = this.openScreenButton(Component.translatable("screen.fbp.terrain.animation_blacklist").append("..."), () -> null /*new FBPAnimationBlacklistScreen(this, this.config)*/, Component.translatable("tooltip.fbp.common.option_unsupported"));
        animationBlacklistButton.setWidth(310);
        animationBlacklistButton.active = false;

        this.list.addBig(
                new FBPToggleButton(310, 20, Component.translatable("button.fbp.animations.fancy_placing_animation"), this.config.animations::isFancyPlacingAnimation, button -> this.config.animations.setFancyPlacingAnimation(!this.config.animations.isFancyPlacingAnimation()), Component.translatable("tooltip.fbp.animations.fancy_placing_animation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isFancyPlacingAnimation()))),
                new FBPToggleButton(310, 20, Component.translatable("button.fbp.animations.smooth_animation_lighting"), this.config.animations::isSmoothAnimationLighting, button -> this.config.animations.setSmoothAnimationLighting(!this.config.animations.isSmoothAnimationLighting()), Component.translatable("tooltip.fbp.animations.smooth_animation_lighting").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isSmoothAnimationLighting()))),

                animationBlacklistButton
        );
    }

    @Override
    protected void resetConfig() {
        this.config.animations.reset();
    }
}
