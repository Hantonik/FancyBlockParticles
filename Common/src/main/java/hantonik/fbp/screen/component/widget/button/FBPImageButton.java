package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class FBPImageButton extends ImageButton implements TooltipAccessor {
    private final Component tooltip;

    public FBPImageButton(int width, int height, ResourceLocation texture, OnPress onPress, Component tooltip) {
        this(0, 0, width, height, texture, onPress, tooltip);
    }

    public FBPImageButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress, Component tooltip) {
        super(x, y, width, height, 0, 0, texture, onPress);

        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        super.renderButton(stack, mouseX, mouseY, partialTick);

        RenderSystem.disableBlend();
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
