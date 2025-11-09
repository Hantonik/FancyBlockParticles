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

public class FBPFireflyScreen extends FBPAbstractOptionsScreen {
    public FBPFireflyScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.firefly"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.min_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.firefly.getMinLifetime(), FancyBlockParticles.CONFIG.firefly.getMinLifetime(), 0, 600, 1, button -> {
            this.config.firefly.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.firefly.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.firefly.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.firefly.getMinLifetime())).append(Component.translatable("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW)))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.max_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.firefly.getMaxLifetime(), FancyBlockParticles.CONFIG.firefly.getMaxLifetime(), 0, 600, 1, button -> {
            this.config.firefly.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.firefly.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.firefly.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.firefly.getMaxLifetime())).append(Component.translatable("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW)))));

        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.firefly.fancy_firefly_particles"), this.config.firefly::isEnabled, button -> this.config.firefly.setEnabled(!this.config.firefly.isEnabled()), Tooltip.create(Component.translatable("tooltip.fbp.firefly.fancy_firefly_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.firefly.isEnabled())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.spawn_while_frozen"), this.config.firefly::isSpawnWhileFrozen, button -> this.config.firefly.setSpawnWhileFrozen(!this.config.firefly.isSpawnWhileFrozen()), Tooltip.create(Component.translatable("tooltip.fbp.common.spawn_while_frozen").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.firefly.isSpawnWhileFrozen())))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_size"), this.config.firefly::isRandomSize, button -> this.config.firefly.setRandomSize(!this.config.firefly.isRandomSize()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.firefly.isRandomSize())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.firefly.random_dimming_speed"), this.config.firefly::isRandomDimmingSpeed, button -> this.config.firefly.setRandomDimmingSpeed(!this.config.firefly.isRandomDimmingSpeed()), Tooltip.create(Component.translatable("tooltip.fbp.firefly.random_dimming_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.firefly.isRandomDimmingSpeed()))), () -> (!this.config.firefly.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked()),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.particles_decay"), () -> !this.config.firefly.isInfiniteDuration(), button -> this.config.firefly.setInfiniteDuration(!this.config.firefly.isInfiniteDuration()), Tooltip.create(Component.translatable("tooltip.fbp.common.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.firefly.isInfiniteDuration())))),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.transparency").append(": "), Component.literal("%"), Math.round(this.config.firefly.getTransparency() * 100.0D * 100.0D) / 100.0D, Math.round(FancyBlockParticles.CONFIG.firefly.getTransparency() * 100.0D * 100.0D) / 100.0D, 0, 100, 1, button -> this.config.firefly.setTransparency(button.getValueFloat() / 100), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.transparency").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(new DecimalFormat("0").format(Math.round(FBPConfig.DEFAULT_CONFIG.firefly.getTransparency() * 100.0D * 100.0D) / 100.0D)).append(Component.literal("%")).withStyle(ChatFormatting.YELLOW)))),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.speed_multiplier").append(": "), Component.literal("x"), this.config.firefly.getSpeedMultiplier(), FancyBlockParticles.CONFIG.firefly.getSpeedMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.firefly.setSpeedMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.speed_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.firefly.getSpeedMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW)))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.firefly.getSizeMultiplier(), FancyBlockParticles.CONFIG.firefly.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.firefly.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.firefly.getSizeMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW)))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get()
        );
    }

    @Override
    protected void resetConfig() {
        this.config.firefly.reset();
    }
}
