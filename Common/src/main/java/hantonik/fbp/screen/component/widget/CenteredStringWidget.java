package hantonik.fbp.screen.component.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class CenteredStringWidget extends StringWidget {
    public CenteredStringWidget(int width, int height, Component message, Font font) {
        this(0, 0, width, height, message, font);
    }

    public CenteredStringWidget(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message, font);

        this.active = false;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var font = this.getFont();
        var message = this.getMessage();

        var maxWidth = this.maxWidth > 0 ? this.maxWidth : this.getWidth();
        var textWidth = font.width(message);

        var x = this.getX() + (maxWidth - textWidth) / 2;
        var y = this.getY() + (this.getHeight() - 9) / 2;

        if (textWidth > maxWidth) {
            switch (this.textOverflow) {
                case CLAMPED -> graphics.drawString(font, this.clipText(message, maxWidth), x, y, this.getColor());
                case SCROLLING -> this.renderScrollingString(graphics, font, 2, this.getColor());
            }
        } else
            graphics.drawString(font, message.getVisualOrderText(), x, y, this.getColor());
    }
}
