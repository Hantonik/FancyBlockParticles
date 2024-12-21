package hantonik.fbp.util;

import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.platform.Services;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SplittableRandom;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConstants {
    public static final Path CONFIG_PATH = Paths.get(Services.PLATFORM.getConfigDir().toAbsolutePath().toString(), FancyBlockParticles.MOD_ID);

    public static final SplittableRandom RANDOM = new SplittableRandom();

    public static final Vector3d[] CUBE = new Vector3d[] {
            new Vector3d(1.0D, 1.0D, -1.0D),   new Vector3d(1.0D, 1.0D, 1.0D),   new Vector3d(-1.0D, 1.0D, 1.0D),  new Vector3d(-1.0D, 1.0D, -1.0D),
            new Vector3d(-1.0D, -1.0D, -1.0D), new Vector3d(-1.0D, -1.0D, 1.0D), new Vector3d(1.0D, -1.0D, 1.0D),  new Vector3d(1.0D, -1.0D, -1.0D),
            new Vector3d(-1.0D, -1.0D, 1.0D),  new Vector3d(-1.0D, 1.0D, 1.0D),  new Vector3d(1.0D, 1.0D, 1.0D),   new Vector3d(1.0D, -1.0D, 1.0D),
            new Vector3d(1.0D, -1.0D, -1.0D),  new Vector3d(1.0D, 1.0D, -1.0D),  new Vector3d(-1.0D, 1.0D, -1.0D), new Vector3d(-1.0D, -1.0D, -1.0D),
            new Vector3d(-1.0D, -1.0D, -1.0D), new Vector3d(-1.0D, 1.0D, -1.0D), new Vector3d(-1.0D, 1.0D, 1.0D),  new Vector3d(-1.0D, -1.0D, 1.0D),
            new Vector3d(1.0D, -1.0D, 1.0D),   new Vector3d(1.0D, 1.0D, 1.0D),   new Vector3d(1.0D, 1.0D, -1.0D),  new Vector3d(1.0D, -1.0D, -1.0D)
    };

    public static final Vector3d[] CUBE_NORMALS = new Vector3d[] {
            new Vector3d(0.0D, 1.0D, 0.0D), new Vector3d(0.0D, -1.0D, 0.0D),
            new Vector3d(0.0D, 0.0D, 1.0D), new Vector3d(0.0D, 0.0D, -1.0D),
            new Vector3d(-1.0D, 0.0D, 0.0D), new Vector3d(1.0D, 0.0D, 0.0D)
    };

    public static final Vec3 ANIMATION_TRANSLATION = new Vec3(0.1F, 0.1F, 0.1F);
    public static final Vec3 ANIMATION_ROTATION = new Vec3(0.0F, -0.07F, 0.07F);
    public static final Vec3 ANIMATION_PIVOT = new Vec3(0.25F, -0.25F, 0.25F);

    public static final Supplier<TextureAtlasSprite> FBP_PARTICLE_SPRITE = () -> Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WHITE_CONCRETE.defaultBlockState());

    public static final ParticleRenderType FBP_PARTICLE_RENDER = new ParticleRenderType("FBP_PARTICLE_RENDER", RenderType.translucentParticle(TextureAtlas.LOCATION_BLOCKS));
}
