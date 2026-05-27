package hantonik.fbp.screen.component.widget;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class CenteredStringWidget extends StringWidget {
    public CenteredStringWidget(int width, int height, Component message, Font font) {
        this(0, 0, width, height, message, font);
    }

    public CenteredStringWidget(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message, font);

        this.maxWidth = width;
        this.active = false;
    }

    @Override
    public void visitLines(ActiveTextCollector collector) {
        var font = this.getFont();
        var message = this.getMessage();

        var maxWidth = this.maxWidth > 0 ? this.maxWidth : this.getWidth();
        var messageWidth = font.width(message);

        var x = this.getX() + maxWidth / 2;
        var y = this.getY() + (this.getHeight() - 9) / 2;

        if (messageWidth > maxWidth) {
            switch (this.textOverflow) {
                case CLAMPED -> collector.accept(x, y, ComponentRenderUtils.clipText(message, font, maxWidth));
                case SCROLLING -> this.extractScrollingStringOverContents(collector, message, 2);
            }
        } else
            collector.accept(TextAlignment.CENTER, x, y, message.getVisualOrderText());
    }
}
