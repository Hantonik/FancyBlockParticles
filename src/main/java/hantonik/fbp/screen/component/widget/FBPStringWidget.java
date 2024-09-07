package hantonik.fbp.screen.component.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

public class FBPStringWidget extends Widget {
    private float alignX = 0.5F;

    @Getter(AccessLevel.PROTECTED)
    private final FontRenderer font;
    @Getter(AccessLevel.PROTECTED)
    private int color = 16777215;

    public FBPStringWidget(int width, int height, ITextComponent message, FontRenderer font) {
        this(0, 0, width, height, message, font);
    }

    public FBPStringWidget(int x, int y, int width, int height, ITextComponent message, FontRenderer font) {
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
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        ITextComponent message = this.getMessage();

        int x = this.x + Math.round(this.alignX * (float) (this.width - this.font.width(message)));
        int y = this.y + (this.height - 9) / 2;

        this.font.drawShadow(stack, message, x, y, this.color);
    }
}
