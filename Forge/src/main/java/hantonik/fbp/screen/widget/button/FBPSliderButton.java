package hantonik.fbp.screen.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FBPSliderButton extends ForgeSlider {
    private final Consumer<FBPSliderButton> action;
    private final BooleanSupplier active;

    private final OnTooltip onTooltip;

    private final double defaultValue;

    public FBPSliderButton(int x, int y, int width, Component prefix, Component suffix, double value, double defaultValue, double minValue, double maxValue, double step, Consumer<FBPSliderButton> action, BooleanSupplier active, OnTooltip onTooltip) {
        super(x, y, width, 20, prefix, suffix, minValue, maxValue, value, step, 0, true);

        this.action = action;
        this.active = active;

        this.onTooltip = onTooltip;

        this.defaultValue = defaultValue;

        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.drawString ? this.prefix.copy().append(Component.literal(this.getValueString()).append(this.suffix).withStyle(Math.abs(this.getValue() - this.defaultValue) < this.stepSize ? ChatFormatting.DARK_AQUA : ChatFormatting.AQUA)) : Component.empty());
    }

    @Override
    public void setValue(double value) {
        super.setValue(value);

        this.applyValue();
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
    protected void applyValue() {
        this.action.accept(this);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnTooltip {
        void onTooltip(Widget widget, PoseStack stack, int mouseX, int mouseY);
    }
}
