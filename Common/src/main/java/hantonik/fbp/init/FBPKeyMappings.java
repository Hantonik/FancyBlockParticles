package hantonik.fbp.init;

import com.google.common.collect.Lists;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class FBPKeyMappings {
    public static final List<KeyMapping> MAPPINGS = Lists.newArrayList();

    public static final KeyMapping TOGGLE_MOD = register("toggle_mod", -1);
    public static final KeyMapping OPEN_SETTINGS = register("open_settings", GLFW.GLFW_KEY_I);
    public static final KeyMapping FREEZE_PARTICLES = register("freeze_particles", GLFW.GLFW_KEY_R);
    public static final KeyMapping KILL_PARTICLES = register("kill_particles", -1);
    public static final KeyMapping ADD_TO_BLACKLIST = register("add_to_blacklist", GLFW.GLFW_KEY_X);
    public static final KeyMapping RELOAD_CONFIG = register("reload_config", -1);

    private static KeyMapping register(String name, int keyCode) {
        var mapping = new KeyMapping("key.fbp." + name, keyCode, "key.fbp.category");
        MAPPINGS.add(mapping);

        return mapping;
    }
}
