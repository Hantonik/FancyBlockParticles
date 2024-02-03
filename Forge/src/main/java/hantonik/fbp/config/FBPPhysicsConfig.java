package hantonik.fbp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPPhysicsConfig {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

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

    public static final FBPPhysicsConfig DEFAULT_CONFIG = new FBPPhysicsConfig(
            DEFAULT_FANCY_FLAME, DEFAULT_FANCY_SMOKE, DEFAULT_FANCY_RAIN, DEFAULT_FANCY_SNOW, DEFAULT_SMOOTH_ANIMATION_LIGHTING,
            DEFAULT_SMART_BREAKING, DEFAULT_LOW_TRACTION, DEFAULT_SPAWN_WHILE_FROZEN, DEFAULT_SMOOTH_ANIMATION_LIGHTING,
            DEFAULT_SPAWN_PLACE_PARTICLES,
            DEFAULT_REST_ON_FLOOR, DEFAULT_BOUNCE_OFF_WALLS, DEFAULT_ENTITY_COLLISION, DEFAULT_WATER_PHYSICS,
            DEFAULT_RANDOM_SCALE, DEFAULT_RANDOM_ROTATION, DEFAULT_RANDOM_FADING_SPEED
    );

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

    public static FBPPhysicsConfig load() {
        var config = DEFAULT_CONFIG.copy();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> config::reload);

        return config;
    }

    public void setConfig(FBPPhysicsConfig config) {
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

        var file = new File(FBPConstants.CONFIG_PATH.toString(), "physics.json");

        try {
            if (!file.exists()) {
                file.createNewFile();

                this.reset();
                this.save();
            }

            var json = JsonParser.parseReader(new InputStreamReader(new FileInputStream(file))).getAsJsonObject();

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
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no load physics config.", e);
        }
    }

    private void save() {
        try (var writer = new FileWriter(new File(FBPConstants.CONFIG_PATH.toString(), "physics.json"))) {
            var json = new JsonObject();

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

            GSON.toJson(json, writer);
        } catch (IOException e) {
            FancyBlockParticles.LOGGER.error("Could no save physics config.", e);
        }
    }

    public FBPPhysicsConfig copy() {
        return new FBPPhysicsConfig(
                this.fancyFlame, this.fancySmoke, this.fancyRain, this.fancySnow, this.fancyPlaceAnimation,
                this.smartBreaking, this.lowTraction, this.spawnWhileFrozen, this.smoothAnimationLighting,
                this.spawnPlaceParticles,
                this.restOnFloor, this.bounceOffWalls, this.entityCollision, this.waterPhysics,
                this.randomScale, this.randomRotation, this.randomFadingSpeed
        );
    }

    @SubscribeEvent
    public void onClientLevelLoad(final LevelEvent.Load event) {
        this.reload();
    }
}
