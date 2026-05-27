package hantonik.fbp.screen.component.widget.button;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.BlacklistMode;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public class FBPBlacklistButton extends Button {
    private static final WidgetSprites OPTION_SPRITE = new WidgetSprites(
            Identifier.tryBuild(FancyBlockParticles.MOD_ID, "blacklist/placing_animation_button"),
            Identifier.tryBuild(FancyBlockParticles.MOD_ID, "blacklist/breaking_particles_button")
    );

    private final boolean particle;
    @Getter
    private final BlacklistMode blacklistMode;

    public FBPBlacklistButton(int x, int y, boolean particle, BlacklistMode blacklistMode, OnPress onPress) {
        super(x, y, 60, 60, Component.empty(), onPress, Button.DEFAULT_NARRATION);

        this.particle = particle;
        this.blacklistMode = blacklistMode;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.tryBuild(FancyBlockParticles.MOD_ID, "blacklist/button_background_" + this.blacklistMode.name().toLowerCase(Locale.ENGLISH)), this.getX(), this.getY(), 60, 60);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, OPTION_SPRITE.get(true, this.particle), (int) (this.getX() + this.width / 2.0F - 22.5F + (this.particle ? 0.0F : 2.0F)), (int) (this.getY() + (float) this.height / 2 - 22.5F), 45, 45);
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
