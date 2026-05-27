package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.component.FBPOptionsList;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.util.function.Supplier;

public abstract class FBPAbstractOptionsScreen extends Screen {
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 62, 74);

    private final FBPConfig activeConfig;
    protected final FBPConfig config;
    protected final Screen lastScreen;

    protected FBPOptionsList list;

    public FBPAbstractOptionsScreen(Component title, Screen lastScreen, FBPConfig config) {
        super(Component.translatable("key.category.fbp.category").append(" - ").append(title));

        this.activeConfig = config;
        this.config = config.copy();
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.layout.addToHeader(new ImageButton(25, 25, FBPOptionsScreen.LOGO_SPRITES, _ -> defaultHandleGameClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/fbp-renewed")), this.minecraft, this), CommonComponents.EMPTY), settings -> settings.alignHorizontallyLeft().alignVerticallyTop().padding(10)).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.homepage")));
        this.layout.addToHeader(new ImageButton(25, 25, FBPOptionsScreen.REPORT_SPRITES, _ -> defaultHandleGameClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/Hantonik/FancyBlockParticles/issues")), this.minecraft, this), CommonComponents.EMPTY), settings -> settings.alignHorizontallyRight().alignVerticallyTop().padding(10)).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.report")));

        this.layout.addToHeader(new StringWidget(this.title, this.font), LayoutSettings::alignHorizontallyCenter);

        this.list = this.addRenderableWidget(new FBPOptionsList(this.minecraft, this.width, this));

        this.initOptions();

        var footer = new GridLayout();
        footer.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();

        var footerHelper = footer.createRowHelper(2);
        footerHelper.addChild(Button.builder(Component.translatable("button.fbp.common.reload"), _ -> {
            FancyBlockParticles.CONFIG.load();
            this.config.setConfig(FancyBlockParticles.CONFIG.copy());
            this.activeConfig.setConfig(FancyBlockParticles.CONFIG.copy());

            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), Component.translatable("button.fbp.common.reload"), Component.translatable("screen.fbp.reload_alert")));

            this.rebuildWidgets();
        }).width(310).build(), 2);
        footerHelper.addChild(Button.builder(Component.translatable("button.fbp.common.reset"), _ -> this.minecraft.setScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                this.resetConfig();

                this.rebuildWidgets();
            }

            this.minecraft.setScreen(this);
        }, Component.translatable("button.fbp.common.reset"), Component.translatable("screen.fbp.reset_confirm")))).width(150).build());
        footerHelper.addChild(Button.builder(Component.translatable("button.fbp.common.done"), _ -> this.onDone()).width(150).build());

        this.layout.addToFooter(footer);

        var version = Component.translatable("text.fbp.version", SharedConstants.getCurrentVersion().name() + "-" + FancyBlockParticles.MOD_VERSION);
        this.layout.addToFooter(new StringWidget(this.font.width(version), 9, version, this.font), settings -> settings.alignHorizontallyLeft().alignVerticallyBottom().paddingLeft(5).paddingBottom(3));

        this.layout.visitWidgets(widget -> {
            if (widget instanceof FBPToggleButton || widget instanceof FBPSliderButton)
                widget.active = !this.activeConfig.global.isLocked();
        });

        this.layout.visitWidgets(this::addRenderableWidget);

        this.repositionElements();
    }

    protected abstract void initOptions();

    protected abstract void resetConfig();

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();

        this.list.updateSize(this.width, this.layout);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if ((event.key() == GLFW.GLFW_KEY_ENTER && event.modifiers() == 0)) {
            this.onDone();

            return true;
        }

        if (FBPKeyMappings.OPEN_SETTINGS.matches(event)) {
            this.minecraft.setScreen(null);

            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (this.lastScreen instanceof FBPAbstractOptionsScreen screen) {
            var scrollAmount = screen.list.scrollAmount();

            screen.rebuildWidgets();
            screen.list.setScrollAmount(scrollAmount);
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    protected void onDone() {
        this.activeConfig.setConfig(this.config);

        FancyBlockParticles.CONFIG.setConfig(this.activeConfig);
        FancyBlockParticles.CONFIG.save();

        this.onClose();
    }

    protected Button openScreenButton(Component title, Supplier<Screen> screen) {
        return this.openScreenButton(title, screen, 150, 20);
    }

    protected Button openScreenButton(Component title, Supplier<Screen> screen, int width, int height) {
        return Button.builder(title, _ -> this.minecraft.setScreen(screen.get())).size(width, height).build();
    }
}
