package hantonik.fbp;

import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.FBPFastBlacklistScreen;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Mod(FancyBlockParticles.MOD_ID)
public final class FancyBlockParticles {
    public static final String MOD_ID = "fbp";
    public static final String MOD_NAME = "FancyBlockParticles";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Marker SETUP_MARKER = MarkerManager.getMarker("SETUP");

    public static final FBPConfig CONFIG = FBPConfig.create();

    public FancyBlockParticles() {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing...");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        ModList.get().getModContainerById(FancyBlockParticles.MOD_ID).ifPresent(mc -> mc.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (minecraft, screen) -> new FBPOptionsScreen(screen)));
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Starting client setup...");

        MinecraftForge.EVENT_BUS.register(this);

        FBPKeyMappings.MAPPINGS.forEach(ClientRegistry::registerKeyBinding);

        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Finished client setup!");
    }

    @SubscribeEvent
    public void onAddReloadListener(final AddReloadListenerEvent event) {
        event.addListener((IResourceManagerReloadListener) manager -> FancyBlockParticles.CONFIG.load());
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (FBPKeyMappings.TOGGLE_MOD.consumeClick()) {
                FancyBlockParticles.CONFIG.global.setEnabled(!FancyBlockParticles.CONFIG.global.isEnabled());

                FancyBlockParticles.CONFIG.save();
            }

            if (FBPKeyMappings.OPEN_SETTINGS.consumeClick())
                Minecraft.getInstance().setScreen(new FBPOptionsScreen(null));

            if (FBPKeyMappings.ADD_TO_BLACKLIST.isDown()) {
                if (Screen.hasShiftDown()) {
                    ItemStack heldItem = Minecraft.getInstance().player.getMainHandItem();

                    if (heldItem.getItem() instanceof BlockItem)
                        Minecraft.getInstance().setScreen(new FBPFastBlacklistScreen(heldItem));
                } else {
                    RayTraceResult hit = Minecraft.getInstance().hitResult;

                    if (hit != null && hit.getType() == RayTraceResult.Type.BLOCK)
                        Minecraft.getInstance().setScreen(new FBPFastBlacklistScreen(((BlockRayTraceResult) hit).getBlockPos()));
                }

                FancyBlockParticles.CONFIG.save();
            }

            if (FBPKeyMappings.FREEZE_PARTICLES.consumeClick()) {
                if (FancyBlockParticles.CONFIG.global.isEnabled()) {
                    FancyBlockParticles.CONFIG.global.setFreezeEffect(!FancyBlockParticles.CONFIG.global.isFreezeEffect());

                    FancyBlockParticles.CONFIG.save();
                }
            }

            if (FBPKeyMappings.RELOAD_CONFIG.consumeClick()) {
                FancyBlockParticles.CONFIG.load();
                FancyBlockParticles.CONFIG.save();
            }
        }
    }

    @SubscribeEvent
    public void postScreenInit(final GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof IngameMenuScreen)
            FancyBlockParticles.CONFIG.save();
    }

    @SubscribeEvent
    public void postRenderGuiOverlay(final RenderGameOverlayEvent.Post event) {
        if (FancyBlockParticles.CONFIG.global.isEnabled() && FancyBlockParticles.CONFIG.overlay.isFreezeEffectOverlay() && FancyBlockParticles.CONFIG.global.isFreezeEffect() && !Minecraft.getInstance().options.hideGui) {
            FontRenderer font = Minecraft.getInstance().font;
            ITextComponent text = new TranslationTextComponent("gui.fbp.freeze_effect").withStyle(TextFormatting.BOLD);

            font.drawShadow(event.getMatrixStack(), text, event.getWindow().getGuiScaledWidth() / 2.0F - font.width(text) / 2.0F, 5.0F, FancyBlockParticles.CONFIG.overlay.getFreezeEffectColor());
        }
    }
}
