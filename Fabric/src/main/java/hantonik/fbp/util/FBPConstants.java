package hantonik.fbp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import hantonik.fbp.FancyBlockParticles;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SplittableRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConstants {
    public static final Path CONFIG_PATH = Paths.get(FabricLoader.getInstance().getConfigDir().toAbsolutePath().toString(), FancyBlockParticles.MOD_ID);

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

    public static final ParticleRenderType FBP_PARTICLE_RENDER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder buffer, @NotNull TextureManager manager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getParticleShader);

            RenderSystem.enableCull();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();

            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getParticleShader);

            RenderSystem.disableCull();
        }
    };

    public static final ParticleRenderType FBP_TERRAIN_RENDER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder buffer, TextureManager manager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);

            if (FancyBlockParticles.CONFIG.isCullParticles())
                RenderSystem.enableCull();
            else
                RenderSystem.disableCull();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();

            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getParticleShader);

            RenderSystem.disableCull();
        }
    };
}
