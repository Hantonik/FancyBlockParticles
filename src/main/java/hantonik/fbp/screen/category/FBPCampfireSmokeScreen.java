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

public class FBPCampfireSmokeScreen extends FBPAbstractOptionsScreen {
    public FBPCampfireSmokeScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.campfire_smoke"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.campfire_smoke.fancy_campfire_smoke_particles"), this.config.campfireSmoke::isEnabled, button -> this.config.campfireSmoke.setEnabled(!this.config.campfireSmoke.isEnabled()), new TranslationTextComponent("tooltip.fbp.campfire_smoke.fancy_campfire_smoke_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isEnabled()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.spawn_while_frozen"), this.config.campfireSmoke::isSpawnWhileFrozen, button -> this.config.campfireSmoke.setSpawnWhileFrozen(!this.config.campfireSmoke.isSpawnWhileFrozen()), new TranslationTextComponent("tooltip.fbp.common.spawn_while_frozen").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isSpawnWhileFrozen()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_size"), this.config.campfireSmoke::isRandomSize, button -> this.config.campfireSmoke.setRandomSize(!this.config.campfireSmoke.isRandomSize()), new TranslationTextComponent("tooltip.fbp.common.random_size").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isRandomSize()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_fading_speed"), this.config.campfireSmoke::isRandomFadingSpeed, button -> this.config.campfireSmoke.setRandomFadingSpeed(!this.config.campfireSmoke.isRandomFadingSpeed()), new TranslationTextComponent("tooltip.fbp.common.random_fading_speed").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isRandomFadingSpeed())), () -> !FancyBlockParticles.CONFIG.global.isLocked()),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.transparency").append(": "), new StringTextComponent("%"), Math.round(this.config.campfireSmoke.getTransparency() * 100.0D * 100.0D) / 100.0D, Math.round(FancyBlockParticles.CONFIG.campfireSmoke.getTransparency() * 100.0D * 100.0D) / 100.0D, 0, 100, 1, button -> this.config.campfireSmoke.setTransparency(button.getValueFloat() / 100), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.transparency").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(new DecimalFormat("0").format(Math.round(FBPConfig.DEFAULT_CONFIG.campfireSmoke.getTransparency() * 100.0D * 100.0D) / 100.0D)).append(new StringTextComponent("%")).withStyle(TextFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.campfireSmoke.getSizeMultiplier(), FancyBlockParticles.CONFIG.campfireSmoke.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.campfireSmoke.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.campfireSmoke.getSizeMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.campfireSmoke.reset();
    }
}
