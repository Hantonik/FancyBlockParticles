package hantonik.fbp.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.widget.button.FBPBlacklistButton;
import hantonik.fbp.util.DelayedSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class FBPBlacklistScreen extends Screen {
    private static final ResourceLocation WIDGETS = new ResourceLocation(FancyBlockParticles.MOD_ID, "textures/gui/widgets.png");

    private final BlockState state;
    private final ItemStack displayStack;

    private final DelayedSupplier<FBPBlacklistButton> animationButton = new DelayedSupplier<>();
    private final DelayedSupplier<FBPBlacklistButton> particleButton = new DelayedSupplier<>();

    public FBPBlacklistScreen(BlockPos lookingAtPos) {
        super(new TranslatableComponent("screen.fbp.blacklist"));

        this.state = Minecraft.getInstance().level.getBlockState(lookingAtPos);
        this.displayStack = new ItemStack(this.state.getBlock());
    }

    public FBPBlacklistScreen(ItemStack heldItem) {
        super(new TranslatableComponent("screen.fbp.blacklist"));

        this.state = ((BlockItem) heldItem.getItem()).getBlock().defaultBlockState();
        this.displayStack = heldItem;
    }

    @Override
    protected void init() {
        var x = this.width / 2;
        var y = this.height / 2;

        this.animationButton.setSupplier(() -> new FBPBlacklistButton(x - 130, y + 5, false, !FancyBlockParticles.CONFIG.isBlockAnimationsEnabled(this.state.getBlock()), button -> {
            FancyBlockParticles.CONFIG.toggleAnimations(this.state.getBlock());

            this.onClose();
        }));

        this.particleButton.setSupplier(() -> new FBPBlacklistButton(x + 70, y + 5, true, !FancyBlockParticles.CONFIG.isBlockParticlesEnabled(this.state.getBlock()), button -> {
            FancyBlockParticles.CONFIG.toggleParticles(this.state.getBlock());

            this.onClose();
        }));

        this.addRenderableWidget(this.animationButton.get());
        this.addRenderableWidget(this.particleButton.get());

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
        if (FBPKeyMappings.FAST_BLACKLIST.matches(keyCode, scanCode)) {
            for (var widget : this.children()) {
                if (widget instanceof FBPBlacklistButton button) {
                    if (button.isHoveredOrFocused()) {
                        button.onPress();

                        return true;
                    }
                }
            }

            this.onClose();
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        this.renderBackground(stack);

        var x = this.width / 2;
        var y = this.height / 2;

        mouseX = Mth.clamp(mouseX, this.animationButton.get().x + 30, this.particleButton.get().x + 30);
        mouseY = y + 35;

        blit(stack, this.animationButton.get().x + 30, this.animationButton.get().y + 30 - 10, 0, 0, 195, 20);
        drawCenteredString(stack, this.font, new TextComponent("<").withStyle(this.animationButton.get().active ? ChatFormatting.GREEN : ChatFormatting.RED).append("             ").append(new TextComponent(">").withStyle(this.particleButton.get().active ? ChatFormatting.GREEN : ChatFormatting.RED)), this.animationButton.get().x + 30 + 100, this.animationButton.get().y + 30 - 4, 0);

        drawCenteredString(stack, this.font, this.title.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), x, 10, 0);

        var displayId = Registry.ITEM.getKey(this.displayStack.getItem());
        drawCenteredString(stack, this.font, new TextComponent(displayId.getNamespace()).withStyle(ChatFormatting.GOLD).append(new TextComponent(":").withStyle(ChatFormatting.RED)).append(new TextComponent(displayId.getPath()).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.BOLD), x, y - 19, 0);

        if (this.animationButton.get().isMouseOver(mouseX, mouseY)) {
            drawCenteredString(stack, this.font, new TranslatableComponent("tooltip.fbp.animation").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), this.animationButton.get().x + 30, this.animationButton.get().y + 30 - 42, 0);

            drawCenteredString(stack, this.font, this.animationButton.get().isBlackListed() ? new TranslatableComponent("tooltip.fbp.remove").withStyle(ChatFormatting.BOLD, ChatFormatting.RED) : new TranslatableComponent("tooltip.fbp.add").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), this.animationButton.get().x + 30, this.animationButton.get().y + 30 + 35, 0);
        }

        if (this.particleButton.get().isMouseOver(mouseX, mouseY)) {
            drawCenteredString(stack, this.font, new TranslatableComponent("tooltip.fbp.particles").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), this.particleButton.get().x + 30, this.particleButton.get().y + 30 - 42, 0);

            drawCenteredString(stack, this.font, this.particleButton.get().isBlackListed() ? new TranslatableComponent("tooltip.fbp.remove").withStyle(ChatFormatting.BOLD, ChatFormatting.RED) : new TranslatableComponent("tooltip.fbp.add").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), this.particleButton.get().x + 30, this.particleButton.get().y + 30 + 35, 0);
        }

        RenderSystem.getModelViewStack().pushPose();

        RenderSystem.getModelViewStack().translate(x - 32, y - 32 - 57.0, 0.0F);
        RenderSystem.getModelViewStack().scale(4.0F, 4.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();

        this.itemRenderer.renderGuiItem(this.displayStack, 0, 0);

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        super.render(stack, mouseX, mouseY, partialTick);

        var mouseOver = this.animationButton.get().isMouseOver(mouseX, mouseY) ? this.animationButton.get() : (this.particleButton.get().isMouseOver(mouseX, mouseY) ? this.particleButton.get() : null);
        blit(stack, mouseX - 20 / 2, mouseY - 20 / 2, mouseOver != null && !mouseOver.active ? 256 - 20 * 2 : 256 - 20, 256 - 20, 20, 20);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void grabMouse() {
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }
}
