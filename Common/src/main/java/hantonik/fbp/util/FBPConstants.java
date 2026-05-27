package hantonik.fbp.util;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.platform.Services;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SplittableRandom;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConstants {
    public static final Path CONFIG_PATH = Paths.get(Services.PLATFORM.getConfigDir().toAbsolutePath().toString(), FancyBlockParticles.MOD_ID);

    public static final SplittableRandom RANDOM = new SplittableRandom();

    public static final Vector3f[] CUBE = new Vector3f[] {
            new Vector3f(1.0F, 1.0F, -1.0F),   new Vector3f(1.0F, 1.0F, 1.0F),   new Vector3f(-1.0F, 1.0F, 1.0F),  new Vector3f(-1.0F, 1.0F, -1.0F),
            new Vector3f(-1.0F, -1.0F, -1.0F), new Vector3f(-1.0F, -1.0F, 1.0F), new Vector3f(1.0F, -1.0F, 1.0F),  new Vector3f(1.0F, -1.0F, -1.0F),
            new Vector3f(-1.0F, -1.0F, 1.0F),  new Vector3f(-1.0F, 1.0F, 1.0F),  new Vector3f(1.0F, 1.0F, 1.0F),   new Vector3f(1.0F, -1.0F, 1.0F),
            new Vector3f(1.0F, -1.0F, -1.0F),  new Vector3f(1.0F, 1.0F, -1.0F),  new Vector3f(-1.0F, 1.0F, -1.0F), new Vector3f(-1.0F, -1.0F, -1.0F),
            new Vector3f(-1.0F, -1.0F, -1.0F), new Vector3f(-1.0F, 1.0F, -1.0F), new Vector3f(-1.0F, 1.0F, 1.0F),  new Vector3f(-1.0F, -1.0F, 1.0F),
            new Vector3f(1.0F, -1.0F, 1.0F),   new Vector3f(1.0F, 1.0F, 1.0F),   new Vector3f(1.0F, 1.0F, -1.0F),  new Vector3f(1.0F, -1.0F, -1.0F)
    };

    public static final Vector3f[] CUBE_NORMALS = new Vector3f[] {
            new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(0.0F, -1.0F, 0.0F),
            new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(0.0F, 0.0F, -1.0F),
            new Vector3f(-1.0F, 0.0F, 0.0F), new Vector3f(1.0F, 0.0F, 0.0F)
    };

    public static final Vec3 ANIMATION_TRANSLATION = new Vec3(0.1F, 0.1F, 0.1F);
    public static final Vec3 ANIMATION_ROTATION = new Vec3(0.0F, -0.07F, 0.07F);
    public static final Vec3 ANIMATION_PIVOT = new Vec3(0.25F, -0.25F, 0.25F);

    public static final Supplier<TextureAtlasSprite> FBP_PARTICLE_SPRITE = () -> Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(Blocks.WHITE_CONCRETE.defaultBlockState()).sprite();

    public static final ParticleRenderType FBP_PARTICLE_RENDER = new ParticleRenderType("fbp:particle_render");
    public static final ParticleRenderType FBP_TERRAIN_RENDER = new ParticleRenderType("fbp:terrain_render");
    public static final ParticleRenderType FBP_ANIMATION_RENDER = new ParticleRenderType("fbp:animation_render");
}
