package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.DecimalFormat;

public class FBPRainScreen extends FBPAbstractOptionsScreen {
    public FBPRainScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.rain"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        DecimalFormat formatter = new DecimalFormat("0.00");

        this.list.addBig(
                new FBPToggleButton(310, 20, new TranslationTextComponent("button.fbp.rain.fancy_rain_particles"), this.config.rain::isEnabled, button -> this.config.rain.setEnabled(!this.config.rain.isEnabled()), new TranslationTextComponent("tooltip.fbp.rain.fancy_rain_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isEnabled())))
        );

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_size"), this.config.rain::isRandomSize, button -> this.config.rain.setRandomSize(!this.config.rain.isRandomSize()), new TranslationTextComponent("tooltip.fbp.common.random_size").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_fading_speed"), this.config.rain::isRandomFadingSpeed, button -> this.config.rain.setRandomFadingSpeed(!this.config.rain.isRandomFadingSpeed()), new TranslationTextComponent("tooltip.fbp.common.random_fading_speed").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isRandomFadingSpeed()))),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.render_distance").append(": "), new TranslationTextComponent("tooltip.fbp.chunks"), this.config.rain.getRenderDistance(), FancyBlockParticles.CONFIG.rain.getRenderDistance(), 2, 32, 1, button -> this.config.rain.setRenderDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.render_distance").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getRenderDistance())).append(new TranslationTextComponent("tooltip.fbp.chunks")).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.simulation_distance").append(": "), new TranslationTextComponent("tooltip.fbp.chunks"), this.config.rain.getSimulationDistance(), FancyBlockParticles.CONFIG.rain.getSimulationDistance(), 2, 32, 1, button -> this.config.rain.setSimulationDistance(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.simulation_distance").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.rain.getSimulationDistance())).append(new TranslationTextComponent("tooltip.fbp.chunks")).withStyle(TextFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.transparency").append(": "), new StringTextComponent("%"), Math.round(this.config.rain.getTransparency() * 100.0D * 100.0D) / 100.0D, Math.round(FancyBlockParticles.CONFIG.rain.getTransparency() * 100.0D * 100.0D) / 100.0D, 0, 100, 1, button -> this.config.rain.setTransparency(button.getValueFloat() / 100), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.transparency").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(new DecimalFormat("0").format(Math.round(FBPConfig.DEFAULT_CONFIG.rain.getTransparency() * 100.0D * 100.0D) / 100.0D)).append(new StringTextComponent("%")).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.rain.particle_density").append(": "), new StringTextComponent("x"), this.config.rain.getParticleDensity(), FancyBlockParticles.CONFIG.rain.getParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.rain.setParticleDensity(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.rain.particle_density").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.rain.getParticleDensity())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.rain.getSizeMultiplier(), FancyBlockParticles.CONFIG.rain.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.rain.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.rain.getSizeMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.gravity_multiplier").append(": "), new StringTextComponent("x"), this.config.rain.getGravityMultiplier(), FancyBlockParticles.CONFIG.rain.getGravityMultiplier(), 0.1D, 2.0D, 0.05D, button -> this.config.rain.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.gravity_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.rain.getGravityMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.rain.reset();
    }
}
