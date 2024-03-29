package hantonik.fbp.screen.widget.button;

import lombok.Getter;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class FBPToggleButton extends Button {
    @Getter
    private final Supplier<Boolean> value;
    private final Component defaultMessage;

    public FBPToggleButton(int x, int y, int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Tooltip tooltip) {
        super(x, y, width, height, CommonComponents.optionNameValue(message, Component.translatable("button.fbp." + value.get())), onPress, Button.DEFAULT_NARRATION);

        this.value = value;
        this.defaultMessage = message;

        this.setTooltip(tooltip);
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(CommonComponents.optionNameValue(this.defaultMessage, Component.translatable("button.fbp." + this.value.get())));
    }
}
