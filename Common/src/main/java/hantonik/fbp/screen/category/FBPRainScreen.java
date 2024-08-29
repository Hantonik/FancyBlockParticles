package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FBPRainScreen extends FBPAbstractOptionsScreen {
    public FBPRainScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.rain"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var waterPhysicsButton = new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.water_physics"), this.config.rain::isWaterPhysics, button -> this.config.rain.setWaterPhysics(!this.config.rain.isWaterPhysics()), /*this.tooltip(Component.translatable("tooltip.fbp.common.water_physics").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isWaterPhysics())))*/ Component.translatable("tooltip.fbp.common.option_unsupported"), () -> false); // TODO

        this.list.addSmall(
                waterPhysicsButton,

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_size"), this.config.rain::isRandomSize, button -> this.config.rain.setRandomSize(!this.config.rain.isRandomSize()), Component.translatable("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isRandomSize()))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_fading_speed"), this.config.rain::isRandomFadingSpeed, button -> this.config.rain.setRandomFadingSpeed(!this.config.rain.isRandomFadingSpeed()), Component.translatable("tooltip.fbp.common.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isRandomFadingSpeed()))),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.render_distance").append(": "), Component.translatable("tooltip.fbp.chunks"), this.config.rain.getRenderDistance(), FancyBlockParticles.CONFIG.rain.getRenderDistance(), 2, 32, 1, button -> this.config.rain.setRenderDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Component.translatable("tooltip.fbp.common.render_distance").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getRenderDistance())).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.simulation_distance").append(": "), Component.translatable("tooltip.fbp.chunks"), this.config.rain.getSimulationDistance(), FancyBlockParticles.CONFIG.rain.getSimulationDistance(), 2, 32, 1, button -> this.config.rain.setSimulationDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Component.translatable("tooltip.fbp.common.simulation_distance").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getSimulationDistance())).withStyle(ChatFormatting.YELLOW))),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.rain.particle_density").append(": "), Component.literal("x"), this.config.rain.getParticleDensity(), FancyBlockParticles.CONFIG.rain.getParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.rain.setParticleDensity(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Component.translatable("tooltip.fbp.rain.particle_density").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getParticleDensity())).withStyle(ChatFormatting.YELLOW))),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.rain.getSizeMultiplier(), FancyBlockParticles.CONFIG.rain.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.rain.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Component.translatable("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getSizeMultiplier())).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.gravity_multiplier").append(": "), Component.literal("x"), this.config.rain.getGravityMultiplier(), FancyBlockParticles.CONFIG.rain.getGravityMultiplier(), 0.1D, 2.0D, 0.05D, button -> this.config.rain.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Component.translatable("tooltip.fbp.common.gravity_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getGravityMultiplier())).withStyle(ChatFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.rain.reset();
    }
}
