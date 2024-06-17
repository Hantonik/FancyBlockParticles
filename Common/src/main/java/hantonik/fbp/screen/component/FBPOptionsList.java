package hantonik.fbp.screen.component;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.screen.FBPAbstractOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FBPOptionsList extends ContainerObjectSelectionList<FBPOptionsList.Entry> {
    private final FBPAbstractOptionsScreen screen;

    public FBPOptionsList(Minecraft minecraft, int width, int height, FBPAbstractOptionsScreen screen) {
        super(minecraft, width, height, screen.layout.getHeaderHeight(), height - screen.layout.getFooterHeight(), 25);

        this.centerListVertically = false;
        this.screen = screen;
    }

    public void addBig(AbstractWidget widget) {
        this.addEntry(Entry.create(widget, this.screen));
    }

    public void addBig(AbstractWidget... widgets) {
        for (var widget : widgets)
            this.addBig(widget);
    }

    public void addSmall(AbstractWidget leftWidget, @Nullable AbstractWidget rightWidget) {
        this.addEntry(Entry.create(leftWidget, rightWidget, this.screen));
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

    protected static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final List<AbstractWidget> widgets;
        private final Screen screen;

        private Entry(List<AbstractWidget> widgets, FBPAbstractOptionsScreen screen) {
            this.widgets = ImmutableList.copyOf(widgets);
            this.screen = screen;
        }

        private static Entry create(AbstractWidget widget, FBPAbstractOptionsScreen screen) {
            return new Entry(List.of(widget), screen);
        }

        private static Entry create(AbstractWidget leftWidget, @Nullable AbstractWidget rightWidget, FBPAbstractOptionsScreen screen) {
            return rightWidget == null ? new Entry(List.of(leftWidget), screen) : new Entry(List.of(leftWidget, rightWidget), screen);
        }

        @Override
        public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            var i = 0;
            var j = this.screen.width / 2 - 155;

            for (AbstractWidget widget : this.widgets) {
                widget.setPosition(i + j, top);
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
