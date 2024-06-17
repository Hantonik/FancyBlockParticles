package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class FBPImageButton extends ImageButton {
    public FBPImageButton(int width, int height, ResourceLocation texture, OnPress onPress) {
        this(0, 0, width, height, texture, onPress);
    }

    public FBPImageButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress) {
        super(x, y, width, height, 0, 0, texture, onPress);
    }

    @Override
    public void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        super.renderWidget(stack, mouseX, mouseY, partialTick);

        RenderSystem.disableBlend();
    }
}
