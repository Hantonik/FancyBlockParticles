package hantonik.fbp.screen.component.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class FBPStringWidget extends AbstractWidget {
    private float alignX = 0.5F;

    @Getter(AccessLevel.PROTECTED)
    private final Font font;
    @Getter(AccessLevel.PROTECTED)
    private int color = 16777215;

    public FBPStringWidget(int width, int height, Component message, Font font) {
        this(0, 0, width, height, message, font);
    }

    public FBPStringWidget(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message);

        this.active = false;

        this.font = font;
    }

    private FBPStringWidget horizontalAlignment(float alignX) {
        this.alignX = alignX;

        return this;
    }

    public FBPStringWidget alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public FBPStringWidget alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public FBPStringWidget alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    public FBPStringWidget setColor(int color) {
        this.color = color;

        return this;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        var message = this.getMessage();

        var x = this.x + Math.round(this.alignX * (float) (this.width - this.font.width(message)));
        var y = this.y + (this.height - 9) / 2;

        this.font.draw(stack, message, x, y, this.color);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }
}
