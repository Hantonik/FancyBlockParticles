package hantonik.fbp.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.screen.component.widget.FBPStringWidget;
import hantonik.fbp.screen.component.widget.button.FBPImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class FBPOculusWarningScreen extends Screen {
    private static final Component OCULUS_COMPONENT = new TextComponent("Oculus").withStyle(ChatFormatting.AQUA);

    private final Screen lastScreen;

    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private MultiLineLabel report = MultiLineLabel.EMPTY;

    private int exitCountdown = 20 * 5;

    private Button continueButton;
    private Button dontShowAgainButton;

    public FBPOculusWarningScreen(Screen lastScreen) {
        super(new TranslatableComponent("screen.fbp.shaders_warning", OCULUS_COMPONENT));

        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new FBPImageButton(10, 10, 25, 25, FBPOptionsScreen.LOGO_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed"))), new TranslatableComponent("tooltip.fbp.common.homepage")));
        this.addRenderableWidget(new FBPImageButton(this.width - 10 - 25, 10, 25, 25, FBPOptionsScreen.REPORT_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles/issues"))), new TranslatableComponent("tooltip.fbp.common.report")));

        var titleWidth = this.font.width(this.title.getVisualOrderText());
        this.addRenderableWidget(new FBPStringWidget(this.width / 2 - titleWidth / 2, 52 / 2, titleWidth, 9, this.title, this.font));

        this.message = MultiLineLabel.create(this.font, new TranslatableComponent("gui.fbp.shaders_warning.message", OCULUS_COMPONENT), this.width - 45);
        this.report = MultiLineLabel.create(this.font, new TranslatableComponent("gui.fbp.shaders_warning.report", OCULUS_COMPONENT), this.width - 45);

        this.continueButton = this.addRenderableWidget(new Button(this.width / 2 - 150 - 5, this.height - 84 / 2 + 4 / 2, 150, 20, new TranslatableComponent("button.fbp.common.continue"), button -> this.onClose()));
        this.continueButton.active = false;

        this.dontShowAgainButton = this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 84 / 2 + 4 / 2, 150, 20, new TranslatableComponent("button.fbp.shaders_warning.dont_show_again"), button -> {
            FancyBlockParticles.CONFIG.global.setDisableOculusWarning(true);
            FancyBlockParticles.CONFIG.save();

            this.onClose();
        }));

        this.dontShowAgainButton.active = false;
    }

    @Override
    public void tick() {
        if (this.exitCountdown <= 0) {
            this.continueButton.active = true;
            this.dontShowAgainButton.active = true;
        } else
            this.exitCountdown--;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.exitCountdown <= 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) && modifiers == 0 && this.shouldCloseOnEsc()) {
            this.onClose();

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(stack);

        super.render(stack, mouseX, mouseY, partialTick);

        this.message.renderCentered(stack, this.width / 2, 62 + 7);
        this.report.renderCentered(stack, this.width / 2, 62 + 7 + this.message.getLineCount() * this.font.lineHeight + 5);

        if (!this.continueButton.active)
            if (mouseX > this.continueButton.x && mouseX < this.continueButton.x + this.continueButton.getWidth() && mouseY > this.continueButton.y && mouseY < this.continueButton.y + this.continueButton.getHeight())
                renderTooltip(stack, new TranslatableComponent("tooltip.fbp.shaders_warning.wait", String.valueOf((this.exitCountdown / 20) + 1)), mouseX, mouseY);

        if (!this.dontShowAgainButton.active)
            if (mouseX > this.dontShowAgainButton.x && mouseX < this.dontShowAgainButton.x + this.dontShowAgainButton.getWidth() && mouseY > this.dontShowAgainButton.y && mouseY < this.dontShowAgainButton.y + this.dontShowAgainButton.getHeight())
                renderTooltip(stack, new TranslatableComponent("tooltip.fbp.shaders_warning.wait", String.valueOf((this.exitCountdown / 20) + 1)), mouseX, mouseY);

        this.renderTooltip(stack, this.tooltipAt(mouseX, mouseY), mouseX, mouseY);
    }

    @Override
    public void renderBackground(PoseStack stack) {
        this.renderDirtBackground(0);
    }

    private List<FormattedCharSequence> tooltipAt(int mouseX, int mouseY) {
        for (var widget : this.children()) {
            if (widget.isMouseOver(mouseX, mouseY))
                if (widget instanceof TooltipAccessor accessor)
                    return accessor.getTooltip();
        }

        return ImmutableList.of();
    }
}
