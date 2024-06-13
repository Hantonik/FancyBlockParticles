package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.component.FBPOptionsList;
import hantonik.fbp.screen.component.widget.button.FBPImageButton;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public abstract class FBPAbstractOptionsScreen extends Screen {
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 62, 74);

    private final FBPConfig activeConfig;
    protected final FBPConfig config;
    protected final Screen lastScreen;

    protected FBPOptionsList list;

    public FBPAbstractOptionsScreen(Component title, Screen lastScreen, FBPConfig config) {
        super(Component.translatable("key.fbp.category").append(" - ").append(title));

        this.activeConfig = config;
        this.config = config.copy();
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.layout.addToHeader(new FBPImageButton(25, 25, FBPOptionsScreen.LOGO_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed")))), LayoutSettings.defaults().alignHorizontallyLeft().alignVerticallyTop().padding(10)).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.homepage")));
        this.layout.addToHeader(new FBPImageButton(25, 25, FBPOptionsScreen.REPORT_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles/issues")))), LayoutSettings.defaults().alignHorizontallyRight().alignVerticallyTop().padding(10)).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.report")));

        this.layout.addToHeader(new StringWidget(this.title, this.font), LayoutSettings.defaults().alignHorizontallyCenter().alignVerticallyMiddle());

        this.list = this.addRenderableWidget(new FBPOptionsList(this.minecraft, this.width, this.height, this));

        this.initOptions();

        var footer = new GridLayout();
        footer.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();

        var footerHelper = footer.createRowHelper(2);
        footerHelper.addChild(Button.builder(Component.translatable("button.fbp.common.reload"), button -> {
            FancyBlockParticles.CONFIG.reload();
            this.config.setConfig(FancyBlockParticles.CONFIG.copy());
            this.activeConfig.setConfig(FancyBlockParticles.CONFIG.copy());

            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), Component.translatable("button.fbp.common.reload"), Component.translatable("screen.fbp.reload_alert")));

            this.rebuildWidgets();
        }).width(310).build(), 2);
        footerHelper.addChild(Button.builder(Component.translatable("button.fbp.common.reset"), button -> this.minecraft.setScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                this.resetConfig();

                this.rebuildWidgets();
            }

            this.minecraft.setScreen(this);
        }, Component.translatable("button.fbp.common.reset"), Component.translatable("screen.fbp.reset_confirm")))).width(150).build());
        footerHelper.addChild(Button.builder(Component.translatable("button.fbp.common.done"), button -> this.onDone()).width(150).build());

        this.layout.addToFooter(footer);

        var version = Component.translatable("text.fbp.version", SharedConstants.getCurrentVersion().getName() + "-" + FancyBlockParticles.MOD_VERSION);
        this.layout.addToFooter(new StringWidget(this.font.width(version), 9, version, this.font), LayoutSettings.defaults().alignHorizontallyLeft().alignVerticallyBottom().paddingLeft(5).paddingBottom(3));

        this.layout.visitWidgets(widget -> {
            if (widget instanceof FBPToggleButton || widget instanceof FBPSliderButton)
                widget.active = !this.activeConfig.global.isLocked();
        });

        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();
    }

    protected abstract void initOptions();

    protected abstract void resetConfig();

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER && modifiers == 0)) {
            this.onDone();

            return true;
        }

        if (FBPKeyMappings.OPEN_SETTINGS.matches(keyCode, scanCode)) {
            this.minecraft.setScreen(null);

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.lastScreen instanceof FBPAbstractOptionsScreen screen)
            screen.rebuildWidgets();

        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        this.renderDirtBackground(graphics);
    }

    protected void onDone() {
        this.activeConfig.setConfig(this.config);

        FancyBlockParticles.CONFIG.setConfig(this.activeConfig);
        FancyBlockParticles.CONFIG.save();

        this.onClose();
    }

    protected Button openScreenButton(Component title, Supplier<Screen> screen) {
        return Button.builder(title, onPress -> this.minecraft.setScreen(screen.get())).build();
    }
}
