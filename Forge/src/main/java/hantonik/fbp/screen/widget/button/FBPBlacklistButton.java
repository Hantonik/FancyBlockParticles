package hantonik.fbp.screen.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import lombok.Getter;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class FBPBlacklistButton extends Button {
    private static final ResourceLocation WIDGETS = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/widgets.png");

    private final boolean particle;
    @Getter
    private final boolean isBlackListed;

    public FBPBlacklistButton(int x, int y, boolean particle, boolean isBlacklisted, OnPress onPress) {
        super(x, y, 60, 60, TextComponent.EMPTY, onPress, Button.NO_TOOLTIP);

        this.particle = particle;
        this.isBlackListed = isBlacklisted;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        blit(stack, this.x, this.y, this.isBlackListed ? 60 : 0, 196, 60, 60);
        blit(stack, (int) (this.x + this.width / 2.0F - 22.5F + (this.particle ? 0.0F : 2.0F)), (int) (this.y + this.height / 2 - 22.5F), 256 - 45, this.particle ? 45 : 0, 45, 45);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        var centerX = this.x + this.height / 2;
        var centerY = this.y + this.height / 2 - 1;

        var distance = Math.sqrt((mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY));
        var radius = (this.height - 1) / 2;

        return distance <= radius;
    }
}
