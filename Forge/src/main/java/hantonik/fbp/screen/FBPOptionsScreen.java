package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.widget.button.FBPSliderButton;
import hantonik.fbp.screen.widget.button.FBPToggleButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class FBPOptionsScreen extends Screen {
    private static final WidgetSprites LOGO_SPRITES = new WidgetSprites(new ResourceLocation(FancyBlockParticles.MOD_ID, "logo"), new ResourceLocation(FancyBlockParticles.MOD_ID, "logo_highlighted"));
    private static final WidgetSprites REPORT_SPRITES = new WidgetSprites(new ResourceLocation(FancyBlockParticles.MOD_ID, "report"), new ResourceLocation(FancyBlockParticles.MOD_ID, "report_highlighted"));

    private final FBPConfig config;
    private final List<LayoutElement> options;

    private int page;

    public FBPOptionsScreen() {
        super(Component.translatable("screen.fbp.settings"));

        this.config = FancyBlockParticles.CONFIG.copy();

        var defaultConfig = FBPConfig.DEFAULT_CONFIG;

        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.min_lifetime"), Component.translatable("button.fbp.ticks"), this.config.getMinLifetime(), FancyBlockParticles.CONFIG.getMinLifetime(), 0, 100, 1, button -> {
            this.config.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue())
                maxLifetimeSlider.get().setValue(button.getValue());
        }, () -> !this.config.isInfiniteDuration() && !FancyBlockParticles.CONFIG.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getMinLifetime())).withStyle(ChatFormatting.YELLOW)))));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.max_lifetime"), Component.translatable("button.fbp.ticks"), this.config.getMaxLifetime(), FancyBlockParticles.CONFIG.getMaxLifetime(), 0, 100, 1, button -> {
            this.config.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue())
                minLifetimeSlider.get().setValue(button.getValue());
        }, () -> !this.config.isInfiniteDuration() && !FancyBlockParticles.CONFIG.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getMaxLifetime())).withStyle(ChatFormatting.YELLOW)))));

        this.options = List.of(
                new FBPToggleButton(Component.translatable("button.fbp.enabled"), this.config::isEnabled, builder -> builder.width(275), button -> this.config.setEnabled(!this.config.isEnabled()), Tooltip.create(Component.translatable("tooltip.fbp.enabled").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isEnabled())))),
                new FBPToggleButton(Component.translatable("button.fbp.particles_decay"), () -> !this.config.isInfiniteDuration(), builder -> builder.width(275), button -> this.config.setInfiniteDuration(!this.config.isInfiniteDuration()), Tooltip.create(Component.translatable("tooltip.fbp.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + !defaultConfig.isInfiniteDuration())))),
                new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.particles_per_axis"), Component.empty(), this.config.getParticlesPerAxis(), FancyBlockParticles.CONFIG.getParticlesPerAxis(), 0, 16, 1, button -> this.config.setParticlesPerAxis(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.particles_per_axis").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getParticlesPerAxis())).withStyle(ChatFormatting.YELLOW)))),
                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),
                new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.scale_multiplier"), Component.literal("x"), this.config.getScaleMultiplier(), FancyBlockParticles.CONFIG.getScaleMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setScaleMultiplier(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.scale_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getScaleMultiplier())).withStyle(ChatFormatting.YELLOW)))),
                new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.rotation_multiplier"), Component.literal("x"), this.config.getRotationMultiplier(), FancyBlockParticles.CONFIG.getRotationMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setRotationMultiplier(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.rotation_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getRotationMultiplier())).withStyle(ChatFormatting.YELLOW)))),
                new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.gravity_multiplier"), Component.literal("x"), this.config.getGravityMultiplier(), FancyBlockParticles.CONFIG.getGravityMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setGravityMultiplier(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.gravity_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getGravityMultiplier())).withStyle(ChatFormatting.YELLOW)))),
                new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.weather_particle_density"), Component.literal("x"), this.config.getWeatherParticleDensity(), FancyBlockParticles.CONFIG.getWeatherParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.setWeatherParticleDensity(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), Tooltip.create(Component.translatable("tooltip.fbp.weather_particle_density").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getWeatherParticleDensity())).withStyle(ChatFormatting.YELLOW)))),
                new FBPToggleButton(Component.translatable("button.fbp.fancy_block_breaking"), this.config::isFancyBlockBraking, builder -> builder.width(275), button -> this.config.setFancyBlockBraking(!this.config.isFancyBlockBraking()), Tooltip.create(Component.translatable("tooltip.fbp.fancy_block_breaking").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isFancyBlockBraking())))),
                new FBPToggleButton(Component.translatable("button.fbp.fancy_flame"), this.config::isFancyFlame, builder -> builder.width(275), button -> this.config.setFancyFlame(!this.config.isFancyFlame()), Tooltip.create(Component.translatable("tooltip.fbp.fancy_flame").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isFancyFlame())))),
                new FBPToggleButton(Component.translatable("button.fbp.fancy_smoke"), this.config::isFancySmoke, builder -> builder.width(275), button -> this.config.setFancySmoke(!this.config.isFancySmoke()), Tooltip.create(Component.translatable("tooltip.fbp.fancy_smoke").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isFancySmoke())))),
                new FBPToggleButton(Component.translatable("button.fbp.fancy_rain"), this.config::isFancyRain, builder -> builder.width(275), button -> this.config.setFancyRain(!this.config.isFancyRain()), Tooltip.create(Component.translatable("tooltip.fbp.fancy_rain").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isFancyRain())))),
                new FBPToggleButton(Component.translatable("button.fbp.fancy_snow"), this.config::isFancySnow, builder -> builder.width(275), button -> this.config.setFancySnow(!this.config.isFancySnow()), Tooltip.create(Component.translatable("tooltip.fbp.fancy_snow").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isFancySnow())))),
                new FBPToggleButton(Component.translatable("button.fbp.fancy_place_animation"), this.config::isFancyPlaceAnimation, builder -> builder.width(275), button -> this.config.setFancyPlaceAnimation(!this.config.isFancyPlaceAnimation()), Tooltip.create(Component.translatable("tooltip.fbp.fancy_place_animation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isFancyPlaceAnimation())))),
                new FBPToggleButton(Component.translatable("button.fbp.cartoon_mode"), this.config::isCartoonMode, builder -> builder.width(275), button -> this.config.setCartoonMode(!this.config.isCartoonMode()), Tooltip.create(Component.translatable("tooltip.fbp.cartoon_mode").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isCartoonMode())))),
                new FBPToggleButton(Component.translatable("button.fbp.cull_particles"), this.config::isCullParticles, builder -> builder.width(275), button -> this.config.setCullParticles(!this.config.isCullParticles()), Tooltip.create(Component.translatable("tooltip.fbp.cull_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isCullParticles())))),
                new FBPToggleButton(Component.translatable("button.fbp.smart_breaking"), this.config::isSmartBreaking, builder -> builder.width(275), button -> this.config.setSmartBreaking(!this.config.isSmartBreaking()), Tooltip.create(Component.translatable("tooltip.fbp.smart_breaking").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isSmartBreaking())))),
                new FBPToggleButton(Component.translatable("button.fbp.low_traction"), this.config::isLowTraction, builder -> builder.width(275), button -> this.config.setLowTraction(!this.config.isLowTraction()), Tooltip.create(Component.translatable("tooltip.fbp.low_traction").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isLowTraction())))),
                new FBPToggleButton(Component.translatable("button.fbp.spawn_while_frozen"), this.config::isSpawnWhileFrozen, builder -> builder.width(275), button -> this.config.setSpawnWhileFrozen(!this.config.isSpawnWhileFrozen()), Tooltip.create(Component.translatable("tooltip.fbp.spawn_while_frozen").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isSpawnWhileFrozen())))),
                new FBPToggleButton(Component.translatable("button.fbp.spawn_place_particles"), this.config::isSpawnPlaceParticles, builder -> builder.width(275), button -> this.config.setSpawnPlaceParticles(!this.config.isSpawnPlaceParticles()), Tooltip.create(Component.translatable("tooltip.fbp.spawn_place_particles").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isSpawnPlaceParticles())))),
                new FBPToggleButton(Component.translatable("button.fbp.rest_on_floor"), this.config::isRestOnFloor, builder -> builder.width(275), button -> this.config.setRestOnFloor(!this.config.isRestOnFloor()), Tooltip.create(Component.translatable("tooltip.fbp.rest_on_floor").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isRestOnFloor())))),
                new FBPToggleButton(Component.translatable("button.fbp.bounce_off_walls"), this.config::isBounceOffWalls, builder -> builder.width(275), button -> this.config.setBounceOffWalls(!this.config.isBounceOffWalls()), Tooltip.create(Component.translatable("tooltip.fbp.bounce_off_walls").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isBounceOffWalls())))),
                new FBPToggleButton(Component.translatable("button.fbp.entity_collision"), this.config::isEntityCollision, builder -> builder.width(275), button -> this.config.setEntityCollision(!this.config.isEntityCollision()), Tooltip.create(Component.translatable("tooltip.fbp.entity_collision").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isEntityCollision())))),
                new FBPToggleButton(Component.translatable("button.fbp.water_physics"), this.config::isWaterPhysics, builder -> builder.width(275), button -> this.config.setWaterPhysics(!this.config.isWaterPhysics()), Tooltip.create(Component.translatable("tooltip.fbp.water_physics").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isWaterPhysics())))),
                new FBPToggleButton(Component.translatable("button.fbp.random_scale"), this.config::isRandomScale, builder -> builder.width(275), button -> this.config.setRandomScale(!this.config.isRandomScale()), Tooltip.create(Component.translatable("tooltip.fbp.random_scale").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isRandomScale())))),
                new FBPToggleButton(Component.translatable("button.fbp.random_rotation"), this.config::isRandomRotation, builder -> builder.width(275), button -> this.config.setRandomRotation(!this.config.isRandomRotation()), Tooltip.create(Component.translatable("tooltip.fbp.random_rotation").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isRandomRotation())))),
                new FBPToggleButton(Component.translatable("button.fbp.random_fading_speed"), this.config::isRandomFadingSpeed, builder -> builder.width(275), button -> this.config.setRandomFadingSpeed(!this.config.isRandomFadingSpeed()), Tooltip.create(Component.translatable("tooltip.fbp.random_fading_speed").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isRandomFadingSpeed())))),
                new FBPToggleButton(Component.translatable("button.fbp.smooth_animation_lighting"), this.config::isSmoothAnimationLighting, builder -> builder.width(275), button -> this.config.setSmoothAnimationLighting(!this.config.isSmoothAnimationLighting()), Tooltip.create(Component.translatable("tooltip.fbp.smooth_animation_lighting").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isSmoothAnimationLighting()))))
        );

        this.page = 1;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new ImageButton(10, 10, 25, 25, LOGO_SPRITES, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed"))))).setTooltip(Tooltip.create(Component.translatable("button.fbp.homepage")));
        this.addRenderableWidget(new ImageButton(this.width - 10 - 25, 10, 25, 25, REPORT_SPRITES, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles/issues"))))).setTooltip(Tooltip.create(Component.translatable("button.fbp.report")));

        var version = Component.translatable("text.fbp.version", SharedConstants.getCurrentVersion().getName() + "-" + FancyBlockParticles.MOD_VERSION);
        this.addRenderableWidget(new StringWidget(5, this.height - 5 - 9, this.font.width(version), 9, version, this.font));

        var layout = new GridLayout();
        layout.defaultCellSetting().paddingTop(3).paddingLeft(3).paddingRight(2).alignVerticallyMiddle().alignHorizontallyCenter();

        var helper = layout.createRowHelper(6);

        helper.addChild(SpacerElement.height(2), 6);
        helper.addChild(new StringWidget(this.title, this.font), 6);
        helper.addChild(SpacerElement.height(0), 6);

        var optionsPerPage = Math.min(Math.max((this.height - 145) / 20, 1), this.options.size());
        var pageCount = (int) Math.ceil((double) this.options.size() / optionsPerPage);

        if (this.page > pageCount)
            this.page = pageCount;

        var startElement = (this.page - 1) * optionsPerPage;

        for (var i = startElement; i < optionsPerPage + startElement; i++) {
            helper.addChild(SpacerElement.width(75));
            helper.addChild(this.options.size() > i ? this.options.get(i) : SpacerElement.height(20), 4);
            helper.addChild(SpacerElement.width(75));
        }

        var previousButton = helper.addChild(Button.builder(Component.translatable("button.fbp.previous"), button -> {
            this.page = Math.max(1, this.page - 1);

            this.rebuildWidgets();
        }).width(75).build());

        helper.addChild(new StringWidget(275, 20, Component.translatable("text.fbp.page", this.page, pageCount), this.font), 4);

        var nextButton = helper.addChild(Button.builder(Component.translatable("button.fbp.next"), button -> {
            this.page = Math.min(pageCount, this.page + 1);

            this.rebuildWidgets();
        }).width(75).build());

        helper.addChild(SpacerElement.width(75));
        var reloadButton = helper.addChild(Button.builder(Component.translatable("button.fbp.reload"), button -> {
            FancyBlockParticles.CONFIG.reload();
            this.config.setConfig(FancyBlockParticles.CONFIG.copy());

            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), Component.translatable("button.fbp.reload"), Component.translatable("screen.fbp.reload_alert")));

            this.rebuildWidgets();
        }).width(275).build(), 4);
        helper.addChild(SpacerElement.width(75));

        helper.addChild(SpacerElement.width(75));
        helper.addChild(Button.builder(Component.translatable("button.fbp.reset"), button -> this.minecraft.setScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                this.config.reset();

                this.rebuildWidgets();
            }

            this.minecraft.setScreen(this);
        }, Component.translatable("button.fbp.reset"), Component.translatable("screen.fbp.reset_confirm")))).width(135).build(), 2);

        var doneButton = helper.addChild(Button.builder(Component.translatable("button.fbp.done"), button -> this.onDone()).width(135).build(), 2);
        helper.addChild(SpacerElement.width(75));

        layout.arrangeElements();
        FrameLayout.alignInRectangle(layout, 0, 0, this.width, this.height, 0.5F, 0.25F);

        layout.visitWidgets(widget -> {
            if (widget != doneButton && widget != reloadButton && widget != previousButton && widget != nextButton)
                widget.active = !FancyBlockParticles.CONFIG.isLocked();

            if (widget == previousButton)
                widget.active = this.page > 1;
            if (widget == nextButton)
                widget.active = this.page < pageCount;
        });

        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER && modifiers == 0)) {
            this.onDone();

            return true;
        }

        if (FBPKeyMappings.SETTINGS.get().matches(keyCode, scanCode)) {
            this.onClose();

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(graphics);
    }

    private void onDone() {
        FancyBlockParticles.CONFIG.applyConfig(this.config);
        FancyBlockParticles.CONFIG.save();

        this.onClose();
    }
}
