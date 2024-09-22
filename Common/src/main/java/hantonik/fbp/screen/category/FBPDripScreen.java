package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.text.DecimalFormat;

public class FBPDripScreen extends FBPAbstractOptionsScreen {
    public FBPDripScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslatableComponent("screen.fbp.category.drip"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var formatter = new DecimalFormat("0.00");

        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.min_lifetime").append(": "), new TranslatableComponent("button.fbp.common.ticks"), this.config.drip.getMinLifetime(), FancyBlockParticles.CONFIG.drip.getMinLifetime(), 0, 200, 1, button -> {
            this.config.drip.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.drip.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.drip.getMinLifetime())).append(new TranslatableComponent("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.max_lifetime").append(": "), new TranslatableComponent("button.fbp.common.ticks"), this.config.drip.getMaxLifetime(), FancyBlockParticles.CONFIG.drip.getMaxLifetime(), 0, 200, 1, button -> {
            this.config.drip.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.drip.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.drip.getMaxLifetime())).append(new TranslatableComponent("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW))));


        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.drip.fancy_dripping_particles"), this.config.drip::isEnabled, button -> this.config.drip.setEnabled(!this.config.drip.isEnabled()), new TranslatableComponent("tooltip.fbp.drip.fancy_dripping_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isEnabled()))),

                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.spawn_while_frozen"), this.config.drip::isSpawnWhileFrozen, button -> this.config.drip.setSpawnWhileFrozen(!this.config.drip.isSpawnWhileFrozen()), new TranslatableComponent("tooltip.fbp.common.spawn_while_frozen").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isSpawnWhileFrozen()))),

                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.random_size"), this.config.drip::isRandomSize, button -> this.config.drip.setRandomSize(!this.config.drip.isRandomSize()), new TranslatableComponent("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.random_fading_speed"), this.config.drip::isRandomFadingSpeed, button -> this.config.drip.setRandomFadingSpeed(!this.config.drip.isRandomFadingSpeed()), new TranslatableComponent("tooltip.fbp.common.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.drip.isRandomFadingSpeed()))),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),

                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.size_multiplier").append(": "), new TextComponent("x"), this.config.drip.getSizeMultiplier(), FancyBlockParticles.CONFIG.drip.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.drip.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.drip.getSizeMultiplier())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.gravity_multiplier").append(": "), new TextComponent("x"), this.config.drip.getGravityMultiplier(), FancyBlockParticles.CONFIG.drip.getGravityMultiplier(), 0.1D, 2.0D, 0.05D, button -> this.config.drip.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.gravity_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.drip.getGravityMultiplier())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.drip.reset();
    }
}
