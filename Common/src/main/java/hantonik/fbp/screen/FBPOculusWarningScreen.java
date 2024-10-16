package hantonik.fbp.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.screen.component.widget.button.FBPImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

public class FBPOculusWarningScreen extends Screen {
    private static final Component OCULUS_COMPONENT = Component.literal("Oculus").withStyle(ChatFormatting.AQUA);

    private final Screen lastScreen;

    private int exitCountdown = 20 * 5;

    private Button continueButton;
    private Button dontShowAgainButton;

    public FBPOculusWarningScreen(Screen lastScreen) {
        super(Component.translatable("screen.fbp.shaders_warning", OCULUS_COMPONENT));

        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        var layout = new HeaderAndFooterLayout(this, 52, 84);

        layout.addToHeader(new FBPImageButton(25, 25, FBPOptionsScreen.LOGO_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/fbp-renewed")))), LayoutSettings.defaults().alignHorizontallyLeft().alignVerticallyTop().padding(10)).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.homepage")));
        layout.addToHeader(new FBPImageButton(25, 25, FBPOptionsScreen.REPORT_TEXTURE, button -> this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Hantonik/FancyBlockParticles/issues")))), LayoutSettings.defaults().alignHorizontallyRight().alignVerticallyTop().padding(10)).setTooltip(Tooltip.create(Component.translatable("tooltip.fbp.common.report")));

        layout.addToHeader(new StringWidget(this.title, this.font), LayoutSettings.defaults().alignHorizontallyCenter().alignVerticallyBottom());

        var contents = new GridLayout();
        contents.defaultCellSetting().alignHorizontallyCenter().paddingTop(5);

        var contentsHelper = contents.createRowHelper(1);
        contentsHelper.addChild(new MultiLineTextWidget(Component.translatable("gui.fbp.shaders_warning.message", OCULUS_COMPONENT), this.font).setMaxWidth(this.width - 45).setCentered(true), LayoutSettings.defaults().paddingTop(7));
        contentsHelper.addChild(new MultiLineTextWidget(Component.translatable("gui.fbp.shaders_warning.report", OCULUS_COMPONENT), this.font).setMaxWidth(this.width - 45).setCentered(true));

        layout.addToContents(contents);

        this.continueButton = Button.builder(CommonComponents.GUI_CONTINUE, button -> this.onClose()).build();
        this.continueButton.active = false;

        this.dontShowAgainButton = Button.builder(Component.translatable("button.fbp.shaders_warning.dont_show_again"), button -> {
            FancyBlockParticles.CONFIG.global.setDisableOculusWarning(true);
            FancyBlockParticles.CONFIG.save();

            this.onClose();
        }).build();

        this.dontShowAgainButton.active = false;

        var footer = new GridLayout();
        footer.defaultCellSetting().paddingHorizontal(5);

        var footerHelper = footer.createRowHelper(2);
        footerHelper.addChild(this.continueButton);
        footerHelper.addChild(this.dontShowAgainButton);

        layout.addToFooter(footer);

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
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

        if (!this.continueButton.active)
            if (mouseX > this.continueButton.getX() && mouseX < this.continueButton.getX() + this.continueButton.getWidth() && mouseY > this.continueButton.getY() && mouseY < this.continueButton.getY() + this.continueButton.getHeight())
                renderTooltip(stack, Component.translatable("tooltip.fbp.shaders_warning.wait", String.valueOf((this.exitCountdown / 20) + 1)), mouseX, mouseY);

        if (!this.dontShowAgainButton.active)
            if (mouseX > this.dontShowAgainButton.getX() && mouseX < this.dontShowAgainButton.getX() + this.dontShowAgainButton.getWidth() && mouseY > this.dontShowAgainButton.getY() && mouseY < this.dontShowAgainButton.getY() + this.dontShowAgainButton.getHeight())
                renderTooltip(stack, Component.translatable("tooltip.fbp.shaders_warning.wait", String.valueOf((this.exitCountdown / 20) + 1)), mouseX, mouseY);
    }

    @Override
    public void renderBackground(PoseStack stack) {
        this.renderDirtBackground(stack);
    }
}
