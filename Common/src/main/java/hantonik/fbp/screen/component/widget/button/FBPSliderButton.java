package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FBPSliderButton extends AbstractSliderButton implements TooltipAccessor {
    private final Component prefix;
    private final Component suffix;

    private final double minValue;
    private final double maxValue;

    private final double defaultValue;
    private final double stepSize;

    private final Component tooltip;

    private final Consumer<FBPSliderButton> action;
    private final BooleanSupplier active;

    private final DecimalFormat format;

    public FBPSliderButton(int width, int height, Component prefix, Component suffix, double value, double defaultValue, double minValue, double maxValue, double step, Consumer<FBPSliderButton> action, BooleanSupplier active, Component tooltip) {
        this(0, 0, width, height, prefix, suffix, value, defaultValue, minValue, maxValue, step, action, active, tooltip);
    }

    public FBPSliderButton(int x, int y, int width, int height, Component prefix, Component suffix, double value, double defaultValue, double minValue, double maxValue, double step, Consumer<FBPSliderButton> action, BooleanSupplier active, Component tooltip) {
        super(x, y, width, height, Component.empty(), 0D);

        this.prefix = prefix;
        this.suffix = suffix;

        this.minValue = minValue;
        this.maxValue = maxValue;

        this.action = action;
        this.active = active;

        this.stepSize = Math.abs(step);
        this.value = this.snapToNearest((value - minValue) / (maxValue - minValue));
        this.defaultValue = defaultValue;

        this.tooltip = tooltip;

        this.format = step == 0.0D || this.stepSize == Math.floor(this.stepSize) ? new DecimalFormat("0") : new DecimalFormat(String.valueOf(this.stepSize).replaceAll("\\d", "0"));

        this.updateMessage();
    }

    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    public float getValueFloat() {
        return (float) this.getValue();
    }

    public long getValueLong() {
        return Math.round(this.getValue());
    }

    public int getValueInt() {
        return (int) this.getValueLong();
    }

    public String getValueString() {
        return this.format.format(this.getValue());
    }

    public void setValue(double value) {
        this.value = this.snapToNearest((value - this.minValue) / (this.maxValue - this.minValue));

        this.updateMessage();
        this.applyValue();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);

        this.setValueFromMouse(mouseX);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var flag = keyCode == GLFW.GLFW_KEY_LEFT;

        if (flag || keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.minValue > this.maxValue)
                flag = !flag;

            var f = flag ? -1.0F : 1.0F;

            if (this.stepSize <= 0.0D)
                this.setSliderValue(this.value + (f / (this.width - 8)));
            else
                this.setValue(this.getValue() + f * this.stepSize);
        }

        return false;
    }

    private void setValueFromMouse(double mouseX) {
        this.setSliderValue((mouseX - (this.x + 4)) / (this.width - 8));
    }

    public void setSliderValue(double value) {
        var oldValue = this.value;
        this.value = this.snapToNearest(value);

        if (oldValue != this.value)
            this.applyValue();

        this.updateMessage();
    }

    public double snapToNearest(double value) {
        if (this.stepSize <= 0D)
            return Mth.clamp(value, 0D, 1D);

        value = Mth.lerp(Mth.clamp(value, 0D, 1D), this.minValue, this.maxValue);
        value = (this.stepSize * Math.round(value / this.stepSize));
        value = this.minValue > this.maxValue ? Mth.clamp(value, this.maxValue, this.minValue) : Mth.clamp(value, this.minValue, this.maxValue);

        return Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
    }

    @Override
    public void updateMessage() {
        this.setMessage(this.prefix.copy().append(Component.literal(this.getValueString()).append(this.suffix).withStyle(Math.abs(this.getValue() - this.defaultValue) < this.stepSize ? ChatFormatting.DARK_AQUA : ChatFormatting.AQUA)));
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        this.applyValue();

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

            enableScissor(minX, minY, maxX, maxY);
            drawString(stack, font, text, minX - (int) g, j, color);
            disableScissor();
        } else
            drawCenteredString(stack, font, text, (minX + maxX) / 2, j, color);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        super.updateNarration(output);

        output.add(NarratedElementType.HINT, this.tooltip);
    }

    @Override
    public void applyValue() {
        this.action.accept(this);
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
