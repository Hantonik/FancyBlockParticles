package hantonik.fbp.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.component.widget.button.FBPBlacklistButton;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class FBPFastBlacklistScreen extends Screen {
    private static final ResourceLocation WIDGETS = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/widgets.png");

    private final BlockState state;
    private final ItemStack displayStack;

    private FBPBlacklistButton animationButton;
    private FBPBlacklistButton particleButton;

    public FBPFastBlacklistScreen(BlockPos lookingAtPos) {
        super(new TranslationTextComponent("screen.fbp.fast_blacklist"));

        this.state = Minecraft.getInstance().level.getBlockState(lookingAtPos);
        this.displayStack = new ItemStack(this.state.getBlock());
    }

    public FBPFastBlacklistScreen(ItemStack heldItem) {
        super(new TranslationTextComponent("screen.fbp.fast_blacklist"));

        this.state = ((BlockItem) heldItem.getItem()).getBlock().defaultBlockState();
        this.displayStack = heldItem;
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        this.animationButton = this.addButton(new FBPBlacklistButton(x - 130, y + 5, false, !FancyBlockParticles.CONFIG.isBlockAnimationsEnabled(this.state.getBlock()), button -> {
            FancyBlockParticles.CONFIG.toggleAnimations(this.state.getBlock());

            this.onClose();
        }));

        this.particleButton = this.addButton(new FBPBlacklistButton(x + 70, y + 5, true, !FancyBlockParticles.CONFIG.isBlockParticlesEnabled(this.state.getBlock()), button -> {
            FancyBlockParticles.CONFIG.toggleParticles(this.state.getBlock());

            this.onClose();
        }));

        this.grabMouse();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.minecraft.mouseHandler.isMouseGrabbed())
            this.grabMouse();
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (FBPKeyMappings.ADD_TO_BLACKLIST.matches(keyCode, scanCode)) {
            for (IGuiEventListener widget : this.children()) {
                if (widget instanceof FBPBlacklistButton) {
                    if (((FBPBlacklistButton) widget).isHovered()) {
                        ((FBPBlacklistButton) widget).onPress();

                        return true;
                    }
                }
            }

            this.onClose();
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(stack);

        int x = this.width / 2;
        int y = this.height / 2;

        mouseX = MathHelper.clamp(mouseX, this.animationButton.x + 30, this.particleButton.x + 30);
        mouseY = y + 35;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.minecraft.textureManager.bind(WIDGETS);

        blit(stack, this.animationButton.x + 30, this.animationButton.y + 30 - 10, 0, 0, 195, 20);
        drawCenteredString(stack, this.font, new StringTextComponent("<").withStyle(this.animationButton.active ? TextFormatting.GREEN : TextFormatting.RED).append("             ").append(new StringTextComponent(">").withStyle(this.particleButton.active ? TextFormatting.GREEN : TextFormatting.RED)), this.animationButton.x + 30 + 100, this.animationButton.y + 30 - 4, 0);

        drawCenteredString(stack, this.font, this.title.copy().withStyle(TextFormatting.BOLD, TextFormatting.GREEN), x, 10, 0);

        ResourceLocation displayId = Registry.ITEM.getKey(this.displayStack.getItem());
        drawCenteredString(stack, this.font, new StringTextComponent(displayId.getNamespace()).withStyle(TextFormatting.GOLD).append(new StringTextComponent(":").withStyle(TextFormatting.RED)).append(new StringTextComponent(displayId.getPath()).withStyle(TextFormatting.GREEN)).withStyle(TextFormatting.BOLD), x, y - 19, 0);

        if (this.animationButton.isHovered()) {
            drawCenteredString(stack, this.font, new TranslationTextComponent("tooltip.fbp.animation").withStyle(TextFormatting.BOLD, TextFormatting.GREEN), this.animationButton.x + 30, this.animationButton.y + 30 - 42, 0);

            drawCenteredString(stack, this.font, this.animationButton.isBlackListed() ? new TranslationTextComponent("tooltip.fbp.remove").withStyle(TextFormatting.BOLD, TextFormatting.RED) : new TranslationTextComponent("tooltip.fbp.add").withStyle(TextFormatting.BOLD, TextFormatting.GREEN), this.animationButton.x + 30, this.animationButton.y + 30 + 35, 0);
        }

        if (this.particleButton.isHovered()) {
            drawCenteredString(stack, this.font, new TranslationTextComponent("tooltip.fbp.particles").withStyle(TextFormatting.BOLD, TextFormatting.GREEN), this.particleButton.x + 30, this.particleButton.y + 30 - 42, 0);

            drawCenteredString(stack, this.font, this.particleButton.isBlackListed() ? new TranslationTextComponent("tooltip.fbp.remove").withStyle(TextFormatting.BOLD, TextFormatting.RED) : new TranslationTextComponent("tooltip.fbp.add").withStyle(TextFormatting.BOLD, TextFormatting.GREEN), this.particleButton.x + 30, this.particleButton.y + 30 + 35, 0);
        }

        RenderSystem.pushMatrix();

        RenderSystem.translatef(x - 32.0F, y - 32.0F - 57.0F, 0.0F);
        RenderSystem.scalef(4.0F, 4.0F, 1.0F);

        this.itemRenderer.renderGuiItem(this.displayStack, 0, 0);

        RenderSystem.popMatrix();

        this.animationButton.render(stack, mouseX, mouseY, partialTick);
        this.particleButton.render(stack, mouseX, mouseY, partialTick);

        this.minecraft.textureManager.bind(WIDGETS);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        Button hovered = this.animationButton.isHovered() ? this.animationButton : (this.particleButton.isHovered() ? this.particleButton : null);
        blit(stack, mouseX - 20 / 2, mouseY - 20 / 2, hovered != null && !hovered.active ? 256 - 20 * 2 : 256 - 20, 256 - 20, 20, 20);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        FancyBlockParticles.CONFIG.save();

        super.onClose();
    }

    private void grabMouse() {
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }
}
