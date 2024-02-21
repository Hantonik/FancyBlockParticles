package hantonik.fbp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import hantonik.fbp.FancyBlockParticles;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SplittableRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConstants {
    public static final Path CONFIG_PATH = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), FancyBlockParticles.MOD_ID);

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

            RenderSystem.enableCull();

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
