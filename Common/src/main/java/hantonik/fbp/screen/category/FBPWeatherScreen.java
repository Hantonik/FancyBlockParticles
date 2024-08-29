package hantonik.fbp.screen.category;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FBPWeatherScreen extends FBPAbstractOptionsScreen {
    public FBPWeatherScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.weather"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.weather.fancy_rain_particles"), this.config.rain::isEnabled, button -> this.config.rain.setEnabled(!this.config.rain.isEnabled()), Component.translatable("tooltip.fbp.weather.fancy_rain_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.rain.isEnabled()))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.weather.fancy_snow_particles"), this.config.snow::isEnabled, button -> this.config.snow.setEnabled(!this.config.snow.isEnabled()), Component.translatable("tooltip.fbp.weather.fancy_snow_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.snow.isEnabled()))),

                this.openScreenButton(Component.translatable("screen.fbp.category.rain").append("..."), () -> new FBPRainScreen(this, this.config)),
                this.openScreenButton(Component.translatable("screen.fbp.category.snow").append("..."), () -> new FBPSnowScreen(this, this.config))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.rain.reset();
        this.config.snow.reset();
    }
}
