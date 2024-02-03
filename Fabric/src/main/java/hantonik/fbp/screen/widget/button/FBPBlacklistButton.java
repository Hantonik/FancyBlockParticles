package hantonik.fbp.screen.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import hantonik.fbp.FancyBlockParticles;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FBPBlacklistButton extends Button {
    private static final ResourceLocation WIDGETS = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/widgets.png");

    private final boolean particle;
    @Getter
    private final boolean isBlackListed;

    public FBPBlacklistButton(int x, int y, boolean particle, boolean isBlacklisted, OnPress onPress) {
        super(x, y, 60, 60, Component.empty(), onPress, Button.DEFAULT_NARRATION);

        this.particle = particle;
        this.isBlackListed = isBlacklisted;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.blit(WIDGETS, this.getX(), this.getY(), this.isBlackListed ? 60 : 0, 196, 60, 60);
        graphics.blit(WIDGETS, (int) (this.getX() + this.width / 2.0F - 22.5F + (this.particle ? 0.0F : 2.0F)), (int) (this.getY() + this.height / 2 - 22.5F), 256 - 45, this.particle ? 45 : 0, 45, 45);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        var centerX = this.getX() + this.height / 2;
        var centerY = this.getY() + this.height / 2 - 1;

        var distance = Math.sqrt((mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY));
        var radius = (this.height - 1) / 2;

        return distance <= radius;
    }
}
