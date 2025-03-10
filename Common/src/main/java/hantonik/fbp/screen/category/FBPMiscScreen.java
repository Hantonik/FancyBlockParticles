package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

public class FBPMiscScreen extends FBPAbstractOptionsScreen {
    public FBPMiscScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.misc"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var formatter = new DecimalFormat("0.00");

        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.misc.fancy_snowball_particles"), this.config.misc::isFancySnowballParticles, button -> this.config.misc.setFancySnowballParticles(!this.config.misc.isFancySnowballParticles()), Tooltip.create(Component.translatable("tooltip.fbp.misc.fancy_snowball_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.misc.isFancySnowballParticles())))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.misc.getSnowballParticleSizeMultiplier(), FancyBlockParticles.CONFIG.misc.getSnowballParticleSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.misc.setSnowballParticleSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.misc.snowball_particle_size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(formatter.format(FBPConfig.DEFAULT_CONFIG.misc.getSnowballParticleSizeMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW)))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.misc.fancy_slime_particles"), this.config.misc::isFancySlimeParticles, button -> this.config.misc.setFancySlimeParticles(!this.config.misc.isFancySlimeParticles()), Tooltip.create(Component.translatable("tooltip.fbp.misc.fancy_slime_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.misc.isFancySlimeParticles())))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.misc.getSlimeParticleSizeMultiplier(), FancyBlockParticles.CONFIG.misc.getSlimeParticleSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.misc.setSlimeParticleSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.misc.slime_particle_size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(formatter.format(FBPConfig.DEFAULT_CONFIG.misc.getSlimeParticleSizeMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW)))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.misc.fancy_breaking_splash_potion_particles"), this.config.misc::isFancyBreakingSplashPotionParticles, button -> this.config.misc.setFancyBreakingSplashPotionParticles(!this.config.misc.isFancyBreakingSplashPotionParticles()), Tooltip.create(Component.translatable("tooltip.fbp.misc.fancy_breaking_splash_potion_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.misc.isFancyBreakingSplashPotionParticles())))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.misc.getBreakingSplashPotionParticleSizeMultiplier(), FancyBlockParticles.CONFIG.misc.getBreakingSplashPotionParticleSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.misc.setBreakingSplashPotionParticleSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.misc.breaking_splash_potion_particle_size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(formatter.format(FBPConfig.DEFAULT_CONFIG.misc.getBreakingSplashPotionParticleSizeMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW))))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.misc.reset();
    }
}
