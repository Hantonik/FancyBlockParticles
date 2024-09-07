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

public class FBPSmokeScreen extends FBPAbstractOptionsScreen {
    public FBPSmokeScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.smoke"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        DelayedSupplier<FBPSliderButton> minLifetimeSlider = new DelayedSupplier<>();
        DelayedSupplier<FBPSliderButton> maxLifetimeSlider = new DelayedSupplier<>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.min_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.smoke.getMinLifetime(), FancyBlockParticles.CONFIG.smoke.getMinLifetime(), 0, 50, 1, button -> {
            this.config.smoke.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.smoke.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.smoke.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.min_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.smoke.getMinLifetime())).withStyle(TextFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.max_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.smoke.getMaxLifetime(), FancyBlockParticles.CONFIG.smoke.getMaxLifetime(), 0, 50, 1, button -> {
            this.config.smoke.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.smoke.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.smoke.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.max_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.smoke.getMaxLifetime())).withStyle(TextFormatting.YELLOW))));

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.smoke.fancy_smoke_particles"), this.config.smoke::isEnabled, button -> this.config.smoke.setEnabled(!this.config.smoke.isEnabled()), new TranslationTextComponent("tooltip.fbp.smoke.fancy_smoke_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.smoke.isEnabled()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.spawn_while_frozen"), this.config.smoke::isSpawnWhileFrozen, button -> this.config.smoke.setSpawnWhileFrozen(!this.config.smoke.isSpawnWhileFrozen()), new TranslationTextComponent("tooltip.fbp.common.spawn_while_frozen").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.smoke.isSpawnWhileFrozen()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.particles_decay"), () -> !this.config.smoke.isInfiniteDuration(), button -> this.config.smoke.setInfiniteDuration(!this.config.smoke.isInfiniteDuration()), new TranslationTextComponent("tooltip.fbp.common.particles_decay").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.smoke.isInfiniteDuration()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_size"), this.config.smoke::isRandomSize, button -> this.config.smoke.setRandomSize(!this.config.smoke.isRandomSize()), new TranslationTextComponent("tooltip.fbp.common.random_size").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.smoke.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_fading_speed"), this.config.smoke::isRandomFadingSpeed, button -> this.config.smoke.setRandomFadingSpeed(!this.config.smoke.isRandomFadingSpeed()), new TranslationTextComponent("tooltip.fbp.common.random_fading_speed").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.smoke.isRandomFadingSpeed())), () -> (!this.config.smoke.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked()),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.smoke.getSizeMultiplier(), FancyBlockParticles.CONFIG.smoke.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.smoke.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.smoke.getSizeMultiplier())).withStyle(TextFormatting.YELLOW))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get()
        );
    }

    @Override
    protected void resetConfig() {
        this.config.smoke.reset();
    }
}
