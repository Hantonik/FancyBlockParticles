package hantonik.fbp.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.widget.button.FBPSliderButton;
import hantonik.fbp.screen.widget.button.FBPToggleButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class FBPOptionsScreen extends Screen {
    private static final ResourceLocation LOGO_SPRITES = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/logo.png");
    private static final ResourceLocation REPORT_SPRITES = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/report.png");

    private final FBPConfig config;
    private final List<AbstractWidget> options;

    private int page;
    private int pageCount;

    private int elements;

    public FBPOptionsScreen() {
        super(new TranslatableComponent("screen.fbp.settings"));

        this.config = FancyBlockParticles.CONFIG.copy();

        var defaultConfig = FBPConfig.DEFAULT_CONFIG;

        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(0, 0, 275, new TranslatableComponent("button.fbp.min_lifetime"), new TranslatableComponent("button.fbp.ticks"), this.config.getMinLifetime(), FancyBlockParticles.CONFIG.getMinLifetime(), 0, 100, 1, button -> {
            this.config.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue())
                maxLifetimeSlider.get().setValue(button.getValue());
        }, () -> !this.config.isInfiniteDuration() && !FancyBlockParticles.CONFIG.isLocked(), (widget, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.min_lifetime").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(defaultConfig.getMinLifetime())).withStyle(ChatFormatting.YELLOW)), mouseX, mouseY)));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(0, 0, 275, new TranslatableComponent("button.fbp.max_lifetime"), new TranslatableComponent("button.fbp.ticks"), this.config.getMaxLifetime(), FancyBlockParticles.CONFIG.getMaxLifetime(), 0, 100, 1, button -> {
            this.config.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue())
                minLifetimeSlider.get().setValue(button.getValue());
        }, () -> !this.config.isInfiniteDuration() && !FancyBlockParticles.CONFIG.isLocked(), (widget, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.max_lifetime").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TextComponent(String.valueOf(defaultConfig.getMaxLifetime())).withStyle(ChatFormatting.YELLOW)), mouseX, mouseY)));

        this.options = List.of(
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.enabled"), this.config::isEnabled, button -> this.config.setEnabled(!this.config.isEnabled()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.enabled").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isEnabled()).withStyle(defaultConfig.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.particles_decay"), () -> !this.config.isInfiniteDuration(), button -> this.config.setInfiniteDuration(!this.config.isInfiniteDuration()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.particles_decay").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + !defaultConfig.isInfiniteDuration()).withStyle(!defaultConfig.isInfiniteDuration() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPSliderButton(0, 0, 275, new TranslatableComponent("button.fbp.particles_per_axis"), TextComponent.EMPTY, this.config.getParticlesPerAxis(), FancyBlockParticles.CONFIG.getParticlesPerAxis(), 0, 16, 1, button -> this.config.setParticlesPerAxis(button.getValueInt()), () -> !FancyBlockParticles.CONFIG.isLocked(), (widget, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.particles_per_axis").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default").append(new TextComponent(String.valueOf(defaultConfig.getParticlesPerAxis())).withStyle(ChatFormatting.YELLOW))), mouseX, mouseY)),
                minLifetimeSlider.get(),
                maxLifetimeSlider.get(),
                new FBPSliderButton(0, 0, 275, new TranslatableComponent("button.fbp.scale_multiplier"), new TextComponent("x"), this.config.getScaleMultiplier(), FancyBlockParticles.CONFIG.getScaleMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setScaleMultiplier(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), (widget, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.scale_multiplier").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default").append(new TextComponent(String.valueOf(defaultConfig.getScaleMultiplier())).withStyle(ChatFormatting.YELLOW))), mouseX, mouseY)),
                new FBPSliderButton(0, 0, 275, new TranslatableComponent("button.fbp.rotation_multiplier"), new TextComponent("x"), this.config.getRotationMultiplier(), FancyBlockParticles.CONFIG.getRotationMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setRotationMultiplier(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), (widget, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.rotation_multiplier").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default").append(new TextComponent(String.valueOf(defaultConfig.getRotationMultiplier())).withStyle(ChatFormatting.YELLOW))), mouseX, mouseY)),
                new FBPSliderButton(0, 0, 275, new TranslatableComponent("button.fbp.gravity_multiplier"), new TextComponent("x"), this.config.getGravityMultiplier(), FancyBlockParticles.CONFIG.getGravityMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setGravityMultiplier(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), (widget, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.gravity_multiplier").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default").append(new TextComponent(String.valueOf(defaultConfig.getGravityMultiplier())).withStyle(ChatFormatting.YELLOW))), mouseX, mouseY)),
                new FBPSliderButton(0, 0, 275, new TranslatableComponent("button.fbp.weather_particle_density"), new TextComponent("x"), this.config.getWeatherParticleDensity(), FancyBlockParticles.CONFIG.getWeatherParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.setWeatherParticleDensity(button.getValue()), () -> !FancyBlockParticles.CONFIG.isLocked(), (widget, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.weather_particle_density").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default").append(new TextComponent(String.valueOf(defaultConfig.getWeatherParticleDensity())).withStyle(ChatFormatting.YELLOW))), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.fancy_block_breaking"), this.config::isFancyBlockBraking, button -> this.config.setFancyBlockBraking(!this.config.isFancyBlockBraking()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.fancy_block_breaking").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isFancyBlockBraking()).withStyle(defaultConfig.isFancyFlame() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.fancy_flame"), this.config::isFancyFlame, button -> this.config.setFancyFlame(!this.config.isFancyFlame()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.fancy_flame").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isFancyFlame()).withStyle(defaultConfig.isFancyFlame() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.fancy_smoke"), this.config::isFancySmoke, button -> this.config.setFancySmoke(!this.config.isFancySmoke()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.fancy_smoke").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isFancySmoke()).withStyle(defaultConfig.isFancySmoke() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.fancy_rain"), this.config::isFancyRain, button -> this.config.setFancyRain(!this.config.isFancyRain()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.fancy_rain").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isFancyRain()).withStyle(defaultConfig.isFancyRain() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.fancy_snow"), this.config::isFancySnow, button -> this.config.setFancySnow(!this.config.isFancySnow()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.fancy_snow").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isFancySnow()).withStyle(defaultConfig.isFancySnow() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.fancy_place_animation"), this.config::isFancyPlaceAnimation, button -> this.config.setFancyPlaceAnimation(!this.config.isFancyPlaceAnimation()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.fancy_place_animation").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isFancyPlaceAnimation()).withStyle(defaultConfig.isFancyPlaceAnimation() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.cartoon_mode"), this.config::isCartoonMode, button -> this.config.setCartoonMode(!this.config.isCartoonMode()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.cartoon_mode").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isCartoonMode()).withStyle(defaultConfig.isCartoonMode() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.cull_particles"), this.config::isCullParticles, button -> this.config.setCullParticles(!this.config.isCullParticles()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.cull_particles").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isCullParticles()).withStyle(defaultConfig.isCullParticles() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.smart_breaking"), this.config::isSmartBreaking, button -> this.config.setSmartBreaking(!this.config.isSmartBreaking()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.smart_breaking").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isSmartBreaking()).withStyle(defaultConfig.isSmartBreaking() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.low_traction"), this.config::isLowTraction, button -> this.config.setLowTraction(!this.config.isLowTraction()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.low_traction").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isLowTraction()).withStyle(defaultConfig.isLowTraction() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.spawn_while_frozen"), this.config::isSpawnWhileFrozen, button -> this.config.setSpawnWhileFrozen(!this.config.isSpawnWhileFrozen()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.spawn_while_frozen").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isSpawnWhileFrozen()).withStyle(defaultConfig.isSpawnWhileFrozen() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.spawn_place_particles"), this.config::isSpawnPlaceParticles, button -> this.config.setSpawnPlaceParticles(!this.config.isSpawnPlaceParticles()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.spawn_place_particles").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isSpawnPlaceParticles()).withStyle(defaultConfig.isSpawnPlaceParticles() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.rest_on_floor"), this.config::isRestOnFloor, button -> this.config.setRestOnFloor(!this.config.isRestOnFloor()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.rest_on_floor").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isRestOnFloor()).withStyle(defaultConfig.isRestOnFloor() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.bounce_off_walls"), this.config::isBounceOffWalls, button -> this.config.setBounceOffWalls(!this.config.isBounceOffWalls()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.bounce_off_walls").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isBounceOffWalls()).withStyle(defaultConfig.isBounceOffWalls() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.entity_collision"), this.config::isEntityCollision, button -> this.config.setEntityCollision(!this.config.isEntityCollision()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.entity_collision").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isEntityCollision()).withStyle(defaultConfig.isEntityCollision() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.water_physics"), this.config::isWaterPhysics, button -> this.config.setWaterPhysics(!this.config.isWaterPhysics()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.water_physics").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isWaterPhysics()).withStyle(defaultConfig.isWaterPhysics() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.random_scale"), this.config::isRandomScale, button -> this.config.setRandomScale(!this.config.isRandomScale()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.random_scale").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isRandomScale()).withStyle(defaultConfig.isRandomScale() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.random_rotation"), this.config::isRandomRotation, button -> this.config.setRandomRotation(!this.config.isRandomRotation()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.random_rotation").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isRandomRotation()).withStyle(defaultConfig.isRandomRotation() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.random_fading_speed"), this.config::isRandomFadingSpeed, button -> this.config.setRandomFadingSpeed(!this.config.isRandomFadingSpeed()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.random_fading_speed").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isRandomFadingSpeed()).withStyle(defaultConfig.isRandomFadingSpeed() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY)),
                new FBPToggleButton(0, 0, 275, 20, new TranslatableComponent("button.fbp.smooth_animation_lighting"), this.config::isSmoothAnimationLighting, button -> this.config.setSmoothAnimationLighting(!this.config.isSmoothAnimationLighting()), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("tooltip.fbp.smooth_animation_lighting").append(CommonComponents.NEW_LINE).append(new TranslatableComponent("tooltip.fbp.default")).append(new TranslatableComponent("button.fbp." + defaultConfig.isSmoothAnimationLighting()).withStyle(defaultConfig.isSmoothAnimationLighting() ? ChatFormatting.GREEN : ChatFormatting.RED)), mouseX, mouseY))
        );

        this.page = 1;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new ImageButton(10, 10, 25, 25, 0, 0, 25, LOGO_SPRITES, 256, 256, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed"))), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("button.fbp.homepage"), mouseX, mouseY), TextComponent.EMPTY));
        this.addRenderableWidget(new ImageButton(this.width - 10 - 25, 10, 25, 25, 0, 0, 25, REPORT_SPRITES, 256, 256, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles/issues"))), (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslatableComponent("button.fbp.report"), mouseX, mouseY), TextComponent.EMPTY));

        var optionsPerPage = Math.min(Math.max((this.height - 145) / 20 - 1, 1), this.options.size());

        this.elements = optionsPerPage + 3;
        this.pageCount = (int) Math.ceil((double) this.options.size() / optionsPerPage);

        if (this.page > this.pageCount)
            this.page = this.pageCount;

        var startElement = (this.page - 1) * optionsPerPage;

        for (var i = startElement; i < optionsPerPage + startElement; i++) {
            if (this.options.size() <= i)
                continue;

            var option = this.options.get(i);

            option.x = this.width / 2 - 138;
            option.y = this.height / this.elements + 23 * (i - startElement + 1);

            this.addRenderableWidget(option);
        }

        var previousButton = this.addRenderableWidget(new Button(this.width / 2 - 138 - 3 - 75, this.height / this.elements + 23 * (this.elements - 2), 75, 20, new TranslatableComponent("button.fbp.previous"), button -> {
            this.page = Math.max(1, this.page - 1);

            this.clearWidgets();
            this.init();
        }));

        var nextButton = this.addRenderableWidget(new Button(this.width / 2 + 138 + 2, this.height / this.elements + 23 * (this.elements - 2), 75, 20, new TranslatableComponent("button.fbp.next"), button -> {
            this.page = Math.min(this.pageCount, this.page + 1);

            this.clearWidgets();
            this.init();
        }));

        var reloadButton = this.addRenderableWidget(new Button(this.width / 2 - 138, this.height / this.elements + 23 * (this.elements - 1), 275, 20, new TranslatableComponent("button.fbp.reload"), button -> {
            FancyBlockParticles.CONFIG.reload();
            this.config.setConfig(FancyBlockParticles.CONFIG.copy());

            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), new TranslatableComponent("button.fbp.reload"), new TranslatableComponent("screen.fbp.reload_alert")));

            this.clearWidgets();
            this.init();
        }));

        this.addRenderableWidget(new Button(this.width / 2 - 135 - 3, this.height / this.elements + 23 * this.elements, 135, 20, new TranslatableComponent("button.fbp.reset"), button -> this.minecraft.setScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                this.config.reset();

                this.clearWidgets();
                this.init();
            }

            this.minecraft.setScreen(this);
        }, new TranslatableComponent("button.fbp.reset"), new TranslatableComponent("screen.fbp.reset_confirm")))));

        var doneButton = this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / this.elements + 23 * this.elements, 135, 20, new TranslatableComponent("button.fbp.done"), button -> this.onDone()));

        for (var child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                if (widget != doneButton && widget != reloadButton && widget != previousButton && widget != nextButton)
                    widget.active = !FancyBlockParticles.CONFIG.isLocked();

                if (widget == previousButton)
                    widget.active = this.page > 1;
                if (widget == nextButton)
                    widget.active = this.page < this.pageCount;
            }
        }
    }

    @Override
    public void renderTooltip(PoseStack stack, Component tooltip, int mouseX, int mouseY) {
        List<Pair<Style, List<MutableComponent>>> visited = Lists.newArrayList();

        tooltip.visit((style, string) -> {
            List<MutableComponent> components = Lists.newArrayList();
            var lines = string.replace("\n", " \n").split("\n");

            for (var i = 0; i < lines.length; i++) {
                var line = lines[i];

                components.add(new TextComponent(line).withStyle(style));

                if (i + 1 < lines.length)
                    components.add(TextComponent.EMPTY.copy());
            }

            if (visited.isEmpty())
                visited.add(Pair.of(style, components));
            else {
                var last = visited.get(visited.size() - 1);

                if (last.getFirst() == style)
                    last.getSecond().addAll(components);
                else
                    visited.add(Pair.of(style, components));
            }

            return Optional.empty();
        }, Style.EMPTY);

        List<MutableComponent> components = Lists.newArrayList();

        for (var i = 0; i < visited.size(); i++) {
            var entryComponents = visited.get(i).getSecond();

            if (i == 0)
                components.addAll(entryComponents);
            else {
                for (var j = 0; j < entryComponents.size(); j++) {
                    if (j == 0)
                        components.get(components.size() - 1).append(entryComponents.get(j));
                    else
                        components.add(entryComponents.get(j));
                }
            }
        }

        this.renderTooltip(stack, components.stream().flatMap(c -> this.font.split(c, 175).stream()).toList(), mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER && modifiers == 0)) {
            this.onDone();

            return true;
        }

        if (FBPKeyMappings.SETTINGS.matches(keyCode, scanCode)) {
            this.onClose();

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(stack);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        drawString(stack, this.font, new TranslatableComponent("text.fbp.version", SharedConstants.getCurrentVersion().getName() + "-" + FancyBlockParticles.MOD_VERSION), 5, this.height - 5 - 9, 0xFFFFFF | 255 << 24);

        super.render(stack, mouseX, mouseY, partialTick);

        drawCenteredString(stack, this.font, new TranslatableComponent("text.fbp.page", this.page, this.pageCount), this.width / 2, this.height / this.elements + 23 * (this.elements - 2) + this.font.lineHeight / 2 + 1, 0xFFFFFF | 255 << 24);
        drawCenteredString(stack, this.font, this.title, this.width / 2, this.height / this.elements + 8, 0xFFFFFF | 255 << 24);
    }

    @Override
    public void renderBackground(PoseStack stack) {
        this.renderDirtBackground(0);
    }

    private void onDone() {
        FancyBlockParticles.CONFIG.applyConfig(this.config);
        FancyBlockParticles.CONFIG.save();

        this.onClose();
    }
}
