package hantonik.fbp.config;

import com.google.common.collect.Lists;
import com.google.gson.*;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.util.FBPConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConfig implements IFBPConfig<FBPConfig> {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static final FBPConfig DEFAULT_CONFIG = new FBPConfig(
            Global.DEFAULT_CONFIG, Terrain.DEFAULT_CONFIG, FlameSmoke.DEFAULT_CONFIG, FlameSmoke.DEFAULT_CONFIG, Rain.DEFAULT_CONFIG, Snow.DEFAULT_CONFIG, Drip.DEFAULT_CONFIG, Animations.DEFAULT_CONFIG, Overlay.DEFAULT_CONFIG
    );

    public final Global global;
    public final Terrain terrain;
    public final FlameSmoke flame;
    public final FlameSmoke smoke;
    public final Rain rain;
    public final Snow snow;
    public final Drip drip;
    public final Animations animations;
    public final Overlay overlay;

    public void toggleParticles(Block block) {
        if (this.isBlockParticlesEnabled(block))
            this.global.disabledParticles.add(block);
        else
            this.global.disabledParticles.remove(block);
    }

    public void toggleAnimations(Block block) {
        if (this.isBlockAnimationsEnabled(block))
            this.global.disabledAnimations.add(block);
        else
            this.global.disabledAnimations.remove(block);
    }

    public boolean isBlockParticlesEnabled(Block block) {
        return !this.global.disabledParticles.contains(block);
    }

    public boolean isBlockAnimationsEnabled(Block block) {
        return !this.global.disabledAnimations.contains(block);
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
        this.rain.setConfig(config.rain);
        this.snow.setConfig(config.snow);
        this.drip.setConfig(config.drip);
        this.animations.setConfig(config.animations);
        this.overlay.setConfig(config.overlay);
    }

    @Override
    public void applyConfig(FBPConfig config) {
        this.global.applyConfig(config.global);
        this.terrain.applyConfig(config.terrain);
        this.flame.applyConfig(config.flame);
        this.smoke.applyConfig(config.smoke);
        this.rain.applyConfig(config.rain);
        this.snow.applyConfig(config.snow);
        this.drip.applyConfig(config.drip);
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

        File file = new File(FBPConstants.CONFIG_PATH.toString(), "config.json");

        try {
            if (!file.exists()) {
                file.createNewFile();

                this.save();
            }

            JsonObject json = new JsonParser().parse(new InputStreamReader(Files.newInputStream(file.toPath()))).getAsJsonObject();

            this.global.load(json.has("global") ? json.getAsJsonObject("global") : new JsonObject());
            this.terrain.load(json.has("terrain") ? json.getAsJsonObject("terrain") : new JsonObject());
            this.flame.load(json.has("flame") ? json.getAsJsonObject("flame") : new JsonObject());
            this.smoke.load(json.has("smoke") ? json.getAsJsonObject("smoke") : new JsonObject());
            this.rain.load(json.has("rain") ? json.getAsJsonObject("rain") : new JsonObject());
            this.snow.load(json.has("snow") ? json.getAsJsonObject("snow") : new JsonObject());
            this.drip.load(json.has("drip") ? json.getAsJsonObject("drip") : new JsonObject());
            this.animations.load(json.has("animations") ? json.getAsJsonObject("animations") : new JsonObject());
            this.overlay.load(json.has("overlay") ? json.getAsJsonObject("overlay") : new JsonObject());
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no load FBP config.", e);
        } catch (JsonSyntaxException e) {
            FancyBlockParticles.LOGGER.warn("FBP config file is corrupt! Generating a new one.");

            this.save();
        }
    }

    @Override
    public JsonObject save() {
        JsonObject json = new JsonObject();

        try (FileWriter writer = new FileWriter(new File(FBPConstants.CONFIG_PATH.toString(), "config.json"))) {
            json.add("global", this.global.save());
            json.add("terrain", this.terrain.save());
            json.add("flame", this.flame.save());
            json.add("smoke", this.smoke.save());
            json.add("rain", this.rain.save());
            json.add("snow", this.snow.save());
            json.add("drip", this.drip.save());
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
                this.global.copy(), this.terrain.copy(), this.flame.copy(), this.smoke.copy(), this.rain.copy(), this.snow.copy(), this.drip.copy(), this.animations.copy(), this.overlay.copy()
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Global implements IFBPConfig<Global> {
        private static final boolean DEFAULT_ENABLED = true;
        private static final boolean DEFAULT_LOCKED = false;
        private static final boolean DEFAULT_FREEZE_EFFECT = false;

        private static final boolean DEFAULT_CARTOON_MODE = false;
        private static final boolean DEFAULT_CULL_PARTICLES = true;

        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final List<Block> DEFAULT_DISABLED_PARTICLES = Lists.newArrayList();
        private static final List<Block> DEFAULT_DISABLED_ANIMATIONS = Lists.newArrayList();

        public static final Global DEFAULT_CONFIG = new Global(
                DEFAULT_ENABLED, DEFAULT_LOCKED, DEFAULT_FREEZE_EFFECT,
                DEFAULT_CARTOON_MODE, DEFAULT_CULL_PARTICLES,
                DEFAULT_INFINITE_DURATION,
                DEFAULT_DISABLED_PARTICLES, DEFAULT_DISABLED_ANIMATIONS
        );

        private boolean enabled;
        private boolean locked;
        private boolean freezeEffect;

        private boolean cartoonMode;
        private boolean cullParticles;

        private boolean infiniteDuration;

        @Setter(AccessLevel.PRIVATE)
        private List<Block> disabledParticles;

        @Setter(AccessLevel.PRIVATE)
        private List<Block> disabledAnimations;

        @Override
        public void setConfig(Global config) {
            this.enabled = config.enabled;
            this.locked = config.locked;
            this.freezeEffect = config.freezeEffect;

            this.cartoonMode = config.cartoonMode;
            this.cullParticles = config.cullParticles;

            this.infiniteDuration = config.infiniteDuration;

            this.disabledParticles = new ArrayList<>(config.disabledParticles);
            this.disabledAnimations = new ArrayList<>(config.disabledAnimations);
        }

        @Override
        public void applyConfig(Global config) {
            this.enabled = config.enabled;
            this.locked = config.locked;
            this.freezeEffect = config.freezeEffect;

            this.cartoonMode = config.cartoonMode;
            this.cullParticles = config.cullParticles;

            this.infiniteDuration = config.infiniteDuration;

            this.disabledParticles.addAll(config.disabledParticles);
            this.disabledAnimations.addAll(config.disabledAnimations);
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = json.has("enabled") ? json.getAsJsonPrimitive("enabled").getAsBoolean() : DEFAULT_ENABLED;
            this.locked = json.has("locked") ? json.getAsJsonPrimitive("locked").getAsBoolean() : DEFAULT_LOCKED;
            this.freezeEffect = json.has("freezeEffect") ? json.getAsJsonPrimitive("freezeEffect").getAsBoolean() : DEFAULT_FREEZE_EFFECT;

            this.cartoonMode = json.has("cartoonMode") ? json.getAsJsonPrimitive("cartoonMode").getAsBoolean() : DEFAULT_CARTOON_MODE;
            this.cullParticles = json.has("cullParticles") ? json.getAsJsonPrimitive("cullParticles").getAsBoolean() : DEFAULT_CULL_PARTICLES;

            this.infiniteDuration = json.has("infiniteDuration") ? json.getAsJsonPrimitive("infiniteDuration").getAsBoolean() : DEFAULT_INFINITE_DURATION;

            this.disabledParticles = Util.make(Lists.newArrayList(), disabled -> {
                if (json.has("disabledParticles")) {
                    for (JsonElement entry : json.getAsJsonArray("disabledParticles"))
                        disabled.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
                } else
                    disabled.addAll(DEFAULT_DISABLED_PARTICLES);
            });

            this.disabledAnimations = Util.make(Lists.newArrayList(), disabled -> {
                if (json.has("disabledAnimations")) {
                    for (JsonElement entry : json.getAsJsonArray("disabledAnimations"))
                        disabled.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
                } else
                    disabled.addAll(DEFAULT_DISABLED_ANIMATIONS);
            });
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

            json.addProperty("enabled", this.enabled);
            json.addProperty("locked", this.locked);
            json.addProperty("freezeEffect", this.freezeEffect);

            json.addProperty("cartoonMode", this.cartoonMode);
            json.addProperty("cullParticles", this.cullParticles);

            json.addProperty("infiniteDuration", this.infiniteDuration);

            json.add("disabledParticles", Util.make(new JsonArray(), disabled -> {
                for (Block entry : this.disabledParticles)
                    disabled.add(ForgeRegistries.BLOCKS.getKey(entry).toString());
            }));

            json.add("disabledAnimations", Util.make(new JsonArray(), disabled -> {
                for (Block entry : this.disabledAnimations)
                    disabled.add(ForgeRegistries.BLOCKS.getKey(entry).toString());
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
                    this.enabled, this.locked, this.freezeEffect,
                    this.cartoonMode, this.cullParticles,
                    this.infiniteDuration,
                    new ArrayList<>(this.disabledParticles), new ArrayList<>(this.disabledAnimations)
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Terrain implements IFBPConfig<Terrain> {
        private static final boolean DEFAULT_FANCY_BREAKING_PARTICLES = true;
        private static final boolean DEFAULT_FANCY_CRACKING_PARTICLES = true;

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
                DEFAULT_FANCY_BREAKING_PARTICLES, DEFAULT_FANCY_CRACKING_PARTICLES,
                DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_INFINITE_DURATION,
                DEFAULT_SMART_BREAKING, DEFAULT_LOW_TRACTION, DEFAULT_REST_ON_FLOOR, DEFAULT_BOUNCE_OFF_WALLS, DEFAULT_ENTITY_COLLISION, DEFAULT_WATER_PHYSICS,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_ROTATION, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_PARTICLES_PER_AXIS,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_ROTATION_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER
        );
        
        private boolean fancyBreakingParticles;
        private boolean fancyCrackingParticles;
        
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
            this.fancyBreakingParticles = json.has("fancyBreakingParticles") ? json.getAsJsonPrimitive("fancyBreakingParticles").getAsBoolean() : DEFAULT_FANCY_BREAKING_PARTICLES;
            this.fancyCrackingParticles = json.has("fancyCrackingParticles") ? json.getAsJsonPrimitive("fancyCrackingParticles").getAsBoolean() : DEFAULT_FANCY_CRACKING_PARTICLES;

            this.spawnWhileFrozen = json.has("spawnWhileFrozen") ? json.getAsJsonPrimitive("spawnWhileFrozen").getAsBoolean() : DEFAULT_SPAWN_WHILE_FROZEN;
            this.infiniteDuration = json.has("infiniteDuration") ? json.getAsJsonPrimitive("infiniteDuration").getAsBoolean() : DEFAULT_INFINITE_DURATION;

            this.smartBreaking = json.has("smartBreaking") ? json.getAsJsonPrimitive("smartBreaking").getAsBoolean() : DEFAULT_SMART_BREAKING;
            this.lowTraction = json.has("lowTraction") ? json.getAsJsonPrimitive("lowTraction").getAsBoolean() : DEFAULT_LOW_TRACTION;
            this.restOnFloor = json.has("restOnFloor") ? json.getAsJsonPrimitive("restOnFloor").getAsBoolean() : DEFAULT_REST_ON_FLOOR;
            this.bounceOffWalls = json.has("bounceOffWalls") ? json.getAsJsonPrimitive("bounceOffWalls").getAsBoolean() : DEFAULT_BOUNCE_OFF_WALLS;
            this.entityCollision = json.has("entityCollision") ? json.getAsJsonPrimitive("entityCollision").getAsBoolean() : DEFAULT_ENTITY_COLLISION;
            this.waterPhysics = json.has("waterPhysics") ? json.getAsJsonPrimitive("waterPhysics").getAsBoolean() : DEFAULT_WATER_PHYSICS;

            this.randomSize = json.has("randomSize") ? json.getAsJsonPrimitive("randomSize").getAsBoolean() : DEFAULT_RANDOM_SIZE;
            this.randomRotation = json.has("randomRotation") ? json.getAsJsonPrimitive("randomRotation").getAsBoolean() : DEFAULT_RANDOM_ROTATION;
            this.randomFadingSpeed = json.has("randomFadingSpeed") ? json.getAsJsonPrimitive("randomFadingSpeed").getAsBoolean() : DEFAULT_RANDOM_FADING_SPEED;

            this.particlesPerAxis = json.has("particlesPerAxis") ? json.getAsJsonPrimitive("particlesPerAxis").getAsInt() : DEFAULT_PARTICLES_PER_AXIS;

            this.minLifetime = json.has("minLifetime") ? json.getAsJsonPrimitive("minLifetime").getAsInt() : DEFAULT_MIN_LIFETIME;
            this.maxLifetime = json.has("maxLifetime") ? json.getAsJsonPrimitive("maxLifetime").getAsInt() : DEFAULT_MAX_LIFETIME;

            this.sizeMultiplier = json.has("sizeMultiplier") ? json.getAsJsonPrimitive("sizeMultiplier").getAsFloat() : DEFAULT_SIZE_MULTIPLIER;
            this.rotationMultiplier = json.has("rotationMultiplier") ? json.getAsJsonPrimitive("rotationMultiplier").getAsFloat() : DEFAULT_ROTATION_MULTIPLIER;
            this.gravityMultiplier = json.has("gravityMultiplier") ? json.getAsJsonPrimitive("gravityMultiplier").getAsFloat() : DEFAULT_GRAVITY_MULTIPLIER;
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

            json.addProperty("fancyBreakingParticles", this.fancyBreakingParticles);
            json.addProperty("fancyCrackingParticles", this.fancyCrackingParticles);

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
                    this.fancyBreakingParticles, this.fancyCrackingParticles,
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
    public static class FlameSmoke implements IFBPConfig<FlameSmoke> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = false;
        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 10;
        private static final int DEFAULT_MAX_LIFETIME = 15;

        private static final float DEFAULT_SIZE_MULTIPLIER = 0.75F;

        public static final FlameSmoke DEFAULT_CONFIG = new FlameSmoke(
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
        public void setConfig(FlameSmoke config) {
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
            this.enabled = json.has("enabled") ? json.getAsJsonPrimitive("enabled").getAsBoolean() : DEFAULT_ENABLED;

            this.spawnWhileFrozen = json.has("spawnWhileFrozen") ? json.getAsJsonPrimitive("spawnWhileFrozen").getAsBoolean() : DEFAULT_SPAWN_WHILE_FROZEN;
            this.infiniteDuration = json.has("infiniteDuration") ? json.getAsJsonPrimitive("infiniteDuration").getAsBoolean() : DEFAULT_INFINITE_DURATION;

            this.randomSize = json.has("infiniteDuration") ? json.getAsJsonPrimitive("infiniteDuration").getAsBoolean() : DEFAULT_RANDOM_SIZE;
            this.randomFadingSpeed = json.has("randomFadingSpeed") ? json.getAsJsonPrimitive("randomFadingSpeed").getAsBoolean() : DEFAULT_RANDOM_FADING_SPEED;

            this.minLifetime = json.has("minLifetime") ? json.getAsJsonPrimitive("minLifetime").getAsInt() : DEFAULT_MIN_LIFETIME;
            this.maxLifetime = json.has("maxLifetime") ? json.getAsJsonPrimitive("maxLifetime").getAsInt() : DEFAULT_MAX_LIFETIME;

            this.sizeMultiplier = json.has("sizeMultiplier") ? json.getAsJsonPrimitive("sizeMultiplier").getAsFloat() : DEFAULT_SIZE_MULTIPLIER;
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

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
        public FlameSmoke copy() {
            return new FlameSmoke(
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
    public static class Rain implements IFBPConfig<Rain> {
        private static final boolean DEFAULT_ENABLED = true;

        private static final boolean DEFAULT_WATER_PHYSICS = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_RENDER_DISTANCE = 4;
        private static final int DEFAULT_SIMULATION_DISTANCE = 12;

        private static final float DEFAULT_PARTICLE_DENSITY = 1.0F;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_GRAVITY_MULTIPLIER = 1.0F;

        public static final Rain DEFAULT_CONFIG = new Rain(
                DEFAULT_ENABLED,
                DEFAULT_WATER_PHYSICS,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_RENDER_DISTANCE, DEFAULT_SIMULATION_DISTANCE,
                DEFAULT_PARTICLE_DENSITY,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER
        );

        private boolean enabled;

        private boolean waterPhysics;

        private boolean randomSize;
        private boolean randomFadingSpeed;

        private int renderDistance;
        private int simulationDistance;

        private float particleDensity;

        private float sizeMultiplier;
        private float gravityMultiplier;

        @Override
        public void setConfig(Rain config) {
            this.enabled = config.enabled;

            this.waterPhysics = config.waterPhysics;

            this.randomSize = config.randomSize;
            this.randomFadingSpeed = config.randomFadingSpeed;

            this.renderDistance = config.renderDistance;
            this.simulationDistance = config.simulationDistance;

            this.particleDensity = config.particleDensity;

            this.sizeMultiplier = config.sizeMultiplier;
            this.gravityMultiplier = config.gravityMultiplier;
        }

        @Override
        public void load(JsonObject json) {
            this.enabled = json.has("enabled") ? json.getAsJsonPrimitive("enabled").getAsBoolean() : DEFAULT_ENABLED;

            this.waterPhysics = json.has("waterPhysics") ? json.getAsJsonPrimitive("waterPhysics").getAsBoolean() : DEFAULT_WATER_PHYSICS;

            this.randomSize = json.has("randomSize") ? json.getAsJsonPrimitive("randomSize").getAsBoolean() : DEFAULT_RANDOM_SIZE;
            this.randomFadingSpeed = json.has("randomFadingSpeed") ? json.getAsJsonPrimitive("randomFadingSpeed").getAsBoolean() : DEFAULT_RANDOM_FADING_SPEED;

            this.renderDistance = json.has("renderDistance") ? json.getAsJsonPrimitive("renderDistance").getAsInt() : DEFAULT_RENDER_DISTANCE;
            this.simulationDistance = json.has("simulationDistance") ? json.getAsJsonPrimitive("simulationDistance").getAsInt() : DEFAULT_SIMULATION_DISTANCE;

            this.particleDensity = json.has("particleDensity") ? json.getAsJsonPrimitive("particleDensity").getAsFloat() : DEFAULT_PARTICLE_DENSITY;

            this.sizeMultiplier = json.has("sizeMultiplier") ? json.getAsJsonPrimitive("sizeMultiplier").getAsFloat() : DEFAULT_SIZE_MULTIPLIER;
            this.gravityMultiplier = json.has("gravityMultiplier") ? json.getAsJsonPrimitive("gravityMultiplier").getAsFloat() : DEFAULT_GRAVITY_MULTIPLIER;
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

            json.addProperty("enabled", this.enabled);

            json.addProperty("waterPhysics", this.waterPhysics);

            json.addProperty("randomSize", this.randomSize);
            json.addProperty("randomFadingSpeed", this.randomFadingSpeed);

            json.addProperty("renderDistance", this.renderDistance);
            json.addProperty("simulationDistance", this.simulationDistance);

            json.addProperty("rainParticleDensity", this.particleDensity);

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
                    this.enabled,
                    this.waterPhysics,
                    this.randomSize, this.randomFadingSpeed,
                    this.renderDistance, this.simulationDistance,
                    this.particleDensity,
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
        private static final boolean DEFAULT_WATER_PHYSICS = false;

        private static final boolean DEFAULT_INFINITE_DURATION = false;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_ROTATION = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 250;
        private static final int DEFAULT_MAX_LIFETIME = 300;

        private static final int DEFAULT_RENDER_DISTANCE = 4;
        private static final int DEFAULT_SIMULATION_DISTANCE = 12;

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
            this.enabled = json.has("enabled") ? json.getAsJsonPrimitive("enabled").getAsBoolean() : DEFAULT_ENABLED;

            this.lowTraction = json.has("lowTraction") ? json.getAsJsonPrimitive("lowTraction").getAsBoolean() : DEFAULT_LOW_TRACTION;
            this.restOnFloor = json.has("restOnFloor") ? json.getAsJsonPrimitive("restOnFloor").getAsBoolean() : DEFAULT_REST_ON_FLOOR;
            this.bounceOffWalls = json.has("bounceOffWalls") ? json.getAsJsonPrimitive("bounceOffWalls").getAsBoolean() : DEFAULT_BOUNCE_OFF_WALLS;
            this.waterPhysics = json.has("waterPhysics") ? json.getAsJsonPrimitive("waterPhysics").getAsBoolean() : DEFAULT_WATER_PHYSICS;

            this.infiniteDuration = json.has("infiniteDuration") ? json.getAsJsonPrimitive("infiniteDuration").getAsBoolean() : DEFAULT_INFINITE_DURATION;

            this.randomSize = json.has("randomSize") ? json.getAsJsonPrimitive("randomSize").getAsBoolean() : DEFAULT_RANDOM_SIZE;
            this.randomRotation = json.has("randomRotation") ? json.getAsJsonPrimitive("randomRotation").getAsBoolean() : DEFAULT_RANDOM_ROTATION;
            this.randomFadingSpeed = json.has("randomFadingSpeed") ? json.getAsJsonPrimitive("randomFadingSpeed").getAsBoolean() : DEFAULT_RANDOM_FADING_SPEED;

            this.minLifetime = json.has("minLifetime") ? json.getAsJsonPrimitive("minLifetime").getAsInt() : DEFAULT_MIN_LIFETIME;
            this.maxLifetime = json.has("maxLifetime") ? json.getAsJsonPrimitive("maxLifetime").getAsInt() : DEFAULT_MAX_LIFETIME;

            this.renderDistance = json.has("renderDistance") ? json.getAsJsonPrimitive("renderDistance").getAsInt() : DEFAULT_RENDER_DISTANCE;
            this.simulationDistance = json.has("simulationDistance") ? json.getAsJsonPrimitive("simulationDistance").getAsInt() : DEFAULT_SIMULATION_DISTANCE;

            this.particleDensity = json.has("particleDensity") ? json.getAsJsonPrimitive("particleDensity").getAsFloat() : DEFAULT_PARTICLE_DENSITY;

            this.sizeMultiplier = json.has("sizeMultiplier") ? json.getAsJsonPrimitive("sizeMultiplier").getAsFloat() : DEFAULT_SIZE_MULTIPLIER;
            this.rotationMultiplier = json.has("rotationMultiplier") ? json.getAsJsonPrimitive("rotationMultiplier").getAsFloat() : DEFAULT_ROTATION_MULTIPLIER;
            this.gravityMultiplier = json.has("gravityMultiplier") ? json.getAsJsonPrimitive("gravityMultiplier").getAsFloat() : DEFAULT_GRAVITY_MULTIPLIER;
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

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

            json.addProperty("snowParticleDensity", this.particleDensity);

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

        private static final boolean DEFAULT_SPAWN_WHILE_FROZEN = true;

        private static final boolean DEFAULT_RANDOM_SIZE = true;
        private static final boolean DEFAULT_RANDOM_FADING_SPEED = true;

        private static final int DEFAULT_MIN_LIFETIME = 60;
        private static final int DEFAULT_MAX_LIFETIME = 100;

        private static final float DEFAULT_SIZE_MULTIPLIER = 1.0F;
        private static final float DEFAULT_GRAVITY_MULTIPLIER = 1.0F;

        public static final Drip DEFAULT_CONFIG = new Drip(
                DEFAULT_ENABLED,
                DEFAULT_SPAWN_WHILE_FROZEN,
                DEFAULT_RANDOM_SIZE, DEFAULT_RANDOM_FADING_SPEED,
                DEFAULT_MIN_LIFETIME, DEFAULT_MAX_LIFETIME,
                DEFAULT_SIZE_MULTIPLIER, DEFAULT_GRAVITY_MULTIPLIER
        );

        private boolean enabled;

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
            this.enabled = json.has("enabled") ? json.getAsJsonPrimitive("enabled").getAsBoolean() : DEFAULT_ENABLED;

            this.spawnWhileFrozen = json.has("spawnWhileFrozen") ? json.getAsJsonPrimitive("spawnWhileFrozen").getAsBoolean() : DEFAULT_SPAWN_WHILE_FROZEN;

            this.randomSize = json.has("randomSize") ? json.getAsJsonPrimitive("randomSize").getAsBoolean() : DEFAULT_RANDOM_SIZE;
            this.randomFadingSpeed = json.has("randomFadingSpeed") ? json.getAsJsonPrimitive("randomFadingSpeed").getAsBoolean() : DEFAULT_RANDOM_FADING_SPEED;

            this.minLifetime = json.has("minLifetime") ? json.getAsJsonPrimitive("minLifetime").getAsInt() : DEFAULT_MIN_LIFETIME;
            this.maxLifetime = json.has("maxLifetime") ? json.getAsJsonPrimitive("maxLifetime").getAsInt() : DEFAULT_MAX_LIFETIME;

            this.sizeMultiplier = json.has("sizeMultiplier") ? json.getAsJsonPrimitive("sizeMultiplier").getAsFloat() : DEFAULT_SIZE_MULTIPLIER;
            this.gravityMultiplier = json.has("gravityMultiplier") ? json.getAsJsonPrimitive("gravityMultiplier").getAsFloat() : DEFAULT_GRAVITY_MULTIPLIER;
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

            json.addProperty("enabled", this.enabled);

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
                    this.enabled,
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
            this.enabled = !ModList.get().isLoaded("optifine") && (json.has("enabled") ? json.getAsJsonPrimitive("enabled").getAsBoolean() : DEFAULT_ENABLED);
            this.renderOutline = json.has("renderOutline") ? json.getAsJsonPrimitive("renderOutline").getAsBoolean() : DEFAULT_RENDER_OUTLINE;

            this.minLifetime = json.has("minLifetime") ? json.getAsJsonPrimitive("minLifetime").getAsInt() : DEFAULT_MIN_LIFETIME;
            this.maxLifetime = json.has("maxLifetime") ? json.getAsJsonPrimitive("maxLifetime").getAsInt() : DEFAULT_MAX_LIFETIME;

            this.sizeMultiplier = json.has("sizeMultiplier") ? json.getAsJsonPrimitive("sizeMultiplier").getAsFloat() : DEFAULT_SCALE;
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

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
            this.freezeEffectOverlay = json.has("freezeEffectOverlay") ? json.getAsJsonPrimitive("freezeEffectOverlay").getAsBoolean() : DEFAULT_FREEZE_EFFECT_OVERLAY;
            this.freezeEffectColor = json.has("freezeEffectColor") ? json.getAsJsonPrimitive("freezeEffectColor").getAsInt() : DEFAULT_FREEZE_EFFECT_COLOR;
        }

        @Override
        public JsonObject save() {
            JsonObject json = new JsonObject();

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
