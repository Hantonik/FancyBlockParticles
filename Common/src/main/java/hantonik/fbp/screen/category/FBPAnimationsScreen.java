package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.platform.Services;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

public class FBPAnimationsScreen extends FBPAbstractOptionsScreen {
    public FBPAnimationsScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.animations"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var fancyPlacingAnimationButton = new FBPToggleButton(310, 20, Component.translatable("button.fbp.animations.fancy_placing_animation"), this.config.animations::isEnabled, button -> this.config.animations.setEnabled(!this.config.animations.isEnabled()), Tooltip.create(Component.translatable("tooltip.fbp.animations.fancy_placing_animation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isEnabled()))));

        if (Services.PLATFORM.isModLoaded("optifine"))
            fancyPlacingAnimationButton.setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.animations.fancy_placing_animation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.common.mod_incompatibility", Component.literal("OptiFine").withStyle(ChatFormatting.AQUA))).append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isEnabled()))));

        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.min_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.animations.getMinLifetime(), FancyBlockParticles.CONFIG.animations.getMinLifetime(), 0, 10, 1, button -> {
            this.config.animations.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.animations.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.animations.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.animations.getMinLifetime())).append(Component.translatable("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW)))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.max_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.animations.getMaxLifetime(), FancyBlockParticles.CONFIG.animations.getMaxLifetime(), 0, 10, 1, button -> {
            this.config.animations.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.animations.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.animations.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.animations.getMaxLifetime())).append(Component.translatable("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW)))));

        this.list.addBig(
                fancyPlacingAnimationButton
        );

        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.animations.render_outline"), this.config.animations::isRenderOutline, button -> this.config.animations.setRenderOutline(!this.config.animations.isRenderOutline()), Tooltip.create(Component.translatable("tooltip.fbp.animations.render_outline").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isRenderOutline())))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.animations.getSizeMultiplier(), FancyBlockParticles.CONFIG.animations.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.animations.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.animations.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.animations.getSizeMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW)))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get()
        );
    }

    @Override
    protected void resetConfig() {
        this.config.animations.reset();
    }
}
