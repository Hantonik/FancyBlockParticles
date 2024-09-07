package hantonik.fbp.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.component.FBPOptionsList;
import hantonik.fbp.screen.component.widget.FBPStringWidget;
import hantonik.fbp.screen.component.widget.button.FBPImageButton;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IBidiTooltip;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.AlertScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class FBPAbstractOptionsScreen extends Screen {
    private final FBPConfig activeConfig;
    protected final FBPConfig config;
    protected final Screen lastScreen;

    protected FBPOptionsList list;

    public FBPAbstractOptionsScreen(ITextComponent title, Screen lastScreen, FBPConfig config) {
        super(new TranslationTextComponent("key.fbp.category").append(" - ").append(title));

        this.activeConfig = config;
        this.config = config.copy();
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.list = new FBPOptionsList(this.minecraft, this.width, this.height, 62, 74);

        this.initOptions();
        this.addWidget(this.list);

        this.addButton(new FBPImageButton(10, 10, 25, 25, FBPOptionsScreen.LOGO_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed"))), new TranslationTextComponent("tooltip.fbp.common.homepage")));
        this.addButton(new FBPImageButton(this.width - 10 - 25, 10, 25, 25, FBPOptionsScreen.REPORT_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles/issues"))), new TranslationTextComponent("tooltip.fbp.common.report")));

        int titleWidth = this.font.width(this.title.getVisualOrderText());
        this.addButton(new FBPStringWidget(this.width / 2 - titleWidth / 2, 62 / 2, titleWidth, 9, this.title, this.font));

        this.addButton(new Button(this.width / 2 - 310 / 2, this.height - 74 / 2 - 20 - 4 / 2, 310, 20, new TranslationTextComponent("button.fbp.common.reload"), button -> {
            FancyBlockParticles.CONFIG.load();
            this.config.setConfig(FancyBlockParticles.CONFIG.copy());
            this.activeConfig.setConfig(FancyBlockParticles.CONFIG.copy());

            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), new TranslationTextComponent("button.fbp.common.reload"), new TranslationTextComponent("screen.fbp.reload_alert")));

            this.rebuildWidgets();
        }));

        this.addButton(new Button(this.width / 2 - 150 - 5, this.height - 74 / 2 + 4 / 2, 150, 20, new TranslationTextComponent("button.fbp.common.reset"), button -> this.minecraft.setScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                this.resetConfig();

                this.rebuildWidgets();
            }

            this.minecraft.setScreen(this);
        }, new TranslationTextComponent("button.fbp.common.reset"), new TranslationTextComponent("screen.fbp.reset_confirm")))));

        this.addButton(new Button(this.width / 2 + 5, this.height - 74 / 2 + 4 / 2, 150, 20, new TranslationTextComponent("button.fbp.common.done"), button -> this.onDone()));

        ITextComponent version = new TranslationTextComponent("text.fbp.version", SharedConstants.getCurrentVersion().getName() + "-" + FBPConstants.MOD_VERSION);
        this.addButton(new FBPStringWidget(5, this.height - 3 - this.font.lineHeight, this.font.width(version), 9, version, this.font).alignLeft());

        this.children().forEach(widget -> {
            if (widget instanceof FBPToggleButton || widget instanceof FBPSliderButton)
                ((Widget) widget).active = !this.activeConfig.global.isLocked();
        });
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
        if (this.lastScreen instanceof FBPAbstractOptionsScreen)
            ((FBPAbstractOptionsScreen) this.lastScreen).rebuildWidgets();

        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(stack);

        this.list.render(stack, mouseX, mouseY, partialTick);

        super.render(stack, mouseX, mouseY, partialTick);

        this.renderTooltip(stack, this.tooltipAt(mouseX, mouseY), mouseX, mouseY);
    }

    @Override
    public void renderBackground(MatrixStack stack) {
        this.renderDirtBackground(0);
    }

    protected void rebuildWidgets() {
        this.buttons.clear();
        this.children.clear();
        this.setFocused(null);
        this.init();
    }

    protected void onDone() {
        this.activeConfig.setConfig(this.config);

        FancyBlockParticles.CONFIG.setConfig(this.activeConfig);
        FancyBlockParticles.CONFIG.save();

        this.onClose();
    }

    private List<IReorderingProcessor> tooltipAt(int mouseX, int mouseY) {
        for (IGuiEventListener widget : this.children()) {
            if (widget.isMouseOver(mouseX, mouseY))
                if (widget instanceof IBidiTooltip)
                    return ((IBidiTooltip) widget).getTooltip().orElse(null);
        }

        Optional<Widget> widget = this.list.getMouseOver(mouseX, mouseY);

        if (widget.isPresent() && widget.get() instanceof IBidiTooltip)
            return ((IBidiTooltip) widget.get()).getTooltip().orElse(null);

        return ImmutableList.of();
    }

    protected Button openScreenButton(ITextComponent title, Supplier<Screen> screen) {
        return new Button(0, 0, 150, 20, title, button -> this.minecraft.setScreen(screen.get()));
    }

    protected Button openScreenButton(ITextComponent title, Supplier<Screen> screen, ITextComponent tooltip) {
        return new TooltipButton(0, 0, 150, 20, title, button -> this.minecraft.setScreen(screen.get()), tooltip);
    }

    private static class TooltipButton extends Button implements IBidiTooltip {
        private final ITextComponent tooltip;

        public TooltipButton(int x, int y, int width, int height, ITextComponent title, IPressable onPress, ITextComponent tooltip) {
            super(x, y, width, height, title, onPress);

            this.tooltip = tooltip;
        }

        @Override
        public boolean isMouseOver(double $$0, double $$1) {
            return this.visible
                    && $$0 >= (double) this.x
                    && $$1 >= (double) this.y
                    && $$0 < (double) (this.x + this.width)
                    && $$1 < (double) (this.y + this.height);
        }

        @Override
        public Optional<List<IReorderingProcessor>> getTooltip() {
            return Optional.of(Minecraft.getInstance().font.split(this.tooltip, 150));
        }
    }
}
