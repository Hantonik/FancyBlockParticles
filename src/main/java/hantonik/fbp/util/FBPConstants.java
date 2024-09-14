package hantonik.fbp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import hantonik.fbp.FancyBlockParticles;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SplittableRandom;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConstants {
    public static final String MOD_VERSION = ModList.get().getModContainerById(FancyBlockParticles.MOD_ID).orElseThrow(IllegalStateException::new).getModInfo().getVersion().getQualifier();
    public static final Path CONFIG_PATH = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), FancyBlockParticles.MOD_ID);

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

    public static final Supplier<TextureAtlasSprite> FBP_PARTICLE_SPRITE = () -> Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WHITE_CONCRETE.defaultBlockState());

    public static final IParticleRenderType FBP_PARTICLE_RENDER = new IParticleRenderType() {
        @Override
        public void begin(BufferBuilder buffer, @Nonnull TextureManager manager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            manager.bind(PlayerContainer.BLOCK_ATLAS);

            RenderSystem.enableCull();

            buffer.begin(7, DefaultVertexFormats.PARTICLE);
        }

        @Override
        public void end(Tessellator tessellator) {
            tessellator.end();

            RenderSystem.disableBlend();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.disableCull();
        }
    };

    public static final IParticleRenderType FBP_TERRAIN_RENDER = new IParticleRenderType() {
        @Override
        public void begin(BufferBuilder buffer, TextureManager manager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            manager.bind(PlayerContainer.BLOCK_ATLAS);

            if (FancyBlockParticles.CONFIG.global.isCullParticles())
                RenderSystem.enableCull();
            else
                RenderSystem.disableCull();

            buffer.begin(7, DefaultVertexFormats.BLOCK);
        }

        @Override
        public void end(Tessellator tessellator) {
            tessellator.end();

            RenderSystem.disableBlend();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.disableCull();
        }
    };
}
