package hantonik.fbp.screen.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class FBPOptionsList extends AbstractOptionList<FBPOptionsList.Entry> {
    public FBPOptionsList(Minecraft minecraft, int width, int height, int headerHeight, int footerHeight) {
        super(minecraft, width, height, headerHeight, height - footerHeight, 25);

        this.centerListVertically = false;
    }

    public void addBig(Widget widget) {
        this.addEntry(Entry.create(widget, this.width));
    }

    public void addBig(Widget... widgets) {
        for (Widget widget : widgets)
            this.addBig(widget);
    }

    public void addSmall(Widget leftWidget, @Nullable Widget rightWidget) {
        this.addEntry(Entry.create(leftWidget, rightWidget, this.width));
    }

    public void addSmall(Widget... widgets) {
        for (int i = 0; i < widgets.length; i += 2) {
            Widget widget = i < widgets.length - 1 ? widgets[i + 1] : null;

            this.addSmall(widgets[i], widget);
        }
    }

    @Override
    public int getRowWidth() {
        return 400;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }

    public Optional<Widget> getMouseOver(double mouseX, double mouseY) {
        for (Entry child : this.children()) {
            for (Widget widget : child.widgets) {
                if (widget.isMouseOver(mouseX, mouseY))
                    return Optional.of(widget);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<IGuiEventListener> getChildAt(double pMouseX, double pMouseY) {
        return super.getChildAt(pMouseX, pMouseY);
    }

    protected static class Entry extends AbstractOptionList.Entry<Entry> {
        private final List<Widget> widgets;
        private final int width;

        private Entry(List<Widget> widgets, int width) {
            this.widgets = ImmutableList.copyOf(widgets);
            this.width = width;
        }

        private static Entry create(Widget widget, int width) {
            return new Entry(Lists.newArrayList(widget), width);
        }

        private static Entry create(Widget leftWidget, @Nullable Widget rightWidget, int width) {
            return rightWidget == null ? new Entry(Lists.newArrayList(leftWidget), width) : new Entry(Lists.newArrayList(leftWidget, rightWidget), width);
        }

        @Override
        public void render(MatrixStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int i = 0;
            int j = this.width / 2 - 155;

            for (Widget widget : this.widgets) {
                widget.x = i + j;
                widget.y = top;

                widget.render(stack, mouseX, mouseY, partialTick);

                i += 160;
            }
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return this.widgets;
        }
    }
}
