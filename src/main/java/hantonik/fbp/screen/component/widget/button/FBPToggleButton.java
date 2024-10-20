package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IBidiTooltip;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class FBPToggleButton extends Button implements IBidiTooltip {
    @Getter
    private final Supplier<Boolean> value;
    private final ITextComponent defaultMessage;

    private final BooleanSupplier active;

    @Setter
    private ITextComponent tooltip;

    public FBPToggleButton(int width, int height, ITextComponent message, Supplier<Boolean> value, IPressable onPress, ITextComponent tooltip) {
        this(width, height, message, value, onPress, tooltip, () -> true);
    }

    public FBPToggleButton(int width, int height, ITextComponent message, Supplier<Boolean> value, IPressable onPress, ITextComponent tooltip, BooleanSupplier active) {
        this(0, 0, width, height, message, value, onPress, tooltip, active);
    }

    public FBPToggleButton(int x, int y, int width, int height, ITextComponent message, Supplier<Boolean> value, IPressable onPress, ITextComponent tooltip, BooleanSupplier active) {
        super(x, y, width, height, message.copy().append(": ").append(new TranslationTextComponent("button.fbp.common." + value.get())), onPress);

        this.value = value;
        this.defaultMessage = message;
        this.tooltip = tooltip;

        this.active = active;
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(this.defaultMessage.copy().append(": ").append(new TranslationTextComponent("button.fbp.common." + this.value.get())));
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        super.renderButton(stack, mouseX, mouseY, partialTick);
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
