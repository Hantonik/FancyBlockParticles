package hantonik.fbp;

import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.config.FBPConfig;
import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.screen.FBPFastBlacklistScreen;
import hantonik.fbp.screen.FBPOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
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
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.season.SeasonHooks;
import sereneseasons.util.biome.BiomeUtil;

@Mod(FancyBlockParticles.MOD_ID)
public final class FancyBlockParticles {
    public static final String MOD_ID = "fbp";
    public static final String MOD_NAME = "FancyBlockParticles";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Marker SETUP_MARKER = MarkerManager.getMarker("SETUP");

    public static final FBPConfig CONFIG = FBPConfig.create();

    public FancyBlockParticles() {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing...");

        FMLJavaModLoadingContext.get().getModEventBus().register(this);

        ModList.get().getModContainerById(FancyBlockParticles.MOD_ID).ifPresent(mc -> mc.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (minecraft, screen) -> new FBPOptionsScreen(screen)));
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Starting client setup...");

        FBPKeyMappings.MAPPINGS.forEach(ClientRegistry::registerKeyBinding);

        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(this::postScreenInit);
        MinecraftForge.EVENT_BUS.addListener(this::postRenderGuiOverlay);
        MinecraftForge.EVENT_BUS.addListener(this::onClientLoggingIn);

        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Finished client setup!");
    }

    private void onAddReloadListener(final AddReloadListenerEvent event) {
        event.addListener((IResourceManagerReloadListener) manager -> FancyBlockParticles.CONFIG.load());
    }

    private void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (FBPKeyMappings.TOGGLE_MOD.consumeClick()) {
                FancyBlockParticles.CONFIG.global.setEnabled(!FancyBlockParticles.CONFIG.global.isEnabled());

                FancyBlockParticles.CONFIG.save();
            }

            if (FBPKeyMappings.TOGGLE_ANIMATIONS.consumeClick()) {
                if (!ModList.get().isLoaded("a_good_place")) {
                    FancyBlockParticles.CONFIG.animations.setEnabled(!FancyBlockParticles.CONFIG.animations.isEnabled());

                    FancyBlockParticles.CONFIG.save();
                }
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

    private void postScreenInit(final GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof IngameMenuScreen)
            FancyBlockParticles.CONFIG.save();
    }

    private void postRenderGuiOverlay(final RenderGameOverlayEvent.Post event) {
        if (FancyBlockParticles.CONFIG.global.isEnabled() && FancyBlockParticles.CONFIG.overlay.isFreezeEffectOverlay() && FancyBlockParticles.CONFIG.global.isFreezeEffect() && !Minecraft.getInstance().options.hideGui) {
            FontRenderer font = Minecraft.getInstance().font;
            ITextComponent text = new TranslationTextComponent("gui.fbp.freeze_effect").withStyle(TextFormatting.BOLD);

            font.drawShadow(event.getMatrixStack(), text, event.getWindow().getGuiScaledWidth() / 2.0F - font.width(text) / 2.0F, 5.0F, FancyBlockParticles.CONFIG.overlay.getFreezeEffectColor());
        }
    }

    private void onClientLoggingIn(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        FBPPlacingAnimationManager.clear();
    }

    public static Biome.RainType getBiomePrecipitation(ClientWorld level, Biome biome) {
        if (ModList.get().isLoaded("sereneseasons")) {
            RegistryKey<Biome> biomeKey = BiomeUtil.getBiomeKey(biome);
            Biome.RainType rainType = biome.getPrecipitation();

            if (SeasonsConfig.isDimensionWhitelisted(level.dimension()) && BiomeConfig.enablesSeasonalEffects(biomeKey) && (rainType == Biome.RainType.RAIN || rainType == Biome.RainType.NONE))
                return SeasonHooks.shouldRainInBiomeInSeason(level, biomeKey) ? Biome.RainType.RAIN : Biome.RainType.NONE;
            else
                return rainType;
        }

        return biome.getPrecipitation();
    }
}
