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

import java.text.DecimalFormat;

public class FBPDripScreen extends FBPAbstractOptionsScreen {
    public FBPDripScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.drip"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        DecimalFormat formatter = new DecimalFormat("0.00");

        DelayedSupplier<FBPSliderButton> minLifetimeSlider = new DelayedSupplier<>();
        DelayedSupplier<FBPSliderButton> maxLifetimeSlider = new DelayedSupplier<>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.min_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.drip.getMinLifetime(), FancyBlockParticles.CONFIG.drip.getMinLifetime(), 0, 200, 1, button -> {
            this.config.drip.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.drip.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.min_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.drip.getMinLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.max_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.drip.getMaxLifetime(), FancyBlockParticles.CONFIG.drip.getMaxLifetime(), 0, 200, 1, button -> {
            this.config.drip.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.drip.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.max_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.drip.getMaxLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));


        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.drip.fancy_dripping_particles"), this.config.drip::isEnabled, button -> this.config.drip.setEnabled(!this.config.drip.isEnabled()), new TranslationTextComponent("tooltip.fbp.drip.fancy_dripping_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isEnabled()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.spawn_while_frozen"), this.config.drip::isSpawnWhileFrozen, button -> this.config.drip.setSpawnWhileFrozen(!this.config.drip.isSpawnWhileFrozen()), new TranslationTextComponent("tooltip.fbp.common.spawn_while_frozen").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isSpawnWhileFrozen()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_size"), this.config.drip::isRandomSize, button -> this.config.drip.setRandomSize(!this.config.drip.isRandomSize()), new TranslationTextComponent("tooltip.fbp.common.random_size").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_fading_speed"), this.config.drip::isRandomFadingSpeed, button -> this.config.drip.setRandomFadingSpeed(!this.config.drip.isRandomFadingSpeed()), new TranslationTextComponent("tooltip.fbp.common.random_fading_speed").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isRandomFadingSpeed()))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.drip.getSizeMultiplier(), FancyBlockParticles.CONFIG.drip.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.drip.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.drip.getSizeMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.gravity_multiplier").append(": "), new StringTextComponent("x"), this.config.drip.getGravityMultiplier(), FancyBlockParticles.CONFIG.drip.getGravityMultiplier(), 0.1D, 2.0D, 0.05D, button -> this.config.drip.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.gravity_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.drip.getGravityMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.drip.reset();
    }
}
