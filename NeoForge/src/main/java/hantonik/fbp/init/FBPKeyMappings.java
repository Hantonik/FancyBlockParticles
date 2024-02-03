package hantonik.fbp.init;

import com.google.common.collect.Lists;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class FBPKeyMappings {
    private static final List<KeyMapping> MAPPINGS = Lists.newArrayList();

    public static final KeyMapping TOGGLE = register("toggle", -1);
    public static final KeyMapping SETTINGS = register("settings", GLFW.GLFW_KEY_I);
    public static final KeyMapping FREEZE = register("freeze", GLFW.GLFW_KEY_R);
    public static final KeyMapping KILL_PARTICLES = register("kill_particles", -1);
    public static final KeyMapping FAST_BLACKLIST = register("fast_blacklist", GLFW.GLFW_KEY_X);

    private static KeyMapping register(String name, int keyCode) {
        var mapping = new KeyMapping("key.fbp." + name, keyCode, "key.categories.fbp");
        MAPPINGS.add(mapping);

        return mapping;
    }

    @SubscribeEvent
    public void register(final RegisterKeyMappingsEvent event) {
        MAPPINGS.forEach(event::register);
    }
}
