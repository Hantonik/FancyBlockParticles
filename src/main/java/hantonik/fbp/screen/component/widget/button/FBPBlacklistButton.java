package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hantonik.fbp.FancyBlockParticles;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class FBPBlacklistButton extends Button {
    private static final ResourceLocation WIDGETS = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/widgets.png");

    private final boolean particle;
    @Getter
    private final boolean isBlackListed;

    public FBPBlacklistButton(int x, int y, boolean particle, boolean isBlacklisted, IPressable onPress) {
        super(x, y, 60, 60, StringTextComponent.EMPTY, onPress);

        this.particle = particle;
        this.isBlackListed = isBlacklisted;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getInstance().textureManager.bind(WIDGETS);

        blit(stack, this.x, this.y, this.isBlackListed ? 60 : 0, 196, 60, 60);
        blit(stack, (int) (this.x + this.width / 2.0F - 22.5F + (this.particle ? 0.0F : 2.0F)), (int) (this.y + (float) this.height / 2 - 22.5F), 256 - 45, this.particle ? 45 : 0, 45, 45);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int centerX = this.x + this.height / 2;
        int centerY = this.y + this.height / 2 - 1;

        double distance = Math.sqrt((mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY));
        int radius = (this.height - 1) / 2;

        return distance <= radius;
    }
}
