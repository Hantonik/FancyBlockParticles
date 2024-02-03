package hantonik.fbp.init;

import com.google.common.collect.Lists;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class FBPKeyMappings {
    private static final List<Lazy<KeyMapping>> MAPPINGS = Lists.newArrayList();

    public static final Lazy<KeyMapping> TOGGLE = register("toggle", -1);
    public static final Lazy<KeyMapping> SETTINGS = register("settings", GLFW.GLFW_KEY_I);
    public static final Lazy<KeyMapping> FREEZE = register("freeze", GLFW.GLFW_KEY_R);
    public static final Lazy<KeyMapping> KILL_PARTICLES = register("kill_particles", -1);
    public static final Lazy<KeyMapping> FAST_BLACKLIST = register("fast_blacklist", GLFW.GLFW_KEY_X);

    private static Lazy<KeyMapping> register(String name, int keyCode) {
        var mapping = Lazy.of(() -> new KeyMapping("key.fbp." + name, keyCode, "key.categories.fbp"));
        MAPPINGS.add(mapping);

        return mapping;
    }

    @SubscribeEvent
    public void register(final RegisterKeyMappingsEvent event) {
        for (var mapping : MAPPINGS)
            event.register(mapping.get());
    }
}
