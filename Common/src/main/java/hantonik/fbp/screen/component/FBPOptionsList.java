package hantonik.fbp.screen.component;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class FBPOptionsList extends ContainerObjectSelectionList<FBPOptionsList.Entry> {
    public FBPOptionsList(Minecraft minecraft, int width, int height, int headerHeight, int footerHeight) {
        super(minecraft, width, height, headerHeight, height - footerHeight, 25);

        this.centerListVertically = false;
    }

    public void addBig(AbstractWidget widget) {
        this.addEntry(Entry.create(widget, this.width));
    }

    public void addBig(AbstractWidget... widgets) {
        for (var widget : widgets)
            this.addBig(widget);
    }

    public void addSmall(AbstractWidget leftWidget, @Nullable AbstractWidget rightWidget) {
        this.addEntry(Entry.create(leftWidget, rightWidget, this.width));
    }

    public void addSmall(AbstractWidget... widgets) {
        for (var i = 0; i < widgets.length; i += 2) {
            var widget = i < widgets.length - 1 ? widgets[i + 1] : null;

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

    public Optional<AbstractWidget> getMouseOver(double mouseX, double mouseY) {
        for (var child : this.children()) {
            for (var widget : child.widgets) {
                if (widget.isMouseOver(mouseX, mouseY))
                    return Optional.of(widget);
            }
        }

        return Optional.empty();
    }

    protected static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final List<AbstractWidget> widgets;
        private final int width;

        private Entry(List<AbstractWidget> widgets, int width) {
            this.widgets = ImmutableList.copyOf(widgets);
            this.width = width;
        }

        private static Entry create(AbstractWidget widget, int width) {
            return new Entry(List.of(widget), width);
        }

        private static Entry create(AbstractWidget leftWidget, @Nullable AbstractWidget rightWidget, int width) {
            return rightWidget == null ? new Entry(List.of(leftWidget), width) : new Entry(List.of(leftWidget, rightWidget), width);
        }

        @Override
        public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            var i = 0;
            var j = this.width / 2 - 155;

            for (AbstractWidget widget : this.widgets) {
                widget.x = i + j;
                widget.y = top;

                widget.render(stack, mouseX, mouseY, partialTick);

                i += 160;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.widgets;
        }
    }
}
