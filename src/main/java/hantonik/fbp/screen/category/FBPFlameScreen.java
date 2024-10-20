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

public class FBPFlameScreen extends FBPAbstractOptionsScreen {
    public FBPFlameScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.flame"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        DelayedSupplier<FBPSliderButton> minLifetimeSlider = new DelayedSupplier<>();
        DelayedSupplier<FBPSliderButton> maxLifetimeSlider = new DelayedSupplier<>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.min_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.flame.getMinLifetime(), FancyBlockParticles.CONFIG.flame.getMinLifetime(), 0, 50, 1, button -> {
            this.config.flame.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.flame.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.flame.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.min_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.flame.getMinLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.max_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.flame.getMaxLifetime(), FancyBlockParticles.CONFIG.flame.getMaxLifetime(), 0, 50, 1, button -> {
            this.config.flame.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.flame.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.flame.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.max_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.flame.getMaxLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.flame.fancy_flame_particles"), this.config.flame::isEnabled, button -> this.config.flame.setEnabled(!this.config.flame.isEnabled()), new TranslationTextComponent("tooltip.fbp.flame.fancy_flame_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.flame.isEnabled()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.spawn_while_frozen"), this.config.flame::isSpawnWhileFrozen, button -> this.config.flame.setSpawnWhileFrozen(!this.config.flame.isSpawnWhileFrozen()), new TranslationTextComponent("tooltip.fbp.common.spawn_while_frozen").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.flame.isSpawnWhileFrozen()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.particles_decay"), () -> !this.config.flame.isInfiniteDuration(), button -> this.config.flame.setInfiniteDuration(!this.config.flame.isInfiniteDuration()), new TranslationTextComponent("tooltip.fbp.common.particles_decay").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.flame.isInfiniteDuration()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_size"), this.config.flame::isRandomSize, button -> this.config.flame.setRandomSize(!this.config.flame.isRandomSize()), new TranslationTextComponent("tooltip.fbp.common.random_size").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.flame.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_fading_speed"), this.config.flame::isRandomFadingSpeed, button -> this.config.flame.setRandomFadingSpeed(!this.config.flame.isRandomFadingSpeed()), new TranslationTextComponent("tooltip.fbp.common.random_fading_speed").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.flame.isRandomFadingSpeed())), () -> (!this.config.flame.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked()),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.flame.getSizeMultiplier(), FancyBlockParticles.CONFIG.flame.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.flame.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.flame.getSizeMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get()
        );
    }

    @Override
    protected void resetConfig() {
        this.config.flame.reset();
    }
}
