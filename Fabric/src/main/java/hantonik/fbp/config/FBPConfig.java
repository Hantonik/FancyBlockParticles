package hantonik.fbp.config;

import com.google.gson.*;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import org.apache.commons.compress.utils.Lists;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConfig {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_LOCKED = false;

    private static final boolean DEFAULT_FROZEN = false;
    private static final boolean DEFAULT_INFINITE_DURATION = false;
    private static final boolean DEFAULT_CARTOON_MODE = false;

    private static final boolean DEFAULT_FANCY_FLAME = true;
    private static final boolean DEFAULT_FANCY_SMOKE = true;
    private static final boolean DEFAULT_FANCY_RAIN = true;
    private static final boolean DEFAULT_FANCY_SNOW = true;
    private static final boolean DEFAULT_FANCY_PLACE_ANIMATION = false;

    private static final boolean DEFAULT_SMART_BREAKING = true;
    private static final boolean DEFAULT_LOW_TRACTION = false;
    private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = true;
    private static final boolean DEFAULT_SMOOTH_ANIMATION_LIGHTING = false;

    private static final boolean DEFAULT_SPAWN_PLACE_PARTICLES = true;

    private static final boolean DEFAULT_REST_ON_FLOOR = true;
    private static final boolean DEFAULT_BOUNCE_OFF_WALLS = true;
    private static final boolean DEFAULT_ENTITY_COLLISION = false;
    private static final boolean DEFAULT_WATER_PHYSICS = true;

    private static final boolean DEFAULT_RANDOM_SCALE = true;
    private static final boolean DEFAULT_RANDOM_ROTATION = true;
    private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

    private static final int DEFAULT_PARTICLES_PER_AXIS = 4;
    private static final double DEFAULT_WEATHER_PARTICLE_DENSITY = 1.0D;

    private static final int DEFAULT_MIN_LIFETIME = 10;
    private static final int DEFAULT_MAX_LIFETIME = 55;

    private static final double DEFAULT_SCALE_MULTIPLIER = 0.75D;
    private static final double DEFAULT_ROTATION_MULTIPLIER = 1.0D;
    private static final double DEFAULT_GRAVITY_MULTIPLIER = 1.0D;

    private static final List<Block> DEFAULT_DISABLED_PARTICLES = Lists.newArrayList();
    private static final List<Block> DEFAULT_DISABLED_ANIMATIONS = Lists.newArrayList();

    public static final FBPConfig DEFAULT_CONFIG = new FBPConfig(
            DEFAULT_ENABLED, DEFAULT_LOCKED,
            DEFAULT_FROZEN, DEFAULT_INFINITE_DURATION, DEFAULT_CARTOON_MODE,
            DEFAULT_FANCY_FLAME, DEFAULT_FANCY_SMOKE, DEFAULT_FANCY_RAIN, DEFAULT_FANCY_SNOW, DEFAULT_SMOOTH_ANIMATION_LIGHTING,
            DEFAULT_SMART_BREAKING, DEFAULT_LOW_TRACTION, DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_SMOOTH_ANIMATION_LIGHTING,
            DEFAULT_SPAWN_PLACE_PARTICLES,
            DEFAULT_REST_ON_FLOOR, DEFAULT_BOUNCE_OFF_WALLS, DEFAULT_ENTITY_COLLISION, DEFAULT_WATER_PHYSICS,
            DEFAULT_RANDOM_SCALE, DEFAULT_RANDOM_ROTATION, DEFAULT_RANDOM_FADING_SPEED,
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

    private boolean fancyFlame;
    private boolean fancySmoke;
    private boolean fancyRain;
    private boolean fancySnow;
    private boolean fancyPlaceAnimation;

    private boolean smartBreaking;
    private boolean lowTraction;
    private boolean spawnWhileFrozen;
    private boolean smoothAnimationLighting;

    private boolean spawnPlaceParticles;

    private boolean restOnFloor;
    private boolean bounceOffWalls;
    private boolean entityCollision;
    private boolean waterPhysics;

    private boolean randomScale;
    private boolean randomRotation;
    private boolean randomFadingSpeed;

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

    public static FBPConfig load() {
        var config = DEFAULT_CONFIG.copy();

        config.reload();

        return config;
    }

    public void setConfig(FBPConfig config) {
        this.enabled = config.enabled;
        this.locked = config.locked;

        this.frozen = config.frozen;
        this.infiniteDuration = config.infiniteDuration;
        this.cartoonMode = config.cartoonMode;

        this.fancyFlame = config.fancyFlame;
        this.fancySmoke = config.fancySmoke;
        this.fancyRain = config.fancyRain;
        this.fancySnow = config.fancySnow;
        this.fancyPlaceAnimation = config.fancyPlaceAnimation;

        this.smartBreaking = config.smartBreaking;
        this.lowTraction = config.lowTraction;
        this.spawnWhileFrozen = config.spawnWhileFrozen;
        this.smoothAnimationLighting = config.smoothAnimationLighting;

        this.spawnPlaceParticles = config.spawnPlaceParticles;

        this.restOnFloor = config.restOnFloor;
        this.bounceOffWalls = config.bounceOffWalls;
        this.entityCollision = config.entityCollision;
        this.waterPhysics = config.waterPhysics;

        this.randomScale = config.randomScale;
        this.randomRotation = config.randomRotation;
        this.randomFadingSpeed = config.randomFadingSpeed;

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

    public void applyConfig(FBPConfig config) {
        this.enabled = config.enabled;

        this.frozen = config.frozen;
        this.infiniteDuration = config.infiniteDuration;
        this.cartoonMode = config.cartoonMode;

        this.fancyFlame = config.fancyFlame;
        this.fancySmoke = config.fancySmoke;
        this.fancyRain = config.fancyRain;
        this.fancySnow = config.fancySnow;
        this.fancyPlaceAnimation = config.fancyPlaceAnimation;

        this.smartBreaking = config.smartBreaking;
        this.lowTraction = config.lowTraction;
        this.spawnWhileFrozen = config.spawnWhileFrozen;
        this.smoothAnimationLighting = config.smoothAnimationLighting;

        this.spawnPlaceParticles = config.spawnPlaceParticles;

        this.restOnFloor = config.restOnFloor;
        this.bounceOffWalls = config.bounceOffWalls;
        this.entityCollision = config.entityCollision;
        this.waterPhysics = config.waterPhysics;

        this.randomScale = config.randomScale;
        this.randomRotation = config.randomRotation;
        this.randomFadingSpeed = config.randomFadingSpeed;

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

        var file = new File(FBPConstants.CONFIG_PATH.toString(), "config.json");

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

            this.fancyFlame = GsonHelper.getAsBoolean(json, "fancyFlame", DEFAULT_FANCY_FLAME);
            this.fancySmoke = GsonHelper.getAsBoolean(json, "fancySmoke", DEFAULT_FANCY_SMOKE);
            this.fancyRain = GsonHelper.getAsBoolean(json, "fancyRain", DEFAULT_FANCY_RAIN);
            this.fancySnow = GsonHelper.getAsBoolean(json, "fancySnow", DEFAULT_FANCY_SNOW);
            this.fancyPlaceAnimation = GsonHelper.getAsBoolean(json, "fancyPlaceAnimation", DEFAULT_FANCY_PLACE_ANIMATION);

            this.smartBreaking = GsonHelper.getAsBoolean(json, "smartBreaking", DEFAULT_SMART_BREAKING);
            this.lowTraction = GsonHelper.getAsBoolean(json, "lowTraction", DEFAULT_LOW_TRACTION);
            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);
            this.smoothAnimationLighting = GsonHelper.getAsBoolean(json, "smoothAnimationLighting", DEFAULT_SMOOTH_ANIMATION_LIGHTING);

            this.spawnPlaceParticles = GsonHelper.getAsBoolean(json, "spawnPlaceParticles", DEFAULT_SPAWN_PLACE_PARTICLES);

            this.restOnFloor = GsonHelper.getAsBoolean(json, "restOnFloor", DEFAULT_REST_ON_FLOOR);
            this.bounceOffWalls = GsonHelper.getAsBoolean(json, "bounceOffWalls", DEFAULT_BOUNCE_OFF_WALLS);
            this.entityCollision = GsonHelper.getAsBoolean(json, "entityCollision", DEFAULT_ENTITY_COLLISION);
            this.waterPhysics = GsonHelper.getAsBoolean(json, "waterPhysics", DEFAULT_WATER_PHYSICS);

            this.randomScale = GsonHelper.getAsBoolean(json, "randomScale", DEFAULT_RANDOM_SCALE);
            this.randomRotation = GsonHelper.getAsBoolean(json, "randomRotation", DEFAULT_RANDOM_ROTATION);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

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
                        disabled.add(Registry.BLOCK.get(new ResourceLocation(entry.getAsString())));
                } else
                    disabled.addAll(DEFAULT_DISABLED_PARTICLES);
            });

            this.disabledAnimations = Util.make(Lists.newArrayList(), disabled -> {
                if (json.has("disabledAnimations")) {
                    for (var entry : GsonHelper.getAsJsonArray(json, "disabledAnimations"))
                        disabled.add(Registry.BLOCK.get(new ResourceLocation(entry.getAsString())));
                } else
                    disabled.addAll(DEFAULT_DISABLED_ANIMATIONS);
            });
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no load FBP config.", e);
        }
    }

    public void save() {
        try (var writer = new FileWriter(new File(FBPConstants.CONFIG_PATH.toString(), "config.json"))) {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);
            json.addProperty("locked", this.locked);

            json.addProperty("frozen", this.frozen);
            json.addProperty("infiniteDuration", this.infiniteDuration);
            json.addProperty("cartoonMode", this.cartoonMode);

            json.addProperty("fancyFlame", this.fancyFlame);
            json.addProperty("fancySmoke", this.fancySmoke);
            json.addProperty("fancyRain", this.fancyRain);
            json.addProperty("fancySnow", this.fancySnow);
            json.addProperty("fancyPlaceAnimation", this.fancyPlaceAnimation);

            json.addProperty("smartBreaking", this.smartBreaking);
            json.addProperty("lowTraction", this.lowTraction);
            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);
            json.addProperty("smoothAnimationLighting", this.smoothAnimationLighting);

            json.addProperty("spawnPlaceParticles", this.spawnPlaceParticles);

            json.addProperty("restOnFloor", this.restOnFloor);
            json.addProperty("bounceOffWalls", this.bounceOffWalls);
            json.addProperty("entityCollision", this.entityCollision);
            json.addProperty("waterPhysics", this.waterPhysics);

            json.addProperty("randomScale", this.randomScale);
            json.addProperty("randomRotation", this.randomRotation);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("particlesPerAxis", this.particlesPerAxis);
            json.addProperty("weatherParticleDensity", this.weatherParticleDensity);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("scaleMultiplier", this.scaleMultiplier);
            json.addProperty("rotationMultiplier", this.rotationMultiplier);
            json.addProperty("gravityMultiplier", this.gravityMultiplier);

            json.add("disabledParticles", Util.make(new JsonArray(), disabled -> {
                for (var entry : this.disabledParticles)
                    disabled.add(Registry.BLOCK.getKey(entry).toString());
            }));

            json.add("disabledAnimations", Util.make(new JsonArray(), disabled -> {
                for (var entry : this.disabledAnimations)
                    disabled.add(Registry.BLOCK.getKey(entry).toString());
            }));

            GSON.toJson(json, writer);
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no save FBP config.", e);
        }
    }

    public FBPConfig copy() {
        return new FBPConfig(
                this.enabled, this.locked,
                this.frozen, this.infiniteDuration, this.cartoonMode,
                this.fancyFlame, this.fancySmoke, this.fancyRain, this.fancySnow, this.fancyPlaceAnimation,
                this.smartBreaking, this.lowTraction, this.spawnWhileFrozen, this.smoothAnimationLighting,
                this.spawnPlaceParticles,
                this.restOnFloor, this.bounceOffWalls, this.entityCollision, this.waterPhysics,
                this.randomScale, this.randomRotation, this.randomFadingSpeed,
                this.particlesPerAxis, this.weatherParticleDensity,
                this.minLifetime, this.maxLifetime,
                this.scaleMultiplier, this.rotationMultiplier, this.gravityMultiplier,
                List.copyOf(this.disabledParticles), List.copyOf(this.disabledAnimations)
        );
    }

    public void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(FancyBlockParticles.MOD_ID, "config");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                FBPConfig.this.reload();
            }
        });
    }
}
