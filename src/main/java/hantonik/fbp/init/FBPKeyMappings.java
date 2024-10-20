package hantonik.fbp.init;

import com.google.common.collect.Lists;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class FBPKeyMappings {
    public static final List<KeyBinding> MAPPINGS = Lists.newArrayList();

    public static final KeyBinding TOGGLE_MOD = register("toggle_mod", -1);
    public static final KeyBinding TOGGLE_ANIMATIONS = register("toggle_animations", -1);
    public static final KeyBinding OPEN_SETTINGS = register("open_settings", GLFW.GLFW_KEY_I);
    public static final KeyBinding FREEZE_PARTICLES = register("freeze_particles", GLFW.GLFW_KEY_R);
    public static final KeyBinding KILL_PARTICLES = register("kill_particles", -1);
    public static final KeyBinding ADD_TO_BLACKLIST = register("add_to_blacklist", GLFW.GLFW_KEY_X);
    public static final KeyBinding RELOAD_CONFIG = register("reload_config", -1);

    private static KeyBinding register(String name, int keyCode) {
        KeyBinding mapping = new KeyBinding("key.fbp." + name, keyCode, "key.fbp.category");
        MAPPINGS.add(mapping);

        return mapping;
    }
}
