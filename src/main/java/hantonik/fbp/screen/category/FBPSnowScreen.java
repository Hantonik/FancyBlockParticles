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

public class FBPSnowScreen extends FBPAbstractOptionsScreen {
    public FBPSnowScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.snow"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        DelayedSupplier<FBPSliderButton> minLifetimeSlider = new DelayedSupplier<>();
        DelayedSupplier<FBPSliderButton> maxLifetimeSlider = new DelayedSupplier<>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.min_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.snow.getMinLifetime(), FancyBlockParticles.CONFIG.snow.getMinLifetime(), 0, 500, 1, button -> {
            this.config.snow.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.snow.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.snow.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.min_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getMinLifetime())).withStyle(TextFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.max_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.snow.getMaxLifetime(), FancyBlockParticles.CONFIG.snow.getMaxLifetime(), 0, 500, 1, button -> {
            this.config.snow.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.snow.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.snow.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.max_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getMaxLifetime())).withStyle(TextFormatting.YELLOW))));

        FBPToggleButton waterPhysicsButton = new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.water_physics"), this.config.snow::isWaterPhysics, button -> this.config.snow.setWaterPhysics(!this.config.snow.isWaterPhysics()), /*this.tooltip(new TranslationTextComponent("tooltip.fbp.common.water_physics").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isWaterPhysics())))*/ new TranslationTextComponent("tooltip.fbp.common.option_unsupported"), () -> false); // TODO

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.low_traction"), this.config.snow::isLowTraction, button -> this.config.snow.setLowTraction(!this.config.snow.isLowTraction()), new TranslationTextComponent("tooltip.fbp.common.low_traction").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isLowTraction()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.rest_on_floor"), this.config.snow::isRestOnFloor, button -> this.config.snow.setRestOnFloor(!this.config.snow.isRestOnFloor()), new TranslationTextComponent("tooltip.fbp.common.rest_on_floor").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRestOnFloor()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.bounce_off_walls"), this.config.snow::isBounceOffWalls, button -> this.config.snow.setBounceOffWalls(!this.config.snow.isBounceOffWalls()), new TranslationTextComponent("tooltip.fbp.common.bounce_off_walls").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isBounceOffWalls()))),
                waterPhysicsButton,

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.particles_decay"), () -> !this.config.snow.isInfiniteDuration(), button -> this.config.snow.setInfiniteDuration(!this.config.snow.isInfiniteDuration()), new TranslationTextComponent("tooltip.fbp.common.particles_decay").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.snow.isInfiniteDuration()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_size"), this.config.snow::isRandomSize, button -> this.config.snow.setRandomSize(!this.config.snow.isRandomSize()), new TranslationTextComponent("tooltip.fbp.common.random_size").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_rotation"), this.config.snow::isRandomRotation, button -> this.config.snow.setRandomRotation(!this.config.snow.isRandomRotation()), new TranslationTextComponent("tooltip.fbp.common.random_rotation").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRandomRotation()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_fading_speed"), this.config.snow::isRandomFadingSpeed, button -> this.config.snow.setRandomFadingSpeed(!this.config.snow.isRandomFadingSpeed()), new TranslationTextComponent("tooltip.fbp.common.random_fading_speed").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isRandomFadingSpeed())), () -> (!this.config.snow.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked()),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.render_distance").append(": "), new TranslationTextComponent("tooltip.fbp.chunks"), this.config.snow.getRenderDistance(), FancyBlockParticles.CONFIG.snow.getRenderDistance(), 2, 32, 1, button -> this.config.snow.setRenderDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.render_distance").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getRenderDistance())).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.simulation_distance").append(": "), new TranslationTextComponent("tooltip.fbp.chunks"), this.config.snow.getSimulationDistance(), FancyBlockParticles.CONFIG.snow.getSimulationDistance(), 2, 32, 1, button -> this.config.snow.setSimulationDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.simulation_distance").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getSimulationDistance())).withStyle(TextFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.snow.particle_density").append(": "), new StringTextComponent("x"), this.config.snow.getParticleDensity(), FancyBlockParticles.CONFIG.snow.getParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.snow.setParticleDensity(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.snow.particle_density").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getParticleDensity())).withStyle(TextFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.snow.getSizeMultiplier(), FancyBlockParticles.CONFIG.snow.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.snow.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getSizeMultiplier())).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.rotation_multiplier").append(": "), new StringTextComponent("x"), this.config.snow.getRotationMultiplier(), FancyBlockParticles.CONFIG.snow.getRotationMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.snow.setRotationMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.rotation_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getRotationMultiplier())).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.gravity_multiplier").append(": "), new StringTextComponent("x"), this.config.snow.getGravityMultiplier(), FancyBlockParticles.CONFIG.snow.getGravityMultiplier(), 0.1D, 2.0D, 0.05D, button -> this.config.snow.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.gravity_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.snow.getGravityMultiplier())).withStyle(TextFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.snow.reset();
    }
}
