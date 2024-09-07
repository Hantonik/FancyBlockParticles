package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IBidiTooltip;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Optional;

public class FBPImageButton extends ImageButton implements IBidiTooltip {
    private final ITextComponent tooltip;

    public FBPImageButton(int width, int height, ResourceLocation texture, IPressable onPress, ITextComponent tooltip) {
        this(0, 0, width, height, texture, onPress, tooltip);
    }

    public FBPImageButton(int x, int y, int width, int height, ResourceLocation texture, IPressable onPress, ITextComponent tooltip) {
        super(x, y, width, height, 0, 0, height, texture, onPress);

        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        super.renderButton(stack, mouseX, mouseY, partialTick);

        RenderSystem.disableBlend();
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
