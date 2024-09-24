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

public class FBPCampfireSmokeScreen extends FBPAbstractOptionsScreen {
    public FBPCampfireSmokeScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.campfire_smoke"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.campfire_smoke.fancy_campfire_smoke_particles"), this.config.campfireSmoke::isEnabled, button -> this.config.campfireSmoke.setEnabled(!this.config.campfireSmoke.isEnabled()), Tooltip.create(Component.translatable("tooltip.fbp.campfire_smoke.fancy_campfire_smoke_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isEnabled())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.spawn_while_frozen"), this.config.campfireSmoke::isSpawnWhileFrozen, button -> this.config.campfireSmoke.setSpawnWhileFrozen(!this.config.campfireSmoke.isSpawnWhileFrozen()), Tooltip.create(Component.translatable("tooltip.fbp.common.spawn_while_frozen").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isSpawnWhileFrozen())))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_size"), this.config.campfireSmoke::isRandomSize, button -> this.config.campfireSmoke.setRandomSize(!this.config.campfireSmoke.isRandomSize()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isRandomSize())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_fading_speed"), this.config.campfireSmoke::isRandomFadingSpeed, button -> this.config.campfireSmoke.setRandomFadingSpeed(!this.config.campfireSmoke.isRandomFadingSpeed()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.campfireSmoke.isRandomFadingSpeed()))), () -> !FancyBlockParticles.CONFIG.global.isLocked()),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.transparency").append(": "), Component.literal("%"), Math.round(this.config.campfireSmoke.getTransparency() * 100.0D * 100.0D) / 100.0D, Math.round(FancyBlockParticles.CONFIG.campfireSmoke.getTransparency() * 100.0D * 100.0D) / 100.0D, 0, 100, 1, button -> this.config.campfireSmoke.setTransparency(button.getValueFloat() / 100), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.transparency").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(new DecimalFormat("0").format(Math.round(FBPConfig.DEFAULT_CONFIG.campfireSmoke.getTransparency() * 100.0D * 100.0D) / 100.0D)).append(Component.literal("%")).withStyle(ChatFormatting.YELLOW)))),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.campfireSmoke.getSizeMultiplier(), FancyBlockParticles.CONFIG.campfireSmoke.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.campfireSmoke.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(new DecimalFormat("0.00").format(FBPConfig.DEFAULT_CONFIG.campfireSmoke.getSizeMultiplier())).append(Component.literal("x")).withStyle(ChatFormatting.YELLOW))))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.campfireSmoke.reset();
    }
}
