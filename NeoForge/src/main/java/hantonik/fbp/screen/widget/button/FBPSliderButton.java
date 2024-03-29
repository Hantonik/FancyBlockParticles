package hantonik.fbp.screen.widget.button;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FBPSliderButton extends ExtendedSlider {
    private final Consumer<FBPSliderButton> action;
    private final BooleanSupplier active;

    private final double defaultValue;

    public FBPSliderButton(int x, int y, int width, Component prefix, Component suffix, double value, double defaultValue, double minValue, double maxValue, double step, Consumer<FBPSliderButton> action, BooleanSupplier active, Tooltip tooltip) {
        super(x, y, width, 20, prefix, suffix, minValue, maxValue, value, step, 0, true);

        this.action = action;
        this.active = active;

        this.defaultValue = defaultValue;

        this.updateMessage();

        this.setTooltip(tooltip);
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
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        this.applyValue();

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void applyValue() {
        this.action.accept(this);
    }
}
