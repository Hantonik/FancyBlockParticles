package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.category.*;
import hantonik.fbp.screen.component.widget.FBPStringWidget;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;

public class FBPOptionsScreen extends FBPAbstractOptionsScreen {
    public static final ResourceLocation LOGO_TEXTURE = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/logo.png");
    public static final ResourceLocation REPORT_TEXTURE = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/report.png");

    public FBPOptionsScreen(Screen lastScreen) {
        super(new TranslationTextComponent("screen.fbp.settings"), lastScreen, FancyBlockParticles.CONFIG.copy());
    }

    @Override
    protected void initOptions() {
        this.list.addBig(new FBPToggleButton(310, 20, new TranslationTextComponent("button.fbp.global.enabled"), this.config.global::isEnabled, button -> this.config.global.setEnabled(!this.config.global.isEnabled()), new TranslationTextComponent("tooltip.fbp.global.enabled").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isEnabled()))));
        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.global.freeze_effect"), this.config.global::isFreezeEffect, button -> this.config.global.setFreezeEffect(!this.config.global.isFreezeEffect()), new TranslationTextComponent("tooltip.fbp.global.freeze_effect").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isFreezeEffect()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.particles_decay"), () -> !this.config.global.isInfiniteDuration(), button -> this.config.global.setInfiniteDuration(!this.config.global.isInfiniteDuration()), new TranslationTextComponent("tooltip.fbp.common.particles_decay").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.global.isInfiniteDuration()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.global.cartoon_mode"), this.config.global::isCartoonMode, button -> this.config.global.setCartoonMode(!this.config.global.isCartoonMode()), new TranslationTextComponent("tooltip.fbp.global.cartoon_mode").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isCartoonMode()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.global.cull_particles"), this.config.global::isCullParticles, button -> this.config.global.setCullParticles(!this.config.global.isCullParticles()), new TranslationTextComponent("tooltip.fbp.global.cull_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.global.isCullParticles())))
        );

        this.list.addBig(
                new FBPStringWidget(310, 20, new TranslationTextComponent("widget.fbp.global.categories"), this.font)
        );

        Button animationsScreenButton;

        if (ModList.get().isLoaded("a_good_place")) {
            animationsScreenButton = this.openScreenButton(new TranslationTextComponent("screen.fbp.category.animations").append("..."), () -> new FBPAnimationsScreen(this, this.config), new TranslationTextComponent("tooltip.fbp.common.mod_incompatibility", new StringTextComponent("A Good Place").withStyle(TextFormatting.AQUA)).withStyle(TextFormatting.YELLOW));
            animationsScreenButton.active = false;
        } else if (ModList.get().isLoaded("optifine"))
            animationsScreenButton = this.openScreenButton(new TranslationTextComponent("screen.fbp.category.animations").append("..."), () -> new FBPAnimationsScreen(this, this.config), new TranslationTextComponent("tooltip.fbp.common.mod_incompatibility", new StringTextComponent("OptiFine").withStyle(TextFormatting.AQUA)));
        else
            animationsScreenButton = this.openScreenButton(new TranslationTextComponent("screen.fbp.category.animations").append("..."), () -> new FBPAnimationsScreen(this, this.config));

        this.list.addSmall(
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.terrain").append("..."), () -> new FBPTerrainScreen(this, this.config)),
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.drip").append("..."), () -> new FBPDripScreen(this, this.config)),
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.rain").append("..."), () -> new FBPRainScreen(this, this.config)),
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.snow").append("..."), () -> new FBPSnowScreen(this, this.config)),
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.flame").append("..."), () -> new FBPFlameScreen(this, this.config)),
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.smoke").append("..."), () -> new FBPSmokeScreen(this, this.config)),
                animationsScreenButton,
                this.openScreenButton(new TranslationTextComponent("screen.fbp.category.overlay").append("..."), () -> new FBPOverlayScreen(this, this.config))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.reset();
    }
}
