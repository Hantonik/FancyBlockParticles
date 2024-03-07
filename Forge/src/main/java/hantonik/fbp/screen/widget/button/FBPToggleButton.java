package hantonik.fbp.screen.widget.button;

import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class FBPToggleButton extends Button {
    @Getter
    private final Supplier<Boolean> value;
    private final Component defaultMessage;

    public FBPToggleButton(int x, int y, int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, height, CommonComponents.optionNameValue(message, Component.translatable("button.fbp." + value.get()).withStyle(value.get() ? ChatFormatting.GREEN : ChatFormatting.RED)), onPress, onTooltip);

        this.value = value;
        this.defaultMessage = message;
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(CommonComponents.optionNameValue(this.defaultMessage, Component.translatable("button.fbp." + this.value.get()).withStyle(this.value.get() ? ChatFormatting.GREEN : ChatFormatting.RED)));
    }
}
