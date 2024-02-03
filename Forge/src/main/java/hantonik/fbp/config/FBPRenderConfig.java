package hantonik.fbp.config;

import com.google.gson.*;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPRenderConfig {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_LOCKED = false;

    private static final boolean DEFAULT_FROZEN = false;
    private static final boolean DEFAULT_INFINITE_DURATION = false;
    private static final boolean DEFAULT_CARTOON_MODE = false;

    private static final int DEFAULT_PARTICLES_PER_AXIS = 4;
    private static final double DEFAULT_WEATHER_PARTICLE_DENSITY = 1.0D;

    private static final int DEFAULT_MIN_LIFETIME = 10;
    private static final int DEFAULT_MAX_LIFETIME = 55;

    private static final double DEFAULT_SCALE_MULTIPLIER = 0.75D;
    private static final double DEFAULT_ROTATION_MULTIPLIER = 1.0D;
    private static final double DEFAULT_GRAVITY_MULTIPLIER = 1.0D;

    private static final List<Block> DEFAULT_DISABLED_PARTICLES = Lists.newArrayList();
    private static final List<Block> DEFAULT_DISABLED_ANIMATIONS = Lists.newArrayList();

    public static final FBPRenderConfig DEFAULT_CONFIG = new FBPRenderConfig(
            DEFAULT_ENABLED, DEFAULT_LOCKED,
            DEFAULT_FROZEN, DEFAULT_INFINITE_DURATION, DEFAULT_CARTOON_MODE,
            DEFAULT_PARTICLES_PER_AXIS, DEFAULT_WEATHER_PARTICLE_DENSITY,
            DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
            DEFAULT_SCALE_MULTIPLIER, DEFAULT_ROTATION_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER,
            DEFAULT_DISABLED_PARTICLES, DEFAULT_DISABLED_ANIMATIONS
    );

    private boolean enabled;
    private boolean locked;

    private boolean frozen;
    private boolean infiniteDuration;
    private boolean cartoonMode;

    private int particlesPerAxis;
    private double weatherParticleDensity;

    private int minLifetime;
    private int maxLifetime;

    private double scaleMultiplier;
    private double rotationMultiplier;
    private double gravityMultiplier;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private List<Block> disabledParticles;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private List<Block> disabledAnimations;

    public void toggleParticles(Block block) {
        if (this.isBlockParticlesEnabled(block))
            this.disabledParticles.add(block);
        else
            this.disabledParticles.remove(block);
    }

    public void toggleAnimations(Block block) {
        if (this.isBlockAnimationsEnabled(block))
            this.disabledAnimations.add(block);
        else
            this.disabledAnimations.remove(block);
    }

    public boolean isBlockParticlesEnabled(Block block) {
        return !this.disabledParticles.contains(block);
    }

    public boolean isBlockAnimationsEnabled(Block block) {
        return !this.disabledAnimations.contains(block);
    }

    public static FBPRenderConfig load() {
        var config = DEFAULT_CONFIG.copy();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> config::reload);

        return config;
    }

    public void setConfig(FBPRenderConfig config) {
        this.enabled = config.enabled;
        this.locked = config.locked;

        this.frozen = config.frozen;
        this.infiniteDuration = config.infiniteDuration;
        this.cartoonMode = config.cartoonMode;

        this.particlesPerAxis = config.particlesPerAxis;
        this.weatherParticleDensity = config.weatherParticleDensity;

        this.minLifetime = config.minLifetime;
        this.maxLifetime = config.maxLifetime;

        this.scaleMultiplier = config.scaleMultiplier;
        this.rotationMultiplier = config.rotationMultiplier;
        this.gravityMultiplier = config.gravityMultiplier;

        this.disabledParticles = config.disabledParticles.stream().toList();
        this.disabledAnimations = config.disabledAnimations.stream().toList();
    }

    public void applyConfig(FBPRenderConfig config) {
        this.enabled = config.enabled;

        this.frozen = config.frozen;
        this.infiniteDuration = config.infiniteDuration;
        this.cartoonMode = config.cartoonMode;

        this.particlesPerAxis = config.particlesPerAxis;
        this.weatherParticleDensity = config.weatherParticleDensity;

        this.minLifetime = config.minLifetime;
        this.maxLifetime = config.maxLifetime;

        this.scaleMultiplier = config.scaleMultiplier;
        this.rotationMultiplier = config.rotationMultiplier;
        this.gravityMultiplier = config.gravityMultiplier;

        this.disabledParticles.addAll(config.disabledParticles);
        this.disabledAnimations.addAll(config.disabledAnimations);
    }

    public void reset() {
        this.setConfig(DEFAULT_CONFIG.copy());
    }

    public void reload() {
        try {
            Files.createDirectory(FBPConstants.CONFIG_PATH);
        } catch (FileAlreadyExistsException e) {
            FancyBlockParticles.LOGGER.debug("{} config directory already exists.", FancyBlockParticles.MOD_NAME);
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Failed to create {} config directory.", FancyBlockParticles.MOD_NAME);
        }

        var file = new File(FBPConstants.CONFIG_PATH.toString(), "render.json");

        try {
            if (!file.exists()) {
                file.createNewFile();

                this.save();
            }

            var json = JsonParser.parseReader(new InputStreamReader(new FileInputStream(file))).getAsJsonObject();

            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);
            this.locked = GsonHelper.getAsBoolean(json, "locked", DEFAULT_LOCKED);

            this.frozen = GsonHelper.getAsBoolean(json, "frozen", DEFAULT_FROZEN);
            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);
            this.cartoonMode = GsonHelper.getAsBoolean(json, "cartoonMode", DEFAULT_CARTOON_MODE);

            this.particlesPerAxis = GsonHelper.getAsInt(json, "particlesPerAxis", DEFAULT_PARTICLES_PER_AXIS);
            this.weatherParticleDensity = GsonHelper.getAsDouble(json, "weatherParticleDensity", DEFAULT_WEATHER_PARTICLE_DENSITY);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.scaleMultiplier = GsonHelper.getAsDouble(json, "scaleMultiplier", DEFAULT_SCALE_MULTIPLIER);
            this.rotationMultiplier = GsonHelper.getAsDouble(json, "rotationMultiplier", DEFAULT_ROTATION_MULTIPLIER);
            this.gravityMultiplier = GsonHelper.getAsDouble(json, "gravityMultiplier", DEFAULT_GRAVITY_MULTIPLIER);

            this.disabledParticles = Util.make(Lists.newArrayList(), disabled -> {
                if (json.has("disabledParticles")) {
                    for (var entry : GsonHelper.getAsJsonArray(json, "disabledParticles"))
                        disabled.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
                } else
                    disabled.addAll(DEFAULT_DISABLED_PARTICLES);
            });

            this.disabledAnimations = Util.make(Lists.newArrayList(), disabled -> {
                if (json.has("disabledAnimations")) {
                    for (var entry : GsonHelper.getAsJsonArray(json, "disabledAnimations"))
                        disabled.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
                } else
                    disabled.addAll(DEFAULT_DISABLED_ANIMATIONS);
            });
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no load render config.", e);
        }
    }

    public void save() {
        try (var writer = new FileWriter(new File(FBPConstants.CONFIG_PATH.toString(), "render.json"))) {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);
            json.addProperty("locked", this.locked);

            json.addProperty("frozen", this.frozen);
            json.addProperty("infiniteDuration", this.infiniteDuration);
            json.addProperty("cartoonMode", this.cartoonMode);

            json.addProperty("particlesPerAxis", this.particlesPerAxis);
            json.addProperty("weatherParticleDensity", this.weatherParticleDensity);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("scaleMultiplier", this.scaleMultiplier);
            json.addProperty("rotationMultiplier", this.rotationMultiplier);
            json.addProperty("gravityMultiplier", this.gravityMultiplier);

            json.add("disabledParticles", Util.make(new JsonArray(), disabled -> {
                for (var entry : this.disabledParticles)
                    disabled.add(ForgeRegistries.BLOCKS.getKey(entry).toString());
            }));

            json.add("disabledAnimations", Util.make(new JsonArray(), disabled -> {
                for (var entry : this.disabledAnimations)
                    disabled.add(ForgeRegistries.BLOCKS.getKey(entry).toString());
            }));

            GSON.toJson(json, writer);
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no save render config.", e);
        }
    }

    public FBPRenderConfig copy() {
        return new FBPRenderConfig(
                this.enabled, this.locked,
                this.frozen, this.infiniteDuration, this.cartoonMode,
                this.particlesPerAxis, this.weatherParticleDensity,
                this.minLifetime, this.maxLifetime,
                this.scaleMultiplier, this.rotationMultiplier, this.gravityMultiplier,
                this.disabledParticles.stream().toList(), this.disabledAnimations.stream().toList()
        );
    }

    @SubscribeEvent
    public void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> this.reload());
    }
}
