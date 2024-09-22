package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class FBPToggleButton extends Button implements TooltipAccessor {
    @Getter
    private final Supplier<Boolean> value;
    private final Component defaultMessage;

    private final BooleanSupplier active;

    @Setter
    private Component tooltip;

    public FBPToggleButton(int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Component tooltip) {
        this(width, height, message, value, onPress, tooltip, () -> true);
    }

    public FBPToggleButton(int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Component tooltip, BooleanSupplier active) {
        this(0, 0, width, height, message, value, onPress, tooltip, active);
    }

    public FBPToggleButton(int x, int y, int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Component tooltip, BooleanSupplier active) {
        super(x, y, width, height, CommonComponents.optionNameValue(message, new TranslatableComponent("button.fbp.common." + value.get())), onPress);

        this.value = value;
        this.defaultMessage = message;
        this.tooltip = tooltip;

        this.active = active;
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(CommonComponents.optionNameValue(this.defaultMessage, new TranslatableComponent("button.fbp.common." + this.value.get())));
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        var i = this.getYImage(this.isHoveredOrFocused());
        this.blit(stack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

        this.renderBg(stack, Minecraft.getInstance(), mouseX, mouseY);

        var minX = this.x + 2;
        var maxX = this.x + this.getWidth() - 2;

        renderScrollingString(stack, Minecraft.getInstance().font, this.getMessage(), minX, this.y, maxX, this.y + this.getHeight(), (super.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24);

        if (this.isHoveredOrFocused())
            this.renderToolTip(stack, mouseX, mouseY);
    }

    private static void renderScrollingString(PoseStack stack, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        var width = font.width(text);

        var j = (minY + maxY - 9) / 2 + 1;
        var k = maxX - minX;

        if (width > k) {
            var l = width - k;

            var d = Util.getMillis() / 1000.0D;
            var e = Math.max(l * 0.5D, 3.0D);
            var f = Math.sin(1.5707963267948966D * Math.cos(6.283185307179586D * d / e)) / 2.0D + 0.5D;
            var g = Mth.lerp(f, 0.0D, l);

            FBPRenderHelper.enableScissor(minX, minY, maxX, maxY);
            drawString(stack, font, text, minX - (int) g, j, color);
            FBPRenderHelper.disableScissor();
        } else
            drawCenteredString(stack, font, text, (minX + maxX) / 2, j, color);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        super.updateNarration(output);

        output.add(NarratedElementType.HINT, this.tooltip);
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
