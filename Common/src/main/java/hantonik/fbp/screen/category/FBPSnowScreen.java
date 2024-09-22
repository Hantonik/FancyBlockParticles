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

public class FBPSnowScreen extends FBPAbstractOptionsScreen {
    public FBPSnowScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslatableComponent("screen.fbp.category.snow"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var formatter = new DecimalFormat("0.00");

        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.min_lifetime").append(": "), new TranslatableComponent("button.fbp.common.ticks"), this.config.snow.getMinLifetime(), FancyBlockParticles.CONFIG.snow.getMinLifetime(), 0, 500, 1, button -> {
            this.config.snow.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.snow.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.snow.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getMinLifetime())).append(new TranslatableComponent("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.max_lifetime").append(": "), new TranslatableComponent("button.fbp.common.ticks"), this.config.snow.getMaxLifetime(), FancyBlockParticles.CONFIG.snow.getMaxLifetime(), 0, 500, 1, button -> {
            this.config.snow.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.snow.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.snow.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getMaxLifetime())).append(new TranslatableComponent("button.fbp.common.ticks")).withStyle(ChatFormatting.YELLOW))));

        this.list.addBig(
                new FBPToggleButton(310, 20, new TranslatableComponent("button.fbp.snow.fancy_snow_particles"), this.config.snow::isEnabled, button -> this.config.snow.setEnabled(!this.config.snow.isEnabled()), new TranslatableComponent("tooltip.fbp.snow.fancy_snow_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isEnabled())))
        );

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.low_traction"), this.config.snow::isLowTraction, button -> this.config.snow.setLowTraction(!this.config.snow.isLowTraction()), new TranslatableComponent("tooltip.fbp.common.low_traction").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isLowTraction()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.rest_on_floor"), this.config.snow::isRestOnFloor, button -> this.config.snow.setRestOnFloor(!this.config.snow.isRestOnFloor()), new TranslatableComponent("tooltip.fbp.common.rest_on_floor").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRestOnFloor()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.bounce_off_walls"), this.config.snow::isBounceOffWalls, button -> this.config.snow.setBounceOffWalls(!this.config.snow.isBounceOffWalls()), new TranslatableComponent("tooltip.fbp.common.bounce_off_walls").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isBounceOffWalls()))),
                waterPhysicsButton,

                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.particles_decay"), () -> !this.config.snow.isInfiniteDuration(), button -> this.config.snow.setInfiniteDuration(!this.config.snow.isInfiniteDuration()), new TranslatableComponent("tooltip.fbp.common.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.snow.isInfiniteDuration()))),

                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.random_size"), this.config.snow::isRandomSize, button -> this.config.snow.setRandomSize(!this.config.snow.isRandomSize()), new TranslatableComponent("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.random_rotation"), this.config.snow::isRandomRotation, button -> this.config.snow.setRandomRotation(!this.config.snow.isRandomRotation()), new TranslatableComponent("tooltip.fbp.common.random_rotation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRandomRotation()))),

                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.particles_decay"), () -> !this.config.snow.isInfiniteDuration(), button -> this.config.snow.setInfiniteDuration(!this.config.snow.isInfiniteDuration()), new TranslatableComponent("tooltip.fbp.common.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.snow.isInfiniteDuration()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.random_fading_speed"), this.config.snow::isRandomFadingSpeed, button -> this.config.snow.setRandomFadingSpeed(!this.config.snow.isRandomFadingSpeed()), new TranslatableComponent("tooltip.fbp.common.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRandomFadingSpeed())), () -> (this.config.snow.isWaterPhysics() || (!this.config.snow.isInfiniteDuration() && !this.config.global.isInfiniteDuration())) && !FancyBlockParticles.CONFIG.global.isLocked()),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),

                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.render_distance").append(": "), new TranslatableComponent("tooltip.fbp.chunks"), this.config.snow.getRenderDistance(), FancyBlockParticles.CONFIG.snow.getRenderDistance(), 2, 32, 1, button -> this.config.snow.setRenderDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.render_distance").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getRenderDistance())).append(new TranslatableComponent("tooltip.fbp.chunks")).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.simulation_distance").append(": "), new TranslatableComponent("tooltip.fbp.chunks"), this.config.snow.getSimulationDistance(), FancyBlockParticles.CONFIG.snow.getSimulationDistance(), 2, 32, 1, button -> this.config.snow.setSimulationDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.simulation_distance").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getSimulationDistance())).append(new TranslatableComponent("tooltip.fbp.chunks")).withStyle(ChatFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.snow.particle_density").append(": "), new TextComponent("x"), this.config.snow.getParticleDensity(), FancyBlockParticles.CONFIG.snow.getParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.snow.setParticleDensity(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.snow.particle_density").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.snow.getParticleDensity())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.size_multiplier").append(": "), new TextComponent("x"), this.config.snow.getSizeMultiplier(), FancyBlockParticles.CONFIG.snow.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.snow.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.snow.getSizeMultiplier())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.rotation_multiplier").append(": "), new TextComponent("x"), this.config.snow.getRotationMultiplier(), FancyBlockParticles.CONFIG.snow.getRotationMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.snow.setRotationMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.rotation_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.snow.getRotationMultiplier())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.gravity_multiplier").append(": "), new TextComponent("x"), this.config.snow.getGravityMultiplier(), FancyBlockParticles.CONFIG.snow.getGravityMultiplier(), 0.1D, 2.0D, 0.05D, button -> this.config.snow.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.gravity_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.snow.getGravityMultiplier())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.snow.reset();
    }
}
