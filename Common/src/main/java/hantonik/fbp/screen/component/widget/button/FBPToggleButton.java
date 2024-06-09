package hantonik.fbp.screen.component.widget.button;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class FBPToggleButton extends Button {
    @Getter
    private final Supplier<Boolean> value;
    private final Component defaultMessage;

    private final BooleanSupplier active;

    public FBPToggleButton(int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Tooltip tooltip) {
        this(width, height, message, value, onPress, tooltip, () -> true);
    }

    public FBPToggleButton(int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Tooltip tooltip, BooleanSupplier active) {
        this(0, 0, width, height, message, value, onPress, tooltip, active);
    }

    public FBPToggleButton(int x, int y, int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Tooltip tooltip, BooleanSupplier active) {
        super(x, y, width, height, CommonComponents.optionNameValue(message, Component.translatable("button.fbp.common." + value.get())), onPress, Button.DEFAULT_NARRATION);

        this.value = value;
        this.defaultMessage = message;

        this.active = active;

        this.setTooltip(tooltip);
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(CommonComponents.optionNameValue(this.defaultMessage, Component.translatable("button.fbp.common." + this.value.get())));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}
