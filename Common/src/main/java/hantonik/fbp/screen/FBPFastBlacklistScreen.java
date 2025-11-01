package hantonik.fbp.screen;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.component.widget.button.FBPBlacklistButton;
import hantonik.fbp.util.BlacklistMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

public class FBPFastBlacklistScreen extends Screen {
    private static final WidgetSprites INDICATOR_SPRITES = new WidgetSprites(
            ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "blacklist/indicator"),
            ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "blacklist/indicator_inactive")
    );
    private static final ResourceLocation INDICATOR_BACKGROUND_SPRITE = ResourceLocation.tryBuild(FancyBlockParticles.MOD_ID, "blacklist/indicator_background");

    private final BlockState state;
    private final ItemStack displayStack;

    private FBPBlacklistButton animationButton;
    private FBPBlacklistButton particleButton;

    public FBPFastBlacklistScreen(BlockPos lookingAtPos) {
        super(Component.translatable("screen.fbp.fast_blacklist"));

        this.state = Minecraft.getInstance().level.getBlockState(lookingAtPos);
        this.displayStack = new ItemStack(this.state.getBlock());
    }

    public FBPFastBlacklistScreen(ItemStack heldItem) {
        super(Component.translatable("screen.fbp.fast_blacklist"));

        this.state = ((BlockItem) heldItem.getItem()).getBlock().defaultBlockState();
        this.displayStack = heldItem;
    }

    @Override
    protected void init() {
        var x = this.width / 2;
        var y = this.height / 2;

        this.animationButton = this.addRenderableWidget(new FBPBlacklistButton(x - 130, y + 5, false, FancyBlockParticles.CONFIG.isBlockAnimationsEnabled(this.state.getBlock()) ? BlacklistMode.FANCY : BlacklistMode.BLACKLISTED, button -> {
            FancyBlockParticles.CONFIG.toggleAnimations(this.state.getBlock());

            this.onClose();
        }));

        this.particleButton = this.addRenderableWidget(new FBPBlacklistButton(x + 70, y + 5, true, FancyBlockParticles.CONFIG.getBlockParticlesMode(this.state.getBlock()), button -> {
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
    public boolean keyReleased(KeyEvent event) {
        if (FBPKeyMappings.ADD_TO_BLACKLIST.matches(event)) {
            for (var widget : this.children()) {
                if (widget instanceof FBPBlacklistButton button) {
                    if (button.isHovered()) {
                        button.onPress(event);

                        return true;
                    }
                }
            }

            this.onClose();
        }

        return super.keyReleased(event);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderMenuBackground(graphics);

        var x = this.width / 2;
        var y = this.height / 2;

        mouseX = Mth.clamp(mouseX, this.animationButton.getX() + 30, this.particleButton.getX() + 30);
        mouseY = y + 35;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INDICATOR_BACKGROUND_SPRITE, this.animationButton.getX() + 30, this.animationButton.getY() + 30 - 10, 200, 20);
        graphics.drawCenteredString(this.font, Component.literal("<").withStyle(this.animationButton.active ? ChatFormatting.GREEN : ChatFormatting.RED).append("             ").append(Component.literal(">").withStyle(this.particleButton.active ? ChatFormatting.GREEN : ChatFormatting.RED)), this.animationButton.getX() + 30 + 100, this.animationButton.getY() + 30 - 4, -1);

        graphics.drawCenteredString(this.font, this.title.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), x, 10, -1);

        var displayId = BuiltInRegistries.ITEM.getKey(this.displayStack.getItem());
        graphics.drawCenteredString(this.font, Component.literal(displayId.getNamespace()).withStyle(ChatFormatting.GOLD).append(Component.literal(":").withStyle(ChatFormatting.RED)).append(Component.literal(displayId.getPath()).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.BOLD), x, y - 19, -1);

        if (this.animationButton.isHovered()) {
            graphics.drawCenteredString(this.font, Component.translatable("tooltip.fbp.animation").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), this.animationButton.getX() + 30, this.animationButton.getY() + 30 - 42, -1);

            graphics.drawCenteredString(this.font, this.animationButton.getBlacklistMode() == BlacklistMode.FANCY ? Component.translatable("tooltip.fbp.fancy").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN) : Component.translatable("tooltip.fbp.disabled").withStyle(ChatFormatting.BOLD, ChatFormatting.RED), this.animationButton.getX() + 30, this.animationButton.getY() + 30 + 35, -1);
        }

        if (this.particleButton.isHovered()) {
            graphics.drawCenteredString(this.font, Component.translatable("tooltip.fbp.particles").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), this.particleButton.getX() + 30, this.particleButton.getY() + 30 - 42, -1);

            switch (this.particleButton.getBlacklistMode()) {
                case FANCY -> graphics.drawCenteredString(this.font, Component.translatable("tooltip.fbp.fancy").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), this.particleButton.getX() + 30, this.particleButton.getY() + 30 + 35, -1);
                case VANILLA -> graphics.drawCenteredString(this.font, Component.translatable("tooltip.fbp.vanilla").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), this.particleButton.getX() + 30, this.particleButton.getY() + 30 + 35, -1);
                case BLACKLISTED -> graphics.drawCenteredString(this.font, Component.translatable("tooltip.fbp.disabled").withStyle(ChatFormatting.BOLD, ChatFormatting.RED), this.particleButton.getX() + 30, this.particleButton.getY() + 30 + 35, -1);
            }
        }

        graphics.pose().pushMatrix();

        graphics.pose().translate(x - 32.0F, y - 32.0F - 57.0F);
        graphics.pose().scale(4.0F, 4.0F);

        graphics.renderFakeItem(this.displayStack, 0, 0);

        graphics.pose().popMatrix();

        this.animationButton.render(graphics, mouseX, mouseY, partialTick);
        this.particleButton.render(graphics, mouseX, mouseY, partialTick);

        var hovered = this.animationButton.isHovered() ? this.animationButton : (this.particleButton.isHovered() ? this.particleButton : null);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INDICATOR_SPRITES.get(true, hovered != null && !hovered.active), mouseX - 20 / 2, mouseY - 20 / 2, 20, 20);
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
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }
}
