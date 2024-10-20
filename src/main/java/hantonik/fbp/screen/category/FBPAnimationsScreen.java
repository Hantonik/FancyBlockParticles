package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;

import java.text.DecimalFormat;

public class FBPAnimationsScreen extends FBPAbstractOptionsScreen {
    public FBPAnimationsScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.animations"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        FBPToggleButton fancyPlacingAnimationButton = new FBPToggleButton(310, 20, new TranslationTextComponent("button.fbp.animations.fancy_placing_animation"), this.config.animations::isEnabled, button -> this.config.animations.setEnabled(!this.config.animations.isEnabled()), new TranslationTextComponent("tooltip.fbp.animations.fancy_placing_animation").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isEnabled())));

        if (ModList.get().isLoaded("optifine"))
            fancyPlacingAnimationButton.setTooltip(new TranslationTextComponent("tooltip.fbp.animations.fancy_placing_animation").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.common.mod_incompatibility", new StringTextComponent("OptiFine").withStyle(TextFormatting.AQUA))).append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isEnabled())));

        DelayedSupplier<FBPSliderButton> minLifetimeSlider = new DelayedSupplier<>();
        DelayedSupplier<FBPSliderButton> maxLifetimeSlider = new DelayedSupplier<>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.min_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.animations.getMinLifetime(), FancyBlockParticles.CONFIG.animations.getMinLifetime(), 0, 10, 1, button -> {
            this.config.animations.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.animations.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.animations.min_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.animations.getMinLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.max_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.animations.getMaxLifetime(), FancyBlockParticles.CONFIG.animations.getMaxLifetime(), 0, 10, 1, button -> {
            this.config.animations.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.animations.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.animations.max_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.animations.getMaxLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));

        this.list.addBig(
                fancyPlacingAnimationButton
        );

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.animations.render_outline"), this.config.animations::isRenderOutline, button -> this.config.animations.setRenderOutline(!this.config.animations.isRenderOutline()), new TranslationTextComponent("tooltip.fbp.animations.render_outline").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.animations.isRenderOutline()))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.animations.getSizeMultiplier(), FancyBlockParticles.CONFIG.animations.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.animations.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.animations.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.animations.getSizeMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get()
        );
    }

    @Override
    protected void resetConfig() {
        this.config.animations.reset();
    }
}
