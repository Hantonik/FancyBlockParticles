package hantonik.fbp.screen.category;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class FBPWeatherScreen extends FBPAbstractOptionsScreen {
    public FBPWeatherScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.weather"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.weather.fancy_rain_particles"), this.config.rain::isEnabled, button -> this.config.rain.setEnabled(!this.config.rain.isEnabled()), new TranslationTextComponent("tooltip.fbp.weather.fancy_rain_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isEnabled()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.weather.fancy_snow_particles"), this.config.snow::isEnabled, button -> this.config.snow.setEnabled(!this.config.snow.isEnabled()), new TranslationTextComponent("tooltip.fbp.weather.fancy_snow_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isEnabled()))),

                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.rain").append("..."), () -> new FBPRainScreen(this, this.config)),
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.snow").append("..."), () -> new FBPSnowScreen(this, this.config))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.rain.reset();
        this.config.snow.reset();
    }
}
