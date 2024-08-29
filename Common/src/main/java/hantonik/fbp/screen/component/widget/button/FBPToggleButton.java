package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class FBPToggleButton extends Button implements TooltipAccessor {
    @Getter
    private final Supplier<Boolean> value;
    private final Component defaultMessage;
    private final Component tooltip;

    private final BooleanSupplier active;

    public FBPToggleButton(int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Component tooltip) {
        this(width, height, message, value, onPress, tooltip, () -> true);
    }

    public FBPToggleButton(int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Component tooltip, BooleanSupplier active) {
        this(0, 0, width, height, message, value, onPress, tooltip, active);
    }

    public FBPToggleButton(int x, int y, int width, int height, Component message, Supplier<Boolean> value, OnPress onPress, Component tooltip, BooleanSupplier active) {
        super(x, y, width, height, CommonComponents.optionNameValue(message, Component.translatable("button.fbp.common." + value.get())), onPress);

        this.value = value;
        this.defaultMessage = message;
        this.tooltip = tooltip;

        this.active = active;
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(CommonComponents.optionNameValue(this.defaultMessage, Component.translatable("button.fbp.common." + this.value.get())));
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        super.renderButton(stack, mouseX, mouseY, partialTick);
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
