package hantonik.fbp.screen.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import lombok.Getter;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class FBPBlacklistButton extends Button {
    private static final ResourceLocation WIDGETS = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/widgets.png");

    private final boolean particle;
    @Getter
    private final boolean isBlackListed;

    public FBPBlacklistButton(boolean particle, boolean isBlacklisted, Function<Builder, Builder> builder, OnPress onPress) {
        super(builder.andThen(b -> b.size(60, 60)).apply(Button.builder(Component.empty(), onPress)));

        this.particle = particle;
        this.isBlackListed = isBlacklisted;
    }

    @Override
    public void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        RenderSystem.setShaderTexture(0, WIDGETS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        blit(stack, this.getX(), this.getY(), this.isBlackListed ? 60 : 0, 196, 60, 60);
        blit(stack, (int) (this.getX() + this.width / 2.0F - 22.5F + (this.particle ? 0.0F : 2.0F)), (int) (this.getY() + this.height / 2 - 22.5F), 256 - 45, this.particle ? 45 : 0, 45, 45);
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
