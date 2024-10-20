package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hantonik.fbp.util.FBPRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IBidiTooltip;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FBPSliderButton extends AbstractSlider implements IBidiTooltip {
    private final ITextComponent prefix;
    private final ITextComponent suffix;

    private final double minValue;
    private final double maxValue;

    private final double defaultValue;
    private final double stepSize;

    private final ITextComponent tooltip;

    private final Consumer<FBPSliderButton> action;
    private final BooleanSupplier active;

    private final DecimalFormat format;

    public FBPSliderButton(int width, int height, ITextComponent prefix, ITextComponent suffix, double value, double defaultValue, double minValue, double maxValue, double step, Consumer<FBPSliderButton> action, BooleanSupplier active, ITextComponent tooltip) {
        this(0, 0, width, height, prefix, suffix, value, defaultValue, minValue, maxValue, step, action, active, tooltip);
    }

    public FBPSliderButton(int x, int y, int width, int height, ITextComponent prefix, ITextComponent suffix, double value, double defaultValue, double minValue, double maxValue, double step, Consumer<FBPSliderButton> action, BooleanSupplier active, ITextComponent tooltip) {
        super(x, y, width, height, StringTextComponent.EMPTY, 0D);

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
        boolean flag = keyCode == GLFW.GLFW_KEY_LEFT;

        if (flag || keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.minValue > this.maxValue)
                flag = !flag;

            float f = flag ? -1.0F : 1.0F;

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
        double oldValue = this.value;
        this.value = this.snapToNearest(value);

        if (oldValue != this.value)
            this.applyValue();

        this.updateMessage();
    }

    public double snapToNearest(double value) {
        if (this.stepSize <= 0D)
            return MathHelper.clamp(value, 0D, 1D);

        value = MathHelper.lerp(MathHelper.clamp(value, 0D, 1D), this.minValue, this.maxValue);
        value = (this.stepSize * Math.round(value / this.stepSize));
        value = this.minValue > this.maxValue ? MathHelper.clamp(value, this.maxValue, this.minValue) : MathHelper.clamp(value, this.minValue, this.maxValue);

        return MathHelper.lerp(MathHelper.inverseLerp(value, this.minValue, this.maxValue), 0.0D, 1.0D);
    }

    @Override
    public void updateMessage() {
        this.setMessage(this.prefix.copy().append(new StringTextComponent(this.getValueString()).append(this.suffix).withStyle(Math.abs(this.getValue() - this.defaultValue) < this.stepSize ? TextFormatting.DARK_AQUA : TextFormatting.AQUA)));
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        this.applyValue();

        Minecraft minecraft = Minecraft.getInstance();

        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = this.getYImage(this.isHovered());
        this.blit(stack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

        this.renderBg(stack, minecraft, mouseX, mouseY);

        int minX = this.x + 2;
        int maxX = this.x + this.getWidth() - 2;

        renderScrollingString(stack, minecraft.font, this.getMessage(), minX, this.y, maxX, this.y + this.getHeight(), this.getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    private static void renderScrollingString(MatrixStack stack, FontRenderer font, ITextComponent text, int minX, int minY, int maxX, int maxY, int color) {
        int width = font.width(text);

        int j = (minY + maxY - 9) / 2 + 1;
        int k = maxX - minX;

        if (width > k) {
            int l = width - k;

            double d = Util.getMillis() / 1000.0D;
            double e = Math.max(l * 0.5D, 3.0D);
            double f = Math.sin(1.5707963267948966D * Math.cos(6.283185307179586D * d / e)) / 2.0D + 0.5D;
            double g = MathHelper.lerp(f, 0.0D, l);

            FBPRenderHelper.enableScissor(minX, minY, maxX, maxY);
            drawString(stack, font, text, minX - (int) g, j, color);
            FBPRenderHelper.disableScissor();
        } else
            drawCenteredString(stack, font, text, (minX + maxX) / 2, j, color);
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
    public Optional<List<IReorderingProcessor>> getTooltip() {
        return Optional.of(Minecraft.getInstance().font.split(this.tooltip, 150));
    }
}
