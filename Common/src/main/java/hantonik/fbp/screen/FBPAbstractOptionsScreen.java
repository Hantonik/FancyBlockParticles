package hantonik.fbp.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.component.FBPOptionsList;
import hantonik.fbp.screen.component.widget.FBPStringWidget;
import hantonik.fbp.screen.component.widget.button.FBPImageButton;
import hantonik.fbp.screen.component.widget.button.FBPSliderButton;
import hantonik.fbp.screen.component.widget.button.FBPToggleButton;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Supplier;

public abstract class FBPAbstractOptionsScreen extends Screen {
    private final FBPConfig activeConfig;
    protected final FBPConfig config;
    protected final Screen lastScreen;

    protected FBPOptionsList list;

    public FBPAbstractOptionsScreen(Component title, Screen lastScreen, FBPConfig config) {
        super(new TranslatableComponent("key.fbp.category").append(" - ").append(title));

        this.activeConfig = config;
        this.config = config.copy();
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.list = new FBPOptionsList(this.minecraft, this.width, this.height, 62, 74);

        this.initOptions();
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(new FBPImageButton(10, 10, 25, 25, FBPOptionsScreen.LOGO_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed"))), new TranslatableComponent("tooltip.fbp.common.homepage")));
        this.addRenderableWidget(new FBPImageButton(this.width - 10 - 25, 10, 25, 25, FBPOptionsScreen.REPORT_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles/issues"))), new TranslatableComponent("tooltip.fbp.common.report")));

        var titleWidth = this.font.width(this.title.getVisualOrderText());
        this.addRenderableWidget(new FBPStringWidget(this.width / 2 - titleWidth / 2, 62 / 2, titleWidth, 9, this.title, this.font));

        this.addRenderableWidget(new Button(this.width / 2 - 310 / 2, this.height - 74 / 2 - 20 - 4 / 2, 310, 20, new TranslatableComponent("button.fbp.common.reload"), button -> {
            FancyBlockParticles.CONFIG.load();
            this.config.setConfig(FancyBlockParticles.CONFIG.copy());
            this.activeConfig.setConfig(FancyBlockParticles.CONFIG.copy());

            this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), new TranslatableComponent("button.fbp.common.reload"), new TranslatableComponent("screen.fbp.reload_alert")));

            this.rebuildWidgets();
        }));

        this.addRenderableWidget(new Button(this.width / 2 - 150 - 5, this.height - 74 / 2 + 4 / 2, 150, 20, new TranslatableComponent("button.fbp.common.reset"), button -> this.minecraft.setScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                this.resetConfig();

                this.rebuildWidgets();
            }

            this.minecraft.setScreen(this);
        }, new TranslatableComponent("button.fbp.common.reset"), new TranslatableComponent("screen.fbp.reset_confirm")))));

        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 74 / 2 + 4 / 2, 150, 20, new TranslatableComponent("button.fbp.common.done"), button -> this.onDone()));

        var version = new TranslatableComponent("text.fbp.version", SharedConstants.getCurrentVersion().getName() + "-" + FancyBlockParticles.MOD_VERSION);
        this.addRenderableWidget(new FBPStringWidget(5, this.height - 3 - this.font.lineHeight, this.font.width(version), 9, version, this.font).alignLeft());

        this.children().forEach(widget -> {
            if (widget instanceof FBPToggleButton || widget instanceof FBPSliderButton)
                ((AbstractWidget) widget).active = !this.activeConfig.global.isLocked();
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
        if (this.lastScreen instanceof FBPAbstractOptionsScreen screen)
            screen.rebuildWidgets();

        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(stack);

        this.font.draw(stack, new TranslatableComponent("text.fbp.version", SharedConstants.getCurrentVersion().getName() + "-" + FancyBlockParticles.MOD_VERSION), 5.0F, (float) this.height - 3.0F, 4210752);

        super.render(stack, mouseX, mouseY, partialTick);

        this.renderTooltip(stack, this.tooltipAt(mouseX, mouseY), mouseX, mouseY);
    }

    @Override
    public void renderBackground(PoseStack stack) {
        this.renderDirtBackground(0);
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.setFocused(null);
        this.init();
    }

    protected void onDone() {
        this.activeConfig.setConfig(this.config);

        FancyBlockParticles.CONFIG.setConfig(this.activeConfig);
        FancyBlockParticles.CONFIG.save();

        this.onClose();
    }

    private List<FormattedCharSequence> tooltipAt(int mouseX, int mouseY) {
        for (var widget : this.children()) {
            if (widget.isMouseOver(mouseX, mouseY))
                if (widget instanceof TooltipAccessor accessor)
                    return accessor.getTooltip();
        }

        var widget = this.list.getMouseOver(mouseX, mouseY);

        if (widget.isPresent() && widget.get() instanceof TooltipAccessor accessor)
            return accessor.getTooltip();

        return ImmutableList.of();
    }

    protected Button openScreenButton(Component title, Supplier<Screen> screen) {
        return new Button(0, 0, 150, 20, title, button -> this.minecraft.setScreen(screen.get()));
    }

    protected Button openScreenButton(Component title, Supplier<Screen> screen, Component tooltip) {
        return new TooltipButton(0, 0, 150, 20, title, button -> this.minecraft.setScreen(screen.get()), tooltip);
    }

    private static class TooltipButton extends Button implements TooltipAccessor {
        private final Component tooltip;

        public TooltipButton(int x, int y, int width, int height, Component title, OnPress onPress, Component tooltip) {
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
        public List<FormattedCharSequence> getTooltip() {
            return Minecraft.getInstance().font.split(this.tooltip, 150);
        }
    }
}
