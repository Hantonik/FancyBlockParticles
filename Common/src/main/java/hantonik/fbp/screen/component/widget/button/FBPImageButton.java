package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public class FBPImageButton extends ImageButton {
    public FBPImageButton(int width, int height, WidgetSprites sprites, OnPress onPress, Component message) {
        super(width, height, sprites, onPress, message);
    }

    public FBPImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress, Component message) {
        super(x, y, width, height, sprites, onPress, message);
    }

    public FBPImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
        super(x, y, width, height, sprites, onPress);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        RenderSystem.disableBlend();
    }
}
