package hantonik.fbp.screen.category;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.DecimalFormat;

public class FBPTerrainScreen extends FBPAbstractOptionsScreen {
    public FBPTerrainScreen(Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("screen.fbp.category.terrain"), lastScreen, config);
    }

    @Override
    protected void initOptions() {
        DecimalFormat formatter = new DecimalFormat("0.00");

        DelayedSupplier<FBPSliderButton> minLifetimeSlider = new DelayedSupplier<>();
        DelayedSupplier<FBPSliderButton> maxLifetimeSlider = new DelayedSupplier<>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.min_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.terrain.getMinLifetime(), FancyBlockParticles.CONFIG.terrain.getMinLifetime(), 0, 100, 1, button -> {
            this.config.terrain.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue()) {
                this.config.terrain.setMaxLifetime(button.getValueInt());

                maxLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.terrain.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.min_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getMinLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.max_lifetime").append(": "), new TranslationTextComponent("button.fbp.common.ticks"), this.config.terrain.getMaxLifetime(), FancyBlockParticles.CONFIG.terrain.getMaxLifetime(), 0, 100, 1, button -> {
            this.config.terrain.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue()) {
                this.config.terrain.setMinLifetime(button.getValueInt());

                minLifetimeSlider.get().setValue(button.getValue());
            }
        }, () -> (!this.config.terrain.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.max_lifetime").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getMaxLifetime())).append(new TranslationTextComponent("button.fbp.common.ticks")).withStyle(TextFormatting.YELLOW))));

        this.list.addBig(
                new FBPToggleButton(310, 20, new TranslationTextComponent("button.fbp.terrain.fancy_breaking_particles"), this.config.terrain::isFancyBreakingParticles, button -> this.config.terrain.setFancyBreakingParticles(!this.config.terrain.isFancyBreakingParticles()), new TranslationTextComponent("tooltip.fbp.terrain.fancy_breaking_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isFancyBreakingParticles())))
        );

        this.list.addSmall(
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.terrain.fancy_cracking_particles"), this.config.terrain::isFancyCrackingParticles, button -> this.config.terrain.setFancyCrackingParticles(!this.config.terrain.isFancyCrackingParticles()), new TranslationTextComponent("tooltip.fbp.terrain.fancy_cracking_particles").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isFancyCrackingParticles()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.spawn_while_frozen"), this.config.terrain::isSpawnWhileFrozen, button -> this.config.terrain.setSpawnWhileFrozen(!this.config.terrain.isSpawnWhileFrozen()), new TranslationTextComponent("tooltip.fbp.common.spawn_while_frozen").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isSpawnWhileFrozen()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.terrain.smart_breaking"), this.config.terrain::isSmartBreaking, button -> this.config.terrain.setSmartBreaking(!this.config.terrain.isSmartBreaking()), new TranslationTextComponent("tooltip.fbp.terrain.smart_breaking").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isSmartBreaking()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.low_traction"), this.config.terrain::isLowTraction, button -> this.config.terrain.setLowTraction(!this.config.terrain.isLowTraction()), new TranslationTextComponent("tooltip.fbp.common.low_traction").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isLowTraction()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.rest_on_floor"), this.config.terrain::isRestOnFloor, button -> this.config.terrain.setRestOnFloor(!this.config.terrain.isRestOnFloor()), new TranslationTextComponent("tooltip.fbp.common.rest_on_floor").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRestOnFloor()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.bounce_off_walls"), this.config.terrain::isBounceOffWalls, button -> this.config.terrain.setBounceOffWalls(!this.config.terrain.isBounceOffWalls()), new TranslationTextComponent("tooltip.fbp.common.bounce_off_walls").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isBounceOffWalls()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.water_physics"), this.config.terrain::isWaterPhysics, button -> this.config.terrain.setWaterPhysics(!this.config.terrain.isWaterPhysics()), new TranslationTextComponent("tooltip.fbp.common.water_physics").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isWaterPhysics()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.terrain.entity_collision"), this.config.terrain::isEntityCollision, button -> this.config.terrain.setEntityCollision(!this.config.terrain.isEntityCollision()), new TranslationTextComponent("tooltip.fbp.terrain.entity_collision").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isEntityCollision()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_rotation"), this.config.terrain::isRandomRotation, button -> this.config.terrain.setRandomRotation(!this.config.terrain.isRandomRotation()), new TranslationTextComponent("tooltip.fbp.common.random_rotation").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRandomRotation()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_size"), this.config.terrain::isRandomSize, button -> this.config.terrain.setRandomSize(!this.config.terrain.isRandomSize()), new TranslationTextComponent("tooltip.fbp.common.random_size").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRandomSize()))),

                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.particles_decay"), () -> !this.config.terrain.isInfiniteDuration(), button -> this.config.terrain.setInfiniteDuration(!this.config.terrain.isInfiniteDuration()), new TranslationTextComponent("tooltip.fbp.common.particles_decay").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + !FBPConfig.DEFAULT_CONFIG.terrain.isInfiniteDuration()))),
                new FBPToggleButton(150, 20, new TranslationTextComponent("button.fbp.common.random_fading_speed"), this.config.terrain::isRandomFadingSpeed, button -> this.config.terrain.setRandomFadingSpeed(!this.config.terrain.isRandomFadingSpeed()), new TranslationTextComponent("tooltip.fbp.common.random_fading_speed").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new TranslationTextComponent("button.fbp.common." + FBPConfig.DEFAULT_CONFIG.terrain.isRandomFadingSpeed())), () -> (!this.config.terrain.isInfiniteDuration() && !this.config.global.isInfiniteDuration()) && !FancyBlockParticles.CONFIG.global.isLocked()),

                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.terrain.particles_per_axis").append(": "), StringTextComponent.EMPTY, this.config.terrain.getParticlesPerAxis(), FancyBlockParticles.CONFIG.terrain.getParticlesPerAxis(), 0, 16, 1, button -> this.config.terrain.setParticlesPerAxis(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.terrain.particles_per_axis").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(String.valueOf(FBPConfig.DEFAULT_CONFIG.terrain.getParticlesPerAxis())).withStyle(TextFormatting.YELLOW))),

                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.size_multiplier").append(": "), new StringTextComponent("x"), this.config.terrain.getSizeMultiplier(), FancyBlockParticles.CONFIG.terrain.getSizeMultiplier(), 0.01D, 2.0D, 0.05D, button -> this.config.terrain.setSizeMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.size_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.terrain.getSizeMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.rotation_multiplier").append(": "), new StringTextComponent("x"), this.config.terrain.getRotationMultiplier(), FancyBlockParticles.CONFIG.terrain.getRotationMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.terrain.setRotationMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.rotation_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.terrain.getRotationMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW))),
                new FBPSliderButton(150, 20, new TranslationTextComponent("button.fbp.common.gravity_multiplier").append(": "), new StringTextComponent("x"), this.config.terrain.getGravityMultiplier(), FancyBlockParticles.CONFIG.terrain.getGravityMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.terrain.setGravityMultiplier(button.getValueFloat()), () -> !FancyBlockParticles.CONFIG.global.isLocked(), new TranslationTextComponent("tooltip.fbp.common.gravity_multiplier").append("\n").append("\n").append(new TranslationTextComponent("tooltip.fbp.default")).append(new StringTextComponent(formatter.format(FBPConfig.DEFAULT_CONFIG.terrain.getGravityMultiplier())).append(new StringTextComponent("x")).withStyle(TextFormatting.YELLOW)))
        );
    }

    @Override
    protected void resetConfig() {
        this.config.terrain.reset();
    }
}
