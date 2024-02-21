package hantonik.fbp.screen.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FBPSliderButton extends AbstractSliderButton {
    private final Component prefix;
    private final Component suffix;

    private final double minValue;
    private final double maxValue;

    private final double defaultValue;
    private final double stepSize;

    private final Consumer<FBPSliderButton> action;
    private final BooleanSupplier active;

    private final OnTooltip onTooltip;

    private final DecimalFormat format;

    public FBPSliderButton(int x, int y, int width, Component prefix, Component suffix, double value, double defaultValue, double minValue, double maxValue, double step, Consumer<FBPSliderButton> action, BooleanSupplier active, OnTooltip onTooltip) {
        super(x, y, width, 20, TextComponent.EMPTY, 0D);

        this.prefix = prefix;
        this.suffix = suffix;

        this.minValue = minValue;
        this.maxValue = maxValue;

        this.action = action;
        this.active = active;

        this.onTooltip = onTooltip;

        this.stepSize = Math.abs(step);
        this.value = this.snapToNearest((value - minValue) / (maxValue - minValue));
        this.defaultValue = defaultValue;

        this.format = step == 0.0D || this.stepSize == Math.floor(this.stepSize) ? new DecimalFormat("0") : new DecimalFormat(String.valueOf(this.stepSize).replaceAll("\\d", "0"));

        this.updateMessage();
    }

    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
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

    private void setSliderValue(double value) {
        var oldValue = this.value;
        this.value = this.snapToNearest(value);

        if (oldValue != this.value)
            this.applyValue();

        this.updateMessage();
    }

    private double snapToNearest(double value) {
        if (this.stepSize <= 0D)
            return Mth.clamp(value, 0D, 1D);

        value = Mth.lerp(Mth.clamp(value, 0D, 1D), this.minValue, this.maxValue);
        value = (this.stepSize * Math.round(value / this.stepSize));
        value = this.minValue > this.maxValue ? Mth.clamp(value, this.maxValue, this.minValue) : Mth.clamp(value, this.minValue, this.maxValue);

        return Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.prefix.copy().append(new TextComponent(this.getValueString()).append(this.suffix).withStyle(Math.abs(this.getValue() - this.defaultValue) < this.stepSize ? ChatFormatting.DARK_AQUA : ChatFormatting.AQUA)));
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        this.applyValue();

        super.renderButton(stack, mouseX, mouseY, partialTick);

        if (this.isHoveredOrFocused())
            this.renderToolTip(stack, mouseX, mouseY);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        this.onTooltip.onTooltip(this, poseStack, mouseX, mouseY);
    }

    @Override
    public void applyValue() {
        this.action.accept(this);
    }

    @Environment(EnvType.CLIENT)
    public interface OnTooltip {
        void onTooltip(Widget widget, PoseStack stack, int mouseX, int mouseY);
    }
}
