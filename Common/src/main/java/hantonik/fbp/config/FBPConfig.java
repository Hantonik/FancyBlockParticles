package hantonik.fbp.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.config.api.IFBPConfig;
import hantonik.fbp.platform.Services;
import hantonik.fbp.util.BlacklistMode;
import hantonik.fbp.util.FBPConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import org.apache.commons.compress.utils.Lists;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConfig implements IFBPConfig<FBPConfig> {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static final FBPConfig DEFAULT_CONFIG = new FBPConfig(
            Global.DEFAULT_CONFIG,
            Terrain.DEFAULT_CONFIG,
            Flame.DEFAULT_CONFIG,
            Smoke.DEFAULT_CONFIG,
            CampfireSmoke.DEFAULT_CONFIG,
            Trail.DEFAULT_CONFIG,
            Firefly.DEFAULT_CONFIG,
            Rain.DEFAULT_CONFIG,
            Snow.DEFAULT_CONFIG,
            Drip.DEFAULT_CONFIG,
            Misc.DEFAULT_CONFIG,
            Animations.DEFAULT_CONFIG,
            Overlay.DEFAULT_CONFIG
    );

    public final Global global;
    public final Terrain terrain;
    public final Flame flame;
    public final Smoke smoke;
    public final CampfireSmoke campfireSmoke;
    public final Trail trail;
    public final Firefly firefly;
    public final Rain rain;
    public final Snow snow;
    public final Drip drip;
    public final Misc misc;
    public final Animations animations;
    public final Overlay overlay;

    public void toggleParticles(Block block) {
        var mode = this.global.particlesBlacklist.getOrDefault(block, BlacklistMode.FANCY).getNext();

        if (mode == BlacklistMode.FANCY)
            this.global.particlesBlacklist.remove(block);
        else
            this.global.particlesBlacklist.put(block, mode);
    }

    public void toggleAnimations(Block block) {
        if (this.isBlockAnimationsEnabled(block))
            this.global.animationsBlacklist.add(block);
        else
            this.global.animationsBlacklist.remove(block);
    }

    public BlacklistMode getBlockParticlesMode(Block block) {
        return this.global.particlesBlacklist.getOrDefault(block, BlacklistMode.FANCY);
    }

    public boolean isBlockAnimationsEnabled(Block block) {
        return !this.global.animationsBlacklist.contains(block);
    }

    public static FBPConfig create() {
        return DEFAULT_CONFIG.copy();
    }

    @Override
    public void setConfig(FBPConfig config) {
        this.global.setConfig(config.global);
        this.terrain.setConfig(config.terrain);
        this.flame.setConfig(config.flame);
        this.smoke.setConfig(config.smoke);
        this.campfireSmoke.setConfig(config.campfireSmoke);
        this.trail.setConfig(config.trail);
        this.firefly.setConfig(config.firefly);
        this.rain.setConfig(config.rain);
        this.snow.setConfig(config.snow);
        this.drip.setConfig(config.drip);
        this.misc.setConfig(config.misc);
        this.animations.setConfig(config.animations);
        this.overlay.setConfig(config.overlay);
    }

    @Override
    public void applyConfig(FBPConfig config) {
        this.global.applyConfig(config.global);
        this.terrain.applyConfig(config.terrain);
        this.flame.applyConfig(config.flame);
        this.smoke.applyConfig(config.smoke);
        this.campfireSmoke.applyConfig(config.campfireSmoke);
        this.trail.applyConfig(config.trail);
        this.firefly.applyConfig(config.firefly);
        this.rain.applyConfig(config.rain);
        this.snow.applyConfig(config.snow);
        this.drip.applyConfig(config.drip);
        this.misc.applyConfig(config.misc);
        this.animations.applyConfig(config.animations);
        this.overlay.applyConfig(config.overlay);
    }

    @Override
    public void load() {
        try {
            Files.createDirectory(FBPConstants.CONFIG_PATH);
        } catch (FileAlreadyExistsException e) {
            FancyBlockParticles.LOGGER.debug("{} config directory already exists.", FancyBlockParticles.MOD_NAME);
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Failed to create {} config directory.", FancyBlockParticles.MOD_NAME);
        }

        var file = new File(FBPConstants.CONFIG_PATH.toString(), "config.json");

        try {
            if (file.createNewFile())
                this.save();
            else {
                try (var reader = new InputStreamReader(new FileInputStream(file))) {
                    var json = JsonParser.parseReader(reader).getAsJsonObject();

                    this.global.load(GsonHelper.getAsJsonObject(json, "global", new JsonObject()));
                    this.terrain.load(GsonHelper.getAsJsonObject(json, "terrain", new JsonObject()));
                    this.flame.load(GsonHelper.getAsJsonObject(json, "flame", new JsonObject()));
                    this.smoke.load(GsonHelper.getAsJsonObject(json, "smoke", new JsonObject()));
                    this.campfireSmoke.load(GsonHelper.getAsJsonObject(json, "campfireSmoke", new JsonObject()));
                    this.trail.load(GsonHelper.getAsJsonObject(json, "trail", new JsonObject()));
                    this.firefly.load(GsonHelper.getAsJsonObject(json, "firefly", new JsonObject()));
                    this.rain.load(GsonHelper.getAsJsonObject(json, "rain", new JsonObject()));
                    this.snow.load(GsonHelper.getAsJsonObject(json, "snow", new JsonObject()));
                    this.drip.load(GsonHelper.getAsJsonObject(json, "drip", new JsonObject()));
                    this.misc.load(GsonHelper.getAsJsonObject(json, "misc", new JsonObject()));
                    this.animations.load(GsonHelper.getAsJsonObject(json, "animations", new JsonObject()));
                    this.overlay.load(GsonHelper.getAsJsonObject(json, "overlay", new JsonObject()));
                } catch (JsonParseException | IllegalStateException e) {
                    FancyBlockParticles.LOGGER.warn("FBP config file is corrupt! Generating a new one.");

                    this.save();
                }
            }
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no load FBP config.", e);
        }
    }

    @Override
    public JsonObject save() {
        var json = new JsonObject();

        try (var writer = new FileWriter(new File(FBPConstants.CONFIG_PATH.toString(), "config.json"))) {
            json.add("global", this.global.save());
            json.add("terrain", this.terrain.save());
            json.add("flame", this.flame.save());
            json.add("smoke", this.smoke.save());
            json.add("campfireSmoke", this.campfireSmoke.save());
            json.add("trail", this.trail.save());
            json.add("firefly", this.firefly.save());
            json.add("rain", this.rain.save());
            json.add("snow", this.snow.save());
            json.add("drip", this.drip.save());
            json.add("misc", this.misc.save());
            json.add("animations", this.animations.save());
            json.add("overlay", this.overlay.save());

            GSON.toJson(json, writer);
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no save FBP config.", e);
        }

        return json;
    }

    @Override
    public void reset() {
        this.setConfig(DEFAULT_CONFIG.copy());
    }

    @Override
    public FBPConfig copy() {
        return new FBPConfig(
                this.global.copy(),
                this.terrain.copy(),
                this.flame.copy(),
                this.smoke.copy(),
                this.campfireSmoke.copy(),
                this.trail.copy(),
                this.firefly.copy(),
                this.rain.copy(),
                this.snow.copy(),
                this.drip.copy(),
                this.misc.copy(),
                this.animations.copy(),
                this.overlay.copy()
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Global implements IFBPConfig<Global> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_LOCKED = false;
        private static final boolean DEFAULT_DISABLE_OCULUS_WARNING = false;

        private static final boolean DEFAULT_FREEZE_EFFECT = false;
        private static final boolean DEFAULT_CARTOON_MODE = false;

        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final Map<Block, BlacklistMode> DEFAULT_PARTICLES_BLACKLIST = Map.of();
        private static final List<Block> DEFAULT_ANIMATIONS_BLACKLIST = List.of();

        public static final Global DEFAULT_CONFIG = new Global(
                DEFAULT_ENABLED,
                DEFAULT_LOCKED, DEFAULT_DISABLE_OCULUS_WARNING,
                DEFAULT_FREEZE_EFFECT, DEFAULT_CARTOON_MODE,
                DEFAULT_INFINITE_DURATION,
                DEFAULT_PARTICLES_BLACKLIST, DEFAULT_ANIMATIONS_BLACKLIST
        );

        private boolean enabled;

        private boolean locked;
        private boolean disableOculusWarning;

        private boolean freezeEffect;
        private boolean cartoonMode;

        private boolean infiniteDuration;

        @Setter(AccessLevel.PRIVATE)
        private Map<Block, BlacklistMode> particlesBlacklist;

        @Setter(AccessLevel.PRIVATE)
        private List<Block> animationsBlacklist;

        @Override
        public void setConfig(Global config) {
            this.enabled = config.enabled;

            this.locked = config.locked;

            this.freezeEffect = config.freezeEffect;

            this.cartoonMode = config.cartoonMode;

            this.infiniteDuration = config.infiniteDuration;

            this.particlesBlacklist = new HashMap<>(config.particlesBlacklist);
            this.animationsBlacklist = new ArrayList<>(config.animationsBlacklist);
        }

        @Override
        public void applyConfig(Global config) {
            this.enabled = config.enabled;

            this.locked = config.locked;

            this.freezeEffect = config.freezeEffect;

            this.cartoonMode = config.cartoonMode;

            this.infiniteDuration = config.infiniteDuration;

            this.particlesBlacklist.putAll(config.particlesBlacklist);
            this.animationsBlacklist.addAll(config.animationsBlacklist);
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);

            this.locked = GsonHelper.getAsBoolean(json, "locked", DEFAULT_LOCKED);
            this.disableOculusWarning = GsonHelper.getAsBoolean(json, "disableOculusWarning", DEFAULT_DISABLE_OCULUS_WARNING);

            this.freezeEffect = GsonHelper.getAsBoolean(json, "freezeEffect", DEFAULT_FREEZE_EFFECT);

            this.cartoonMode = GsonHelper.getAsBoolean(json, "cartoonMode", DEFAULT_CARTOON_MODE);

            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);

            this.particlesBlacklist = Util.make(Maps.newHashMap(), blacklisted -> {
                if (json.has("particlesBlacklist")) {
                    for (var entry : GsonHelper.getAsJsonObject(json, "particlesBlacklist").entrySet()) {
                        try {
                            var mode = BlacklistMode.valueOf(entry.getValue().getAsString().toUpperCase(Locale.ENGLISH));

                            if (mode != BlacklistMode.FANCY)
                                blacklisted.put(BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(entry.getKey())), mode);
                        } catch (IllegalArgumentException e) {
                            FancyBlockParticles.LOGGER.error("Value '{}' is not a valid blacklist mode!", entry.getValue().getAsString().toUpperCase(Locale.ENGLISH));
                        }
                    }
                } else
                    blacklisted.putAll(DEFAULT_PARTICLES_BLACKLIST);
            });

            this.animationsBlacklist = Util.make(Lists.newArrayList(), blacklisted -> {
                if (json.has("animationsBlacklist")) {
                    for (var entry : GsonHelper.getAsJsonArray(json, "animationsBlacklist"))
                        blacklisted.add(BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(entry.getAsString())));
                } else
                    blacklisted.addAll(DEFAULT_ANIMATIONS_BLACKLIST);
            });

            // OUTDATED
            this.loadOutdated(json);
        }

        @Deprecated(forRemoval = true)
        private void loadOutdated(JsonObject json) {
            this.particlesBlacklist = Util.make(this.particlesBlacklist, blacklisted -> {
                if (json.has("disabledParticles")) {
                    FancyBlockParticles.LOGGER.warn("Using outdated object name: 'disabledParticles'");

                    for (var entry : GsonHelper.getAsJsonArray(json, "disabledParticles"))
                        blacklisted.put(BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(entry.getAsString())), BlacklistMode.VANILLA);
                }
            });

            this.animationsBlacklist = Util.make(this.animationsBlacklist, disabled -> {
                if (json.has("disabledAnimations")) {
                    FancyBlockParticles.LOGGER.warn("Using outdated object name: 'disabledAnimations'");

                    for (var entry : GsonHelper.getAsJsonArray(json, "disabledAnimations"))
                        disabled.add(BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(entry.getAsString())));
                }
            });
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("locked", this.locked);
            json.addProperty("disableOculusWarning", this.disableOculusWarning);

            json.addProperty("freezeEffect", this.freezeEffect);

            json.addProperty("cartoonMode", this.cartoonMode);

            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.add("particlesBlacklist", Util.make(new JsonObject(), blacklisted -> {
                for (var entry : this.particlesBlacklist.entrySet())
                    if (entry.getValue() != BlacklistMode.FANCY)
                        blacklisted.addProperty(BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString(), entry.getValue().name());
            }));

            json.add("animationsBlacklist", Util.make(new JsonArray(), disabled -> {
                for (var entry : this.animationsBlacklist)
                    disabled.add(BuiltInRegistries.BLOCK.getKey(entry).toString());
            }));

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Global copy() {
            return new Global(
                    this.enabled,
                    this.locked, this.disableOculusWarning,
                    this.freezeEffect,
                    this.cartoonMode,
                    this.infiniteDuration,
                    new HashMap<>(this.particlesBlacklist), new ArrayList<>(this.animationsBlacklist)
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Terrain implements IFBPConfig<Terrain> {
        private static final boolean DEFAULT_FANCY_BREAKING_PARTICLES = true;
        private static final boolean DEFAULT_FANCY_CRACKING_PARTICLES = true;
        private static final boolean DEFAULT_FANCY_FALLING_DUST_PARTICLES = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = true;
        private static final boolean DEFAULT_INFINITE_DURATION = false;
        
        private static final boolean DEFAULT_SMART_BREAKING = true;
        private static final boolean DEFAULT_LOW_TRACTION = false;
        private static final boolean DEFAULT_REST_ON_FLOOR = true;
        private static final boolean DEFAULT_BOUNCE_OFF_WALLS = true;
        private static final boolean DEFAULT_ENTITY_COLLISION = false;
        private static final boolean DEFAULT_WATER_PHYSICS = true;
        
        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_ROTATION = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;
        
        private static final int DEFAULT_PARTICLES_PER_AXIS = 4;
        
        private static final int DEFAULT_MIN_LIFETIME = 10;
        private static final int DEFAULT_MAX_LIFETIME = 55;

        private static final float DEFAULT_SIZE_MULTIPLIER = 0.75F;
        private static final float DEFAULT_ROTATION_MULTIPLIER = 1.0F;
        private static final float DEFAULT_GRAVITY_MULTIPLIER = 1.0F;
        
        public static final Terrain DEFAULT_CONFIG = new Terrain(
                DEFAULT_FANCY_BREAKING_PARTICLES, DEFAULT_FANCY_CRACKING_PARTICLES, DEFAULT_FANCY_FALLING_DUST_PARTICLES,
                DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_INFINITE_DURATION,
                DEFAULT_SMART_BREAKING, DEFAULT_LOW_TRACTION, DEFAULT_REST_ON_FLOOR, DEFAULT_BOUNCE_OFF_WALLS, DEFAULT_ENTITY_COLLISION, DEFAULT_WATER_PHYSICS,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_ROTATION, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_PARTICLES_PER_AXIS,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_ROTATION_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER
        );
        
        private boolean fancyBreakingParticles;
        private boolean fancyCrackingParticles;
        private boolean fancyFallingDustParticles;

        private boolean spawnWhileFrozen;
        private boolean infiniteDuration;
        
        private boolean smartBreaking;
        private boolean lowTraction;
        private boolean restOnFloor;
        private boolean bounceOffWalls;
        private boolean entityCollision;
        private boolean waterPhysics;
        
        private boolean randomSize;
        private boolean randomRotation;
        private boolean randomFadingSpeed;
        
        private int particlesPerAxis;
        
        private int minLifetime;
        private int maxLifetime;
        
        private float sizeMultiplier;
        private float rotationMultiplier;
        private float gravityMultiplier;

        @Override
        public void setConfig(Terrain config) {
            this.fancyBreakingParticles = config.fancyBreakingParticles;
            this.fancyCrackingParticles = config.fancyCrackingParticles;
            this.fancyFallingDustParticles = config.fancyFallingDustParticles;

            this.spawnWhileFrozen = config.spawnWhileFrozen;
            this.infiniteDuration = config.infiniteDuration;

            this.smartBreaking = config.smartBreaking;
            this.lowTraction = config.lowTraction;
            this.restOnFloor = config.restOnFloor;
            this.bounceOffWalls = config.bounceOffWalls;
            this.entityCollision = config.entityCollision;
            this.waterPhysics = config.waterPhysics;

            this.randomSize = config.randomSize;
            this.randomRotation = config.randomRotation;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.particlesPerAxis = config.particlesPerAxis;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.sizeMultiplier = config.sizeMultiplier;
            this.rotationMultiplier = config.rotationMultiplier;
            this.gravityMultiplier = config.gravityMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.fancyBreakingParticles = GsonHelper.getAsBoolean(json, "fancyBreakingParticles", DEFAULT_FANCY_BREAKING_PARTICLES);
            this.fancyCrackingParticles = GsonHelper.getAsBoolean(json, "fancyCrackingParticles", DEFAULT_FANCY_CRACKING_PARTICLES);
            this.fancyFallingDustParticles = GsonHelper.getAsBoolean(json, "fancyFallingDustParticles", DEFAULT_FANCY_FALLING_DUST_PARTICLES);

            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);
            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);

            this.smartBreaking = GsonHelper.getAsBoolean(json, "smartBreaking", DEFAULT_SMART_BREAKING);
            this.lowTraction = GsonHelper.getAsBoolean(json, "lowTraction", DEFAULT_LOW_TRACTION);
            this.restOnFloor = GsonHelper.getAsBoolean(json, "restOnFloor", DEFAULT_REST_ON_FLOOR);
            this.bounceOffWalls = GsonHelper.getAsBoolean(json, "bounceOffWalls", DEFAULT_BOUNCE_OFF_WALLS);
            this.entityCollision = GsonHelper.getAsBoolean(json, "entityCollision", DEFAULT_ENTITY_COLLISION);
            this.waterPhysics = GsonHelper.getAsBoolean(json, "waterPhysics", DEFAULT_WATER_PHYSICS);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomRotation = GsonHelper.getAsBoolean(json, "randomRotation", DEFAULT_RANDOM_ROTATION);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.particlesPerAxis = GsonHelper.getAsInt(json, "particlesPerAxis", DEFAULT_PARTICLES_PER_AXIS);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
            this.rotationMultiplier = GsonHelper.getAsFloat(json, "rotationMultiplier", DEFAULT_ROTATION_MULTIPLIER);
            this.gravityMultiplier = GsonHelper.getAsFloat(json, "gravityMultiplier", DEFAULT_GRAVITY_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("fancyBreakingParticles", this.fancyBreakingParticles);
            json.addProperty("fancyCrackingParticles", this.fancyCrackingParticles);
            json.addProperty("fancyFallingDustParticles", this.fancyFallingDustParticles);

            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);
            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.addProperty("smartBreaking", this.smartBreaking);
            json.addProperty("lowTraction", this.lowTraction);
            json.addProperty("restOnFloor", this.restOnFloor);
            json.addProperty("bounceOffWalls", this.bounceOffWalls);
            json.addProperty("entityCollision", this.entityCollision);
            json.addProperty("waterPhysics", this.waterPhysics);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomRotation", this.randomRotation);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("particlesPerAxis", this.particlesPerAxis);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);
            json.addProperty("rotationMultiplier", this.rotationMultiplier);
            json.addProperty("gravityMultiplier", this.gravityMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Terrain copy() {
            return new Terrain(
                    this.fancyBreakingParticles, this.fancyCrackingParticles, this.fancyFallingDustParticles,
                    this.spawnWhileFrozen, this.infiniteDuration,
                    this.smartBreaking, this.lowTraction, this.restOnFloor, this.bounceOffWalls, this.entityCollision, this.waterPhysics,
                    this.randomSize, this.randomRotation, this.randomFadingSpeed,
                    this.particlesPerAxis,
                    this.minLifetime, this.maxLifetime,
                    this.sizeMultiplier, this.rotationMultiplier, this.gravityMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Flame implements IFBPConfig<Flame> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = false;
        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 10;
        private static final int DEFAULT_MAX_LIFETIME = 15;

        private static final float DEFAULT_SIZE_MULTIPLIER = 0.75F;

        public static final Flame DEFAULT_CONFIG = new Flame(
                DEFAULT_ENABLED,
                DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_INFINITE_DURATION,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER
        );

        private boolean enabled;

        private boolean spawnWhileFrozen;
        private boolean infiniteDuration;

        private boolean randomSize;
        private boolean randomFadingSpeed;

        private int minLifetime;
        private int maxLifetime;

        private float sizeMultiplier;

        @Override
        public void setConfig(Flame config) {
            this.enabled = config.enabled;

            this.spawnWhileFrozen = config.spawnWhileFrozen;
            this.infiniteDuration = config.infiniteDuration;

            this.randomSize = config.randomSize;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.sizeMultiplier = config.sizeMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);

            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);
            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);
            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Flame copy() {
            return new Flame(
                    this.enabled,
                    this.spawnWhileFrozen, this.infiniteDuration,
                    this.randomSize, this.randomFadingSpeed,
                    this.minLifetime, this.maxLifetime,
                    this.sizeMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Smoke implements IFBPConfig<Smoke> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = false;
        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 10;
        private static final int DEFAULT_MAX_LIFETIME = 15;

        private static final float DEFAULT_SIZE_MULTIPLIER = 0.75F;

        public static final Smoke DEFAULT_CONFIG = new Smoke(
                DEFAULT_ENABLED,
                DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_INFINITE_DURATION,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER
        );

        private boolean enabled;

        private boolean spawnWhileFrozen;
        private boolean infiniteDuration;

        private boolean randomSize;
        private boolean randomFadingSpeed;

        private int minLifetime;
        private int maxLifetime;

        private float sizeMultiplier;

        @Override
        public void setConfig(Smoke config) {
            this.enabled = config.enabled;

            this.spawnWhileFrozen = config.spawnWhileFrozen;
            this.infiniteDuration = config.infiniteDuration;

            this.randomSize = config.randomSize;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.sizeMultiplier = config.sizeMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);

            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);
            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);
            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Smoke copy() {
            return new Smoke(
                    this.enabled,
                    this.spawnWhileFrozen, this.infiniteDuration,
                    this.randomSize, this.randomFadingSpeed,
                    this.minLifetime, this.maxLifetime,
                    this.sizeMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CampfireSmoke implements IFBPConfig<CampfireSmoke> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final float DEFAULT_TRANSPARENCY = 0.8F;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;

        public static final CampfireSmoke DEFAULT_CONFIG = new CampfireSmoke(
                DEFAULT_ENABLED,
                DEFAULT_SPAWN_WHILE_FROZEN,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_TRANSPARENCY,
                DEFAULT_SIZE_MULTIPLIER
        );

        private boolean enabled;

        private boolean spawnWhileFrozen;

        private boolean randomSize;
        private boolean randomFadingSpeed;

        private float transparency;

        private float sizeMultiplier;

        @Override
        public void setConfig(CampfireSmoke config) {
            this.enabled = config.enabled;

            this.spawnWhileFrozen = config.spawnWhileFrozen;

            this.randomSize = config.randomSize;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.transparency = config.transparency;

            this.sizeMultiplier = config.sizeMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);

            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.transparency = GsonHelper.getAsFloat(json, "transparency", DEFAULT_TRANSPARENCY);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("transparency", this.transparency);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public CampfireSmoke copy() {
            return new CampfireSmoke(
                    this.enabled,
                    this.spawnWhileFrozen,
                    this.randomSize, this.randomFadingSpeed,
                    this.transparency,
                    this.sizeMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Trail implements IFBPConfig<Trail> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = false;
        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 30;
        private static final int DEFAULT_MAX_LIFETIME = 50;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;

        public static final Trail DEFAULT_CONFIG = new Trail(
                DEFAULT_ENABLED,
                DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_INFINITE_DURATION,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER
        );

        private boolean enabled;

        private boolean spawnWhileFrozen;
        private boolean infiniteDuration;

        private boolean randomSize;
        private boolean randomFadingSpeed;

        private int minLifetime;
        private int maxLifetime;

        private float sizeMultiplier;

        @Override
        public void setConfig(Trail config) {
            this.enabled = config.enabled;

            this.spawnWhileFrozen = config.spawnWhileFrozen;
            this.infiniteDuration = config.infiniteDuration;

            this.randomSize = config.randomSize;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.sizeMultiplier = config.sizeMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);

            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);
            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);
            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Trail copy() {
            return new Trail(
                    this.enabled,
                    this.spawnWhileFrozen, this.infiniteDuration,
                    this.randomSize, this.randomFadingSpeed,
                    this.minLifetime, this.maxLifetime,
                    this.sizeMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Firefly implements IFBPConfig<Firefly> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = false;
        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_DIMMING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 200;
        private static final int DEFAULT_MAX_LIFETIME = 300;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_SPEED_MULTIPLIER = 1.0F;

        private static final float DEFAULT_TRANSPARENCY = 0.75F;

        public static final Firefly DEFAULT_CONFIG = new Firefly(
                DEFAULT_ENABLED,
                DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_INFINITE_DURATION,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_DIMMING_SPEED,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_SPEED_MULTIPLIER,
                DEFAULT_TRANSPARENCY
        );

        private boolean enabled;

        private boolean spawnWhileFrozen;
        private boolean infiniteDuration;

        private boolean randomSize;
        private boolean randomDimmingSpeed;

        private int minLifetime;
        private int maxLifetime;

        private float sizeMultiplier;
        private float speedMultiplier;

        private float transparency;

        @Override
        public void setConfig(Firefly config) {
            this.enabled = config.enabled;

            this.spawnWhileFrozen = config.spawnWhileFrozen;
            this.infiniteDuration = config.infiniteDuration;

            this.randomSize = config.randomSize;
            this.randomDimmingSpeed = config.randomDimmingSpeed;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.sizeMultiplier = config.sizeMultiplier;
            this.speedMultiplier = config.speedMultiplier;

            this.transparency = config.transparency;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);

            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);
            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomDimmingSpeed = GsonHelper.getAsBoolean(json, "randomDimmingSpeed", DEFAULT_RANDOM_DIMMING_SPEED);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
            this.speedMultiplier = GsonHelper.getAsFloat(json, "speedMultiplier", DEFAULT_SPEED_MULTIPLIER);

            this.transparency = GsonHelper.getAsFloat(json, "transparency", DEFAULT_TRANSPARENCY);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);
            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomDimmingSpeed", this.randomDimmingSpeed);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);
            json.addProperty("speedMultiplier", this.speedMultiplier);

            json.addProperty("transparency", this.transparency);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Firefly copy() {
            return new Firefly(
                    this.enabled,
                    this.spawnWhileFrozen, this.infiniteDuration,
                    this.randomSize, this.randomDimmingSpeed,
                    this.minLifetime, this.maxLifetime,
                    this.sizeMultiplier, this.speedMultiplier,
                    this.transparency
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Rain implements IFBPConfig<Rain> {
        private static final boolean DEFAULT_ENABLED = true;
        private static final boolean DEFAULT_PUDDLE = true;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_RENDER_DISTANCE = 4;
        private static final int DEFAULT_SIMULATION_DISTANCE = 8;

        private static final float DEFAULT_TRANSPARENCY = 0.6F;
        private static final float DEFAULT_PARTICLE_DENSITY = 1.0F;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_GRAVITY_MULTIPLIER = 1.0F;

        public static final Rain DEFAULT_CONFIG = new Rain(
                DEFAULT_ENABLED, DEFAULT_PUDDLE,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_RENDER_DISTANCE, DEFAULT_SIMULATION_DISTANCE,
                DEFAULT_TRANSPARENCY, DEFAULT_PARTICLE_DENSITY,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER
        );

        private boolean enabled;
        private boolean puddle;

        private boolean randomSize;
        private boolean randomFadingSpeed;

        private int renderDistance;
        private int simulationDistance;

        private float transparency;
        private float particleDensity;

        private float sizeMultiplier;
        private float gravityMultiplier;

        @Override
        public void setConfig(Rain config) {
            this.enabled = config.enabled;
            this.puddle = config.puddle;

            this.randomSize = config.randomSize;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.renderDistance = config.renderDistance;
            this.simulationDistance = config.simulationDistance;

            this.transparency = config.transparency;
            this.particleDensity = config.particleDensity;

            this.sizeMultiplier = config.sizeMultiplier;
            this.gravityMultiplier = config.gravityMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);
            this.puddle = GsonHelper.getAsBoolean(json, "puddle", DEFAULT_PUDDLE);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.renderDistance = GsonHelper.getAsInt(json, "renderDistance", DEFAULT_RENDER_DISTANCE);
            this.simulationDistance = GsonHelper.getAsInt(json, "simulationDistance", DEFAULT_SIMULATION_DISTANCE);

            this.transparency = GsonHelper.getAsFloat(json, "transparency", DEFAULT_TRANSPARENCY);
            this.particleDensity = GsonHelper.getAsFloat(json, "particleDensity", DEFAULT_PARTICLE_DENSITY);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
            this.gravityMultiplier = GsonHelper.getAsFloat(json, "gravityMultiplier", DEFAULT_GRAVITY_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);
            json.addProperty("puddle", this.puddle);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("renderDistance", this.renderDistance);
            json.addProperty("simulationDistance", this.simulationDistance);

            json.addProperty("transparency", this.transparency);
            json.addProperty("particleDensity", this.particleDensity);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);
            json.addProperty("gravityMultiplier", this.gravityMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Rain copy() {
            return new Rain(
                    this.enabled, this.puddle,
                    this.randomSize, this.randomFadingSpeed,
                    this.renderDistance, this.simulationDistance,
                    this.transparency, this.particleDensity,
                    this.sizeMultiplier, this.gravityMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Snow implements IFBPConfig<Snow> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_LOW_TRACTION = false;
        private static final boolean DEFAULT_REST_ON_FLOOR = true;
        private static final boolean DEFAULT_BOUNCE_OFF_WALLS = true;
        private static final boolean DEFAULT_WATER_PHYSICS = true;

        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_ROTATION = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 250;
        private static final int DEFAULT_MAX_LIFETIME = 300;

        private static final int DEFAULT_RENDER_DISTANCE = 4;
        private static final int DEFAULT_SIMULATION_DISTANCE = 8;

        private static final float DEFAULT_PARTICLE_DENSITY = 1.0F;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_ROTATION_MULTIPLIER = 1.0F;
        private static final float DEFAULT_GRAVITY_MULTIPLIER = 1.0F;

        public static final Snow DEFAULT_CONFIG = new Snow(
                DEFAULT_ENABLED,
                DEFAULT_LOW_TRACTION, DEFAULT_REST_ON_FLOOR, DEFAULT_BOUNCE_OFF_WALLS, DEFAULT_WATER_PHYSICS,
                DEFAULT_INFINITE_DURATION,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_ROTATION, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_RENDER_DISTANCE, DEFAULT_SIMULATION_DISTANCE,
                DEFAULT_PARTICLE_DENSITY,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_ROTATION_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER
        );

        private boolean enabled;

        private boolean lowTraction;
        private boolean restOnFloor;
        private boolean bounceOffWalls;
        private boolean waterPhysics;

        private boolean infiniteDuration;

        private boolean randomSize;
        private boolean randomRotation;
        private boolean randomFadingSpeed;

        private int minLifetime;
        private int maxLifetime;

        private int renderDistance;
        private int simulationDistance;

        private float particleDensity;

        private float sizeMultiplier;
        private float rotationMultiplier;
        private float gravityMultiplier;

        @Override
        public void setConfig(Snow config) {
            this.enabled = config.enabled;

            this.lowTraction = config.lowTraction;
            this.restOnFloor = config.restOnFloor;
            this.bounceOffWalls = config.bounceOffWalls;
            this.waterPhysics = config.waterPhysics;

            this.infiniteDuration = config.infiniteDuration;

            this.randomSize = config.randomSize;
            this.randomRotation = config.randomRotation;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.renderDistance = config.renderDistance;
            this.simulationDistance = config.simulationDistance;

            this.particleDensity = config.particleDensity;

            this.sizeMultiplier = config.sizeMultiplier;
            this.rotationMultiplier = config.rotationMultiplier;
            this.gravityMultiplier = config.gravityMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);

            this.lowTraction = GsonHelper.getAsBoolean(json, "lowTraction", DEFAULT_LOW_TRACTION);
            this.restOnFloor = GsonHelper.getAsBoolean(json, "restOnFloor", DEFAULT_REST_ON_FLOOR);
            this.bounceOffWalls = GsonHelper.getAsBoolean(json, "bounceOffWalls", DEFAULT_BOUNCE_OFF_WALLS);
            this.waterPhysics = GsonHelper.getAsBoolean(json, "waterPhysics", DEFAULT_WATER_PHYSICS);

            this.infiniteDuration = GsonHelper.getAsBoolean(json, "infiniteDuration", DEFAULT_INFINITE_DURATION);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomRotation = GsonHelper.getAsBoolean(json, "randomRotation", DEFAULT_RANDOM_ROTATION);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.renderDistance = GsonHelper.getAsInt(json, "renderDistance", DEFAULT_RENDER_DISTANCE);
            this.simulationDistance = GsonHelper.getAsInt(json, "simulationDistance", DEFAULT_SIMULATION_DISTANCE);

            this.particleDensity = GsonHelper.getAsFloat(json, "particleDensity", DEFAULT_PARTICLE_DENSITY);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
            this.rotationMultiplier = GsonHelper.getAsFloat(json, "rotationMultiplier", DEFAULT_ROTATION_MULTIPLIER);
            this.gravityMultiplier = GsonHelper.getAsFloat(json, "gravityMultiplier", DEFAULT_GRAVITY_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("lowTraction", this.lowTraction);
            json.addProperty("restOnFloor", this.restOnFloor);
            json.addProperty("bounceOffWalls", this.bounceOffWalls);
            json.addProperty("waterPhysics", this.waterPhysics);

            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomRotation", this.randomRotation);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("renderDistance", this.renderDistance);
            json.addProperty("simulationDistance", this.simulationDistance);

            json.addProperty("particleDensity", this.particleDensity);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);
            json.addProperty("rotationMultiplier", this.rotationMultiplier);
            json.addProperty("gravityMultiplier", this.gravityMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Snow copy() {
            return new Snow(
                    this.enabled,
                    this.lowTraction, this.restOnFloor, this.bounceOffWalls, this.waterPhysics,
                    this.infiniteDuration,
                    this.randomSize, this.randomRotation, this.randomFadingSpeed,
                    this.minLifetime, this.maxLifetime,
                    this.renderDistance, this.simulationDistance,
                    this.particleDensity,
                    this.sizeMultiplier, this.rotationMultiplier, this.gravityMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Drip implements IFBPConfig<Drip> {
        private static final boolean DEFAULT_ENABLED = true;
        private static final boolean DEFAULT_PUDDLE = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 60;
        private static final int DEFAULT_MAX_LIFETIME = 100;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_GRAVITY_MULTIPLIER = 1.0F;

        public static final Drip DEFAULT_CONFIG = new Drip(
                DEFAULT_ENABLED, DEFAULT_PUDDLE,
                DEFAULT_SPAWN_WHILE_FROZEN,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER
        );

        private boolean enabled;
        private boolean puddle;

        private boolean spawnWhileFrozen;

        private boolean randomSize;
        private boolean randomFadingSpeed;

        private int minLifetime;
        private int maxLifetime;

        private float sizeMultiplier;
        private float gravityMultiplier;

        @Override
        public void setConfig(Drip config) {
            this.enabled = config.enabled;
            this.puddle = config.puddle;

            this.spawnWhileFrozen = config.spawnWhileFrozen;

            this.randomSize = config.randomSize;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.sizeMultiplier = config.sizeMultiplier;
            this.gravityMultiplier = config.gravityMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);
            this.puddle = GsonHelper.getAsBoolean(json, "puddle", DEFAULT_PUDDLE);

            this.spawnWhileFrozen = GsonHelper.getAsBoolean(json, "spawnWhileFrozen", DEFAULT_SPAWN_WHILE_FROZEN);

            this.randomSize = GsonHelper.getAsBoolean(json, "randomSize", DEFAULT_RANDOM_SIZE);
            this.randomFadingSpeed = GsonHelper.getAsBoolean(json, "randomFadingSpeed", DEFAULT_RANDOM_FADING_SPEED);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SIZE_MULTIPLIER);
            this.gravityMultiplier = GsonHelper.getAsFloat(json, "gravityMultiplier", DEFAULT_GRAVITY_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);
            json.addProperty("puddle", this.puddle);

            json.addProperty("spawnWhileFrozen", this.spawnWhileFrozen);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);
            json.addProperty("gravityMultiplier", this.gravityMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Drip copy() {
            return new Drip(
                    this.enabled, this.puddle,
                    this.spawnWhileFrozen,
                    this.randomSize, this.randomFadingSpeed,
                    this.minLifetime, this.maxLifetime,
                    this.sizeMultiplier, this.gravityMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Misc implements IFBPConfig<Misc> {
        private static final boolean DEFAULT_FANCY_SNOWBALL_PARTICLES = true;
        private static final boolean DEFAULT_FANCY_SLIME_PARTICLES = true;
        private static final boolean DEFAULT_FANCY_BREAKING_SPLASH_POTION_PARTICLES = true;

        private static final float DEFAULT_SNOWBALL_PARTICLE_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_SLIME_PARTICLE_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_BREAKING_SPLASH_POTION_PARTICLE_SIZE_MULTIPLIER = 1.0F;

        public static final Misc DEFAULT_CONFIG = new Misc(
                DEFAULT_FANCY_SNOWBALL_PARTICLES, DEFAULT_FANCY_SLIME_PARTICLES, DEFAULT_FANCY_BREAKING_SPLASH_POTION_PARTICLES,
                DEFAULT_SNOWBALL_PARTICLE_SIZE_MULTIPLIER, DEFAULT_SLIME_PARTICLE_SIZE_MULTIPLIER, DEFAULT_BREAKING_SPLASH_POTION_PARTICLE_SIZE_MULTIPLIER
        );

        private boolean fancySnowballParticles;
        private boolean fancySlimeParticles;
        private boolean fancyBreakingSplashPotionParticles;

        private float snowballParticleSizeMultiplier;
        private float slimeParticleSizeMultiplier;
        private float breakingSplashPotionParticleSizeMultiplier;

        @Override
        public void setConfig(Misc config) {
            this.fancySnowballParticles = config.fancySnowballParticles;
            this.fancySlimeParticles = config.fancySlimeParticles;
            this.fancyBreakingSplashPotionParticles = config.fancyBreakingSplashPotionParticles;

            this.snowballParticleSizeMultiplier = config.snowballParticleSizeMultiplier;
            this.slimeParticleSizeMultiplier = config.slimeParticleSizeMultiplier;
            this.breakingSplashPotionParticleSizeMultiplier = config.breakingSplashPotionParticleSizeMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.fancySnowballParticles = GsonHelper.getAsBoolean(json, "fancySnowballParticles", DEFAULT_FANCY_SNOWBALL_PARTICLES);
            this.fancySlimeParticles = GsonHelper.getAsBoolean(json, "fancySlimeParticles", DEFAULT_FANCY_SLIME_PARTICLES);
            this.fancyBreakingSplashPotionParticles = GsonHelper.getAsBoolean(json, "fancyBreakingSplashPotionParticles", DEFAULT_FANCY_BREAKING_SPLASH_POTION_PARTICLES);

            this.snowballParticleSizeMultiplier = GsonHelper.getAsFloat(json, "snowballParticleSizeMultiplier", DEFAULT_SNOWBALL_PARTICLE_SIZE_MULTIPLIER);
            this.slimeParticleSizeMultiplier = GsonHelper.getAsFloat(json, "slimeParticleSizeMultiplier", DEFAULT_SLIME_PARTICLE_SIZE_MULTIPLIER);
            this.breakingSplashPotionParticleSizeMultiplier = GsonHelper.getAsFloat(json, "breakingSplashPotionParticleSizeMultiplier", DEFAULT_BREAKING_SPLASH_POTION_PARTICLE_SIZE_MULTIPLIER);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("fancySnowballParticles", this.fancySnowballParticles);
            json.addProperty("fancySlimeParticles", this.fancySlimeParticles);
            json.addProperty("fancyBreakingSplashPotionParticles", this.fancyBreakingSplashPotionParticles);

            json.addProperty("snowballParticleSizeMultiplier", this.snowballParticleSizeMultiplier);
            json.addProperty("slimeParticleSizeMultiplier", this.slimeParticleSizeMultiplier);
            json.addProperty("breakingSplashPotionParticleSizeMultiplier", this.breakingSplashPotionParticleSizeMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Misc copy() {
            return new Misc(
                    this.fancySnowballParticles, this.fancySlimeParticles, this.fancyBreakingSplashPotionParticles,
                    this.snowballParticleSizeMultiplier, this.slimeParticleSizeMultiplier, this.breakingSplashPotionParticleSizeMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Animations implements IFBPConfig<Animations> {
        private static final boolean DEFAULT_ENABLED = true;
        private static final boolean DEFAULT_RENDER_OUTLINE = false;

        private static final int DEFAULT_MIN_LIFETIME = 3;
        private static final int DEFAULT_MAX_LIFETIME = 3;

        private static final float DEFAULT_SCALE = 1.0F;

        public static final Animations DEFAULT_CONFIG = new Animations(
                DEFAULT_ENABLED, DEFAULT_RENDER_OUTLINE,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SCALE
        );

        private boolean enabled;
        private boolean renderOutline;

        private int minLifetime;
        private int maxLifetime;

        private float sizeMultiplier;

        @Override
        public void setConfig(Animations config) {
            if (this.enabled != config.enabled)
                FBPPlacingAnimationManager.clear();

            this.enabled = config.enabled;
            this.renderOutline = config.renderOutline;

            this.minLifetime = config.minLifetime;
            this.maxLifetime = config.maxLifetime;

            this.sizeMultiplier = config.sizeMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = !Services.PLATFORM.isModLoaded("optifine") && GsonHelper.getAsBoolean(json, "enabled", DEFAULT_ENABLED);
            this.renderOutline = GsonHelper.getAsBoolean(json, "renderOutline", DEFAULT_RENDER_OUTLINE);

            this.minLifetime = GsonHelper.getAsInt(json, "minLifetime", DEFAULT_MIN_LIFETIME);
            this.maxLifetime = GsonHelper.getAsInt(json, "maxLifetime", DEFAULT_MAX_LIFETIME);

            this.sizeMultiplier = GsonHelper.getAsFloat(json, "sizeMultiplier", DEFAULT_SCALE);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("enabled", this.enabled);
            json.addProperty("renderOutline", this.renderOutline);

            json.addProperty("minLifetime", this.minLifetime);
            json.addProperty("maxLifetime", this.maxLifetime);

            json.addProperty("sizeMultiplier", this.sizeMultiplier);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Animations copy() {
            return new Animations(
                    this.enabled, this.renderOutline,
                    this.minLifetime, this.maxLifetime,
                    this.sizeMultiplier
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Overlay implements IFBPConfig<Overlay> {
        private static final boolean DEFAULT_FREEZE_EFFECT_OVERLAY = true;
        private static final int DEFAULT_FREEZE_EFFECT_COLOR = 0x0080FF;

        public static final Overlay DEFAULT_CONFIG = new Overlay(
                DEFAULT_FREEZE_EFFECT_OVERLAY, DEFAULT_FREEZE_EFFECT_COLOR
        );

        private boolean freezeEffectOverlay;
        private int freezeEffectColor;

        @Override
        public void setConfig(Overlay config) {
            this.freezeEffectOverlay = config.freezeEffectOverlay;
            this.freezeEffectColor = config.freezeEffectColor;
        }

        @Override
        public void load(JsonObject json) {
            this.freezeEffectOverlay = GsonHelper.getAsBoolean(json, "freezeEffectOverlay", DEFAULT_FREEZE_EFFECT_OVERLAY);
            this.freezeEffectColor = GsonHelper.getAsInt(json, "freezeEffectColor", DEFAULT_FREEZE_EFFECT_COLOR);
        }

        @Override
        public JsonObject save() {
            var json = new JsonObject();

            json.addProperty("freezeEffectOverlay", this.freezeEffectOverlay);
            json.addProperty("freezeEffectColor", this.freezeEffectColor);

            return json;
        }

        @Override
        public void reset() {
            this.setConfig(DEFAULT_CONFIG.copy());
        }

        @Override
        public Overlay copy() {
            return new Overlay(
                    this.freezeEffectOverlay, this.freezeEffectColor
            );
        }
    }
}
