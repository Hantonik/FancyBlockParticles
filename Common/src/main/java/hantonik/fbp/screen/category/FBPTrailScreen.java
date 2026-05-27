package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
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

public class FBPTrailScreen extends FBPAbstractOptionsScreen {
    public FBPTrailScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.trail"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.min_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.trail.getMinLifetime(), FancyBlockParticles.CONFIG.trail.getMinLifetime(), 0, 100, 1, button -> {
            this.config.trail.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.trail.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.trail.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.trail.getMinLifetime())).append(Component.translatable("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW)))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.max_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.trail.getMaxLifetime(), FancyBlockParticles.CONFIG.trail.getMaxLifetime(), 0, 100, 1, button -> {
            this.config.trail.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.trail.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.trail.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.trail.getMaxLifetime())).append(Component.translatable("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW)))));

        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.trail.fancy_trail_particles"), this.config.trail::isEnabled, _ -> this.config.trail.setEnabled(!this.config.trail.isEnabled()), Tooltip.create(Component.translatable("tooltip.fbp.trail.fancy_trail_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.trail.isEnabled())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.spawn_while_frozen"), this.config.trail::isSpawnWhileFrozen, _ -> this.config.trail.setSpawnWhileFrozen(!this.config.trail.isSpawnWhileFrozen()), Tooltip.create(Component.translatable("tooltip.fbp.common.spawn_while_frozen").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.trail.isSpawnWhileFrozen())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.particles_decay"), () -> !this.config.trail.isInfiniteDuration(), _ -> this.config.trail.setInfiniteDuration(!this.config.trail.isInfiniteDuration()), Tooltip.create(Component.translatable("tooltip.fbp.common.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.trail.isInfiniteDuration())))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_size"), this.config.trail::isRandomSize, _ -> this.config.trail.setRandomSize(!this.config.trail.isRandomSize()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.trail.isRandomSize())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_fading_speed"), this.config.trail::isRandomFadingSpeed, _ -> this.config.trail.setRandomFadingSpeed(!this.config.trail.isRandomFadingSpeed()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.trail.isRandomFadingSpeed()))), () -> (!this.config.trail.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked()),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.trail.getSizeMultiplier(), FancyBlockParticles.CONFIG.trail.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.trail.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.trail.getSizeMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW)))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get()
        );
    }

    @Override
    protected void resetConfig() {
        this.config.trail.reset();
    }
}
