package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.category.*;
import hantonik.fbp.screen.component.widget.FBPStringWidget;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class FBPOptionsScreen extends FBPAbstractOptionsScreen {
    public static final ResourceLocation LOGO_TEXTURE = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/logo.png");
    public static final ResourceLocation REPORT_TEXTURE = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/report.png");

    public FBPOptionsScreen(Screen lastScreen) {
        super(new TranslatableComponent("screen.fbp.settings"), lastScreen, FancyBlockParticles.CONFIG.copy());
    }

    @Override
    protected void initOptions() {
        this.list.addBig(new FBPToggleButton(310, 20, new TranslatableComponent("button.fbp.global.enabled"), this.config.global::isEnabled, button -> this.config.global.setEnabled(!this.config.global.isEnabled()), new TranslatableComponent("tooltip.fbp.global.enabled").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isEnabled()))));
        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.global.freeze_effect"), this.config.global::isFreezeEffect, button -> this.config.global.setFreezeEffect(!this.config.global.isFreezeEffect()), new TranslatableComponent("tooltip.fbp.global.freeze_effect").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isFreezeEffect()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.common.particles_decay"), () -> !this.config.global.isInfiniteDuration(), button -> this.config.global.setInfiniteDuration(!this.config.global.isInfiniteDuration()), new TranslatableComponent("tooltip.fbp.common.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.global.isInfiniteDuration()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.global.cartoon_mode"), this.config.global::isCartoonMode, button -> this.config.global.setCartoonMode(!this.config.global.isCartoonMode()), new TranslatableComponent("tooltip.fbp.global.cartoon_mode").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isCartoonMode()))),
                new FBPToggleButton(150, 20, new TranslatableComponent("button.fbp.global.cull_particles"), this.config.global::isCullParticles, button -> this.config.global.setCullParticles(!this.config.global.isCullParticles()), new TranslatableComponent("tooltip.fbp.global.cull_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isCullParticles())))
        );

        this.list.addBig(
                new FBPStringWidget(310, 20, new TranslatableComponent("widget.fbp.global.categories"), this.font)
        );

        var animationsButton = this.openScreenButton(new TranslatableComponent("screen.fbp.category.animations").append("..."), () -> new FBPAnimationsScreen(this, this.config), new TranslatableComponent("tooltip.fbp.common.option_unsupported"));
        animationsButton.active = false;

        this.list.addSmall(
                this.openScreenButton(new TranslatableComponent("screen.fbp.category.terrain").append("..."), () -> new FBPTerrainScreen(this, this.config)),
                this.openScreenButton(new TranslatableComponent("screen.fbp.category.weather").append("..."), () -> new FBPWeatherScreen(this, this.config)),
                this.openScreenButton(new TranslatableComponent("screen.fbp.category.flame").append("..."), () -> new FBPFlameScreen(this, this.config)),
                this.openScreenButton(new TranslatableComponent("screen.fbp.category.smoke").append("..."), () -> new FBPSmokeScreen(this, this.config)),
                animationsButton,
                this.openScreenButton(new TranslatableComponent("screen.fbp.category.overlay").append("..."), () -> new FBPOverlayScreen(this, this.config))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.reset();
    }
}
