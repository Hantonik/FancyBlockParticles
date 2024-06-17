package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FBPTerrainScreen extends FBPAbstractOptionsScreen {
    public FBPTerrainScreen(Screen lastScreen, FBPConfig config) {
        super(Component.translatable("screen.fbp.category.terrain"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.min_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.terrain.getMinLifetime(), FancyBlockParticles.CONFIG.terrain.getMinLifetime(), 0, 100, 1, button -> {
            this.config.terrain.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.terrain.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.terrain.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getMinLifetime())).withStyle(ChatFormatting.YELLOW)))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.max_lifetime").append(": "), Component.translatable("button.fbp.common.ticks"), this.config.terrain.getMaxLifetime(), FancyBlockParticles.CONFIG.terrain.getMaxLifetime(), 0, 100, 1, button -> {
            this.config.terrain.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.terrain.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.terrain.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getMaxLifetime())).withStyle(ChatFormatting.YELLOW)))));

        var particleBlacklistButton = this.openScreenButton(Component.translatable("screen.fbp.terrain.particle_blacklist").append("..."), () -> null /*new FBPParticleBlacklistScreen(this, this.config)*/); // TODO
        particleBlacklistButton.setWidth(310);
        particleBlacklistButton.setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.option_unsupported")));
        particleBlacklistButton.active = false;

        this.list.addBig(
                new FBPToggleButton(310, 20, Component.translatable("button.fbp.terrain.fancy_breaking_particles"), this.config.terrain::isFancyBreakingParticles, button -> this.config.terrain.setFancyBreakingParticles(!this.config.terrain.isFancyBreakingParticles()), Tooltip.create(Component.translatable("tooltip.fbp.terrain.fancy_breaking_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isFancyBreakingParticles()))))
        );

        this.list.addSmall(
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.terrain.fancy_cracking_particles"), this.config.terrain::isFancyCrackingParticles, button -> this.config.terrain.setFancyCrackingParticles(!this.config.terrain.isFancyCrackingParticles()), Tooltip.create(Component.translatable("tooltip.fbp.terrain.fancy_cracking_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isFancyCrackingParticles())))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.spawn_while_frozen"), this.config.terrain::isSpawnWhileFrozen, button -> this.config.terrain.setSpawnWhileFrozen(!this.config.terrain.isSpawnWhileFrozen()), Tooltip.create(Component.translatable("tooltip.fbp.common.spawn_while_frozen").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isSpawnWhileFrozen())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.particles_decay"), () -> !this.config.terrain.isInfiniteDuration(), button -> this.config.terrain.setInfiniteDuration(!this.config.terrain.isInfiniteDuration()), Tooltip.create(Component.translatable("tooltip.fbp.common.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.terrain.isInfiniteDuration())))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.terrain.smart_breaking"), this.config.terrain::isSmartBreaking, button -> this.config.terrain.setSmartBreaking(!this.config.terrain.isSmartBreaking()), Tooltip.create(Component.translatable("tooltip.fbp.terrain.smart_breaking").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isSmartBreaking())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.low_traction"), this.config.terrain::isLowTraction, button -> this.config.terrain.setLowTraction(!this.config.terrain.isLowTraction()), Tooltip.create(Component.translatable("tooltip.fbp.common.low_traction").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isLowTraction())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.rest_on_floor"), this.config.terrain::isRestOnFloor, button -> this.config.terrain.setRestOnFloor(!this.config.terrain.isRestOnFloor()), Tooltip.create(Component.translatable("tooltip.fbp.common.rest_on_floor").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRestOnFloor())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.bounce_off_walls"), this.config.terrain::isBounceOffWalls, button -> this.config.terrain.setBounceOffWalls(!this.config.terrain.isBounceOffWalls()), Tooltip.create(Component.translatable("tooltip.fbp.common.bounce_off_walls").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isBounceOffWalls())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.terrain.entity_collision"), this.config.terrain::isEntityCollision, button -> this.config.terrain.setEntityCollision(!this.config.terrain.isEntityCollision()), Tooltip.create(Component.translatable("tooltip.fbp.terrain.entity_collision").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isEntityCollision())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.water_physics"), this.config.terrain::isWaterPhysics, button -> this.config.terrain.setWaterPhysics(!this.config.terrain.isWaterPhysics()), Tooltip.create(Component.translatable("tooltip.fbp.common.water_physics").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isWaterPhysics())))),

                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_size"), this.config.terrain::isRandomSize, button -> this.config.terrain.setRandomSize(!this.config.terrain.isRandomSize()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_size").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRandomSize())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_rotation"), this.config.terrain::isRandomRotation, button -> this.config.terrain.setRandomRotation(!this.config.terrain.isRandomRotation()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_rotation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRandomRotation())))),
                new FBPToggleButton(150, 20, Component.translatable("button.fbp.common.random_fading_speed"), this.config.terrain::isRandomFadingSpeed, button -> this.config.terrain.setRandomFadingSpeed(!this.config.terrain.isRandomFadingSpeed()), Tooltip.create(Component.translatable("tooltip.fbp.common.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRandomFadingSpeed()))), () -> (!this.config.terrain.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked()),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.terrain.particles_per_axis").append(": "), Component.empty(), this.config.terrain.getParticlesPerAxis(), FancyBlockParticles.CONFIG.terrain.getParticlesPerAxis(), 0, 16, 1, button -> this.config.terrain.setParticlesPerAxis(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.terrain.particles_per_axis").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getParticlesPerAxis())).withStyle(ChatFormatting.YELLOW)))),

                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.size_multiplier").append(": "), Component.literal("x"), this.config.terrain.getSizeMultiplier(), FancyBlockParticles.CONFIG.terrain.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.terrain.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.size_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getSizeMultiplier())).withStyle(ChatFormatting.YELLOW)))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.rotation_multiplier").append(": "), Component.literal("x"), this.config.terrain.getRotationMultiplier(), FancyBlockParticles.CONFIG.terrain.getRotationMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.terrain.setRotationMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.rotation_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getRotationMultiplier())).withStyle(ChatFormatting.YELLOW)))),
                new FBPSliderButton(150, 20, Component.translatable("button.fbp.common.gravity_multiplier").append(": "), Component.literal("x"), this.config.terrain.getGravityMultiplier(), FancyBlockParticles.CONFIG.terrain.getGravityMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.terrain.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.common.gravity_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getGravityMultiplier())).withStyle(ChatFormatting.YELLOW))))
        );

        this.list.addBig(
                particleBlacklistButton
        );
    }

    @Override
    protected void resetConfig() {
        this.config.terrain.reset();
    }
}
