package hantonik.fbp.screen.component.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hantonik.fbp.util.FBPRenderHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IBidiTooltip;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class FBPToggleButton extends Button implements IBidiTooltip {
    @Getter
    private final Supplier<Boolean> value;
    private final ITextComponent defaultMessage;

    private final BooleanSupplier active;

    @Setter
    private ITextComponent tooltip;

    public FBPToggleButton(int width, int height, ITextComponent message, Supplier<Boolean> value, IPressable onPress, ITextComponent tooltip) {
        this(width, height, message, value, onPress, tooltip, () -> true);
    }

    public FBPToggleButton(int width, int height, ITextComponent message, Supplier<Boolean> value, IPressable onPress, ITextComponent tooltip, BooleanSupplier active) {
        this(0, 0, width, height, message, value, onPress, tooltip, active);
    }

    public FBPToggleButton(int x, int y, int width, int height, ITextComponent message, Supplier<Boolean> value, IPressable onPress, ITextComponent tooltip, BooleanSupplier active) {
        super(x, y, width, height, message.copy().append(": ").append(new TranslationTextComponent("button.fbp.common." + value.get())), onPress);

        this.value = value;
        this.defaultMessage = message;
        this.tooltip = tooltip;

        this.active = active;
    }

    @Override
    public void onPress() {
        super.onPress();

        this.setMessage(this.defaultMessage.copy().append(": ").append(new TranslationTextComponent("button.fbp.common." + this.value.get())));
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        super.active = this.active.getAsBoolean();

        Minecraft minecraft = Minecraft.getInstance();

        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = this.getYImage(this.isHovered());
        this.blit(stack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

        this.renderBg(stack, minecraft, mouseX, mouseY);

        int minX = this.x + 2;
        int maxX = this.x + this.getWidth() - 2;

        renderScrollingString(stack, minecraft.font, this.getMessage(), minX, this.y, maxX, this.y + this.getHeight(), this.getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24);

        if (this.isHovered())
            this.renderToolTip(stack, mouseX, mouseY);
    }

    private static void renderScrollingString(MatrixStack stack, FontRenderer font, ITextComponent text, int minX, int minY, int maxX, int maxY, int color) {
        int width = font.width(text);

        int j = (minY + maxY - 9) / 2 + 1;
        int k = maxX - minX;

        if (width > k) {
            int l = width - k;

            double d = Util.getMillis() / 1000.0D;
            double e = Math.max(l * 0.5D, 3.0D);
            double f = Math.sin(1.5707963267948966D * Math.cos(6.283185307179586D * d / e)) / 2.0D + 0.5D;
            double g = MathHelper.lerp(f, 0.0D, l);

            FBPRenderHelper.enableScissor(minX, minY, maxX, maxY);
            drawString(stack, font, text, minX - (int) g, j, color);
            FBPRenderHelper.disableScissor();
        } else
            drawCenteredString(stack, font, text, (minX + maxX) / 2, j, color);
    }

    @Override
    public boolean isMouseOver(double $$0, double $$1) {
        return this.visible
                && $$0 >= (double) this.x
                && $$1 >= (double) this.y
                && $$0 < (double) (this.x + this.width)
                && $$1 < (double) (this.y + this.height);
    }

    @Override
    public Optional<List<IReorderingProcessor>> getTooltip() {
        return Optional.of(Minecraft.getInstance().font.split(this.tooltip, 150));
    }
}
