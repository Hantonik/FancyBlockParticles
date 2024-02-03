package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPRenderConfig;
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
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class FBPOptionsScreen extends Screen {
    private static final WidgetSprites LOGO_SPRITES = new WidgetSprites(new ResourceLocation(FancyBlockParticles.MOD_ID, "logo"), new ResourceLocation(FancyBlockParticles.MOD_ID, "logo_highlighted"));
    private static final WidgetSprites REPORT_SPRITES = new WidgetSprites(new ResourceLocation(FancyBlockParticles.MOD_ID, "report"), new ResourceLocation(FancyBlockParticles.MOD_ID, "report_highlighted"));

    private final FBPRenderConfig config;

    public FBPOptionsScreen() {
        super(Component.translatable("screen.fbp.settings"));

        this.config = FancyBlockParticles.RENDER_CONFIG.copy();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new ImageButton(10, 10, 25, 25, LOGO_SPRITES, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed"))))).setTooltip(Tooltip.create(Component.translatable("button.fbp.homepage")));
        this.addRenderableWidget(new ImageButton(this.width - 10 - 25, 10, 25, 25, REPORT_SPRITES, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles-Remake/issues"))))).setTooltip(Tooltip.create(Component.translatable("button.fbp.report")));

        var version = Component.translatable("text.fbp.version", SharedConstants.getCurrentVersion().getName() + "-" + FancyBlockParticles.MOD_VERSION);
        this.addRenderableWidget(new StringWidget(5, this.height - 5 - 9, this.font.width(version), 9, version, this.font));

        var defaultConfig = FBPRenderConfig.DEFAULT_CONFIG;

        var layout = new GridLayout();

        layout.defaultCellSetting().paddingTop(3).paddingHorizontal(2).alignVerticallyMiddle().alignHorizontallyCenter();

        var helper = layout.createRowHelper(2);

        helper.addChild(SpacerElement.height(2), 2);

        helper.addChild(new StringWidget(this.title, this.font), 2);

        helper.addChild(SpacerElement.height(0), 2);

        helper.addChild(new FBPToggleButton(Component.translatable("button.fbp.enabled"), this.config::isEnabled, builder -> builder.width(275), button -> this.config.setEnabled(!this.config.isEnabled())), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.enabled").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + defaultConfig.isEnabled()))));
        helper.addChild(new FBPToggleButton(Component.translatable("button.fbp.particles_decay"), () -> !this.config.isInfiniteDuration(), builder -> builder.width(275), button -> this.config.setInfiniteDuration(!this.config.isInfiniteDuration())), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.particles_decay").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.translatable("button.fbp." + !defaultConfig.isInfiniteDuration()))));

        helper.addChild(new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.particles_per_axis"), Component.empty(), this.config.getParticlesPerAxis(), FancyBlockParticles.RENDER_CONFIG.getParticlesPerAxis(), 0, 16, 1, button -> this.config.setParticlesPerAxis(button.getValueInt()), () -> !FancyBlockParticles.RENDER_CONFIG.isLocked()), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.particles_per_axis").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getParticlesPerAxis())).withStyle(ChatFormatting.YELLOW))));

        var minLifetimeSlider = new DelayedSupplier<FBPSliderButton>();
        var maxLifetimeSlider = new DelayedSupplier<FBPSliderButton>();

        minLifetimeSlider.setSupplier(() -> new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.min_lifetime"), Component.translatable("button.fbp.ticks"), this.config.getMinLifetime(), FancyBlockParticles.RENDER_CONFIG.getMinLifetime(), 0, 100, 1, button -> {
            this.config.setMinLifetime(button.getValueInt());

            if (button.getValue() > maxLifetimeSlider.get().getValue())
                maxLifetimeSlider.get().setValue(button.getValue());
        }, () -> !this.config.isInfiniteDuration() && !FancyBlockParticles.RENDER_CONFIG.isLocked()));

        maxLifetimeSlider.setSupplier(() -> new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.max_lifetime"), Component.translatable("button.fbp.ticks"), this.config.getMaxLifetime(), FancyBlockParticles.RENDER_CONFIG.getMaxLifetime(), 0, 100, 1, button -> {
            this.config.setMaxLifetime(button.getValueInt());

            if (button.getValue() < minLifetimeSlider.get().getValue())
                minLifetimeSlider.get().setValue(button.getValue());
        }, () -> !this.config.isInfiniteDuration() && !FancyBlockParticles.RENDER_CONFIG.isLocked()));

        helper.addChild(minLifetimeSlider.get(), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.min_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getMinLifetime())).withStyle(ChatFormatting.YELLOW))));
        helper.addChild(maxLifetimeSlider.get(), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.max_lifetime").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getMaxLifetime())).withStyle(ChatFormatting.YELLOW))));

        helper.addChild(new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.scale_multiplier"), Component.literal("x"), this.config.getScaleMultiplier(), FancyBlockParticles.RENDER_CONFIG.getScaleMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setScaleMultiplier(button.getValue()), () -> !FancyBlockParticles.RENDER_CONFIG.isLocked()), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.scale_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getScaleMultiplier())).withStyle(ChatFormatting.YELLOW))));
        helper.addChild(new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.rotation_multiplier"), Component.literal("x"), this.config.getRotationMultiplier(), FancyBlockParticles.RENDER_CONFIG.getRotationMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setRotationMultiplier(button.getValue()), () -> !FancyBlockParticles.RENDER_CONFIG.isLocked()), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.rotation_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getRotationMultiplier())).withStyle(ChatFormatting.YELLOW))));
        helper.addChild(new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.gravity_multiplier"), Component.literal("x"), this.config.getGravityMultiplier(), FancyBlockParticles.RENDER_CONFIG.getGravityMultiplier(), 0.0D, 2.0D, 0.05D, button -> this.config.setGravityMultiplier(button.getValue()), () -> !FancyBlockParticles.RENDER_CONFIG.isLocked()), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.gravity_multiplier").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getGravityMultiplier())).withStyle(ChatFormatting.YELLOW))));
        helper.addChild(new FBPSliderButton(0, 0, 275, Component.translatable("button.fbp.weather_particle_density"), Component.literal("x"), this.config.getWeatherParticleDensity(), FancyBlockParticles.RENDER_CONFIG.getWeatherParticleDensity(), 0.0D, 2.0D, 0.05D, button -> this.config.setWeatherParticleDensity(button.getValue()), () -> !FancyBlockParticles.RENDER_CONFIG.isLocked()), 2).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.weather_particle_density").append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.translatable("tooltip.fbp.default")).append(Component.literal(String.valueOf(defaultConfig.getWeatherParticleDensity())).withStyle(ChatFormatting.YELLOW))));

        var reloadButton = helper.addChild(Button.builder(Component.translatable("button.fbp.reload"), button -> {
            FancyBlockParticles.RENDER_CONFIG.reload();
            this.config.setConfig(FancyBlockParticles.RENDER_CONFIG.copy());

            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), Component.translatable("button.fbp.reload"), Component.translatable("screen.fbp.reload_alert")));

            this.rebuildWidgets();
        }).width(275).build(), 2);

        helper.addChild(Button.builder(Component.translatable("button.fbp.reset"), button -> this.minecraft.setScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                this.config.reset();

                this.rebuildWidgets();
            }

            this.minecraft.setScreen(this);
        }, Component.translatable("button.fbp.reset"), Component.translatable("screen.fbp.reset_confirm")))).width(135).build());

        var doneButton = helper.addChild(Button.builder(Component.translatable("button.fbp.done"), button -> this.onDone()).width( 135).build());

        layout.arrangeElements();
        FrameLayout.alignInRectangle(layout, 0, 0, this.width, this.height, 0.5F, 0.25F);

        layout.visitWidgets(widget -> {
            if (widget != doneButton && widget != reloadButton)
                widget.active = !FancyBlockParticles.RENDER_CONFIG.isLocked();
        });

        layout.visitWidgets(this::addRenderableWidget);
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(graphics);
    }

    private void onDone() {
        FancyBlockParticles.RENDER_CONFIG.applyConfig(this.config);
        FancyBlockParticles.RENDER_CONFIG.save();

        this.onClose();
    }
}
