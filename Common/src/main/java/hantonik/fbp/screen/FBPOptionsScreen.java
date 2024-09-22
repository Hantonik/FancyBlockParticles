package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.platform.Services;
import hantonik.fbp.screen.category.*;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FBPOptionsScreen extends FBPAbstractOptionsScreen {
    public static final WidgetSprites LOGO_SPRITES = new WidgetSprites(ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "logo"), ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "logo_highlighted"));
    public static final WidgetSprites REPORT_SPRITES = new WidgetSprites(ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "report"), ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "report_highlighted"));

    public FBPOptionsScreen(Screen lastScreen) {
        super(Component.translatable("screen.fbp.settings"), lastScreen, FancyBlockParticles.CONFIG.copy());
    }

    @Override
    protected void initOptions() {
        this.list.addBig(new FBPToggleButton(310, 20, Component.translatable("button.fbp.global.enabled"), this.config.global::isEnabled, button -> this.config.global.setEnabled(!this.config.global.isEnabled()), Tooltip.create(Component.translatable("tooltip.fbp.global.enabled").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isEnabled())))));
        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.global.freeze_effect"), this.config.global::isFreezeEffect, button -> this.config.global.setFreezeEffect(!this.config.global.isFreezeEffect()), Tooltip.create(Component.translatable("tooltip.fbp.global.freeze_effect").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isFreezeEffect())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.particles_decay"), () -> !this.config.global.isInfiniteDuration(), button -> this.config.global.setInfiniteDuration(!this.config.global.isInfiniteDuration()), Tooltip.create(Component.translatable("tooltip.fbp.common.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.global.isInfiniteDuration())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.global.cartoon_mode"), this.config.global::isCartoonMode, button -> this.config.global.setCartoonMode(!this.config.global.isCartoonMode()), Tooltip.create(Component.translatable("tooltip.fbp.global.cartoon_mode").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isCartoonMode())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.global.cull_particles"), this.config.global::isCullParticles, button -> this.config.global.setCullParticles(!this.config.global.isCullParticles()), Tooltip.create(Component.translatable("tooltip.fbp.global.cull_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isCullParticles()))))
        );

        this.list.addBig(
                new StringWidget(310, 20, Component.translatable("widget.fbp.global.categories"), this.font)
        );

        var animationsScreenButton = this.openScreenButton(Component.translatable("screen.fbp.category.animations").append("..."), () -> new FBPAnimationsScreen(this, this.config));

        if (Services.PLATFORM.isModLoaded("a_good_place")) {
            animationsScreenButton.active = false;
            animationsScreenButton.setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.mod_incompatibility", Component.literal("A Good Place").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW)));
        } else if (Services.PLATFORM.isModLoaded("optifine"))
            animationsScreenButton.setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.mod_incompatibility", Component.literal("OptiFine").withStyle(ChatFormatting.AQUA))));


        this.list.addSmall(
                this.openScreenButton(Component.translatable("screen.fbp.category.terrain").append("..."), () -> new FBPTerrainScreen(this, this.config)),
                this.openScreenButton(Component.translatable("screen.fbp.category.drip").append("..."), () -> new FBPDripScreen(this, this.config)),
                this.openScreenButton(Component.translatable("screen.fbp.category.rain").append("..."), () -> new FBPRainScreen(this, this.config)),
                this.openScreenButton(Component.translatable("screen.fbp.category.snow").append("..."), () -> new FBPSnowScreen(this, this.config)),
                this.openScreenButton(Component.translatable("screen.fbp.category.flame").append("..."), () -> new FBPFlameScreen(this, this.config)),
                this.openScreenButton(Component.translatable("screen.fbp.category.smoke").append("..."), () -> new FBPSmokeScreen(this, this.config)),
                animationsScreenButton,
                this.openScreenButton(Component.translatable("screen.fbp.category.overlay").append("..."), () -> new FBPOverlayScreen(this, this.config))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.reset();
    }
}
