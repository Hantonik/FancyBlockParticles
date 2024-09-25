package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.text.DecimalFormat;

public class FBPRainScreen extends FBPAbstractOptionsScreen {
    public FBPRainScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslatableComponent("screen.fbp.category.rain"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var formatter = new DecimalFormat("0.00");

        this.list.addBig(
                new FBPToggleButton(310, 20, new TranslatableComponent("button.fbp.rain.fancy_rain_particles"), this.config.rain::isEnabled, button -> this.config.rain.setEnabled(!this.config.rain.isEnabled()), new TranslatableComponent("tooltip.fbp.rain.fancy_rain_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isEnabled())))
        );

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.random_size"), this.config.rain::isRandomSize, button -> this.config.rain.setRandomSize(!this.config.rain.isRandomSize()), new TranslatableComponent("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.random_fading_speed"), this.config.rain::isRandomFadingSpeed, button -> this.config.rain.setRandomFadingSpeed(!this.config.rain.isRandomFadingSpeed()), new TranslatableComponent("tooltip.fbp.common.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isRandomFadingSpeed()))),

                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.render_distance").append(": "), new TranslatableComponent("tooltip.fbp.chunks"), this.config.rain.getRenderDistance(), FancyBlockParticles.CONFIG.rain.getRenderDistance(), 2, 32, 1, button -> this.config.rain.setRenderDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.render_distance").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getRenderDistance())).append(new TranslatableComponent("tooltip.fbp.chunks")).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.simulation_distance").append(": "), new TranslatableComponent("tooltip.fbp.chunks"), this.config.rain.getSimulationDistance(), FancyBlockParticles.CONFIG.rain.getSimulationDistance(), 2, 32, 1, button -> this.config.rain.setSimulationDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.simulation_distance").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getSimulationDistance())).append(new TranslatableComponent("tooltip.fbp.chunks")).withStyle(ChatFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.transparency").append(": "), new TextComponent("%"), Math.round(this.config.rain.getTransparency() * 100.0D * 100.0D) / 100.0D, Math.round(FancyBlockParticles.CONFIG.rain.getTransparency() * 100.0D * 100.0D) / 100.0D, 0, 100, 1, button -> this.config.rain.setTransparency(button.getValueFloat() / 100), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.transparency").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(new DecimalFormat("0").format(Math.round(FBPConfig.DEFAULT_CONFIG.rain.getTransparency() * 100.0D * 100.0D) / 100.0D)).append(new TextComponent("%")).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.rain.particle_density").append(": "), new TextComponent("x"), this.config.rain.getParticleDensity(), FancyBlockParticles.CONFIG.rain.getParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.rain.setParticleDensity(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.rain.particle_density").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.rain.getParticleDensity())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.size_multiplier").append(": "), new TextComponent("x"), this.config.rain.getSizeMultiplier(), FancyBlockParticles.CONFIG.rain.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.rain.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.rain.getSizeMultiplier())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslatableComponent("button.fbp.common.gravity_multiplier").append(": "), new TextComponent("x"), this.config.rain.getGravityMultiplier(), FancyBlockParticles.CONFIG.rain.getGravityMultiplier(), 0.1D, 2.0D, 0.05D, button -> this.config.rain.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslatableComponent("tooltip.fbp.common.gravity_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.rain.getGravityMultiplier())).append(new TextComponent("x")).withStyle(ChatFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.rain.reset();
    }
}
