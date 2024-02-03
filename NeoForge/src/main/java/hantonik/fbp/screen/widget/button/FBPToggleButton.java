package hantonik.fbp.screen.widget.button;

import lombok.Getter;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Function;
import java.util.function.Supplier;

public class FBPToggleButton extends Button {
    @Getter
    private final Supplier<Boolean> value;
    private final Component defaultMessage;

    public FBPToggleButton(Component message, Supplier<Boolean> value, Function<Builder, Builder> builder, OnPress onPress) {
        super(builder.apply(builder(CommonComponents.optionNameValue(message, Component.translatable("button.fbp." + value.get())), onPress)));

        this.value = value;
        this.defaultMessage = message;
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(CommonComponents.optionNameValue(this.defaultMessage, Component.translatable("button.fbp." + this.value.get())));
    }
}
