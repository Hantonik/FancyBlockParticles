package hantonik.fbp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.platform.Services;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SplittableRandom;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPConstants {
    public static final Path CONFIG_PATH = Paths.get(Services.PLATFORM.getConfigDir().toAbsolutePath().toString(), FancyBlockParticles.MOD_ID);

    public static final SplittableRandom RANDOM = new SplittableRandom();

    public static final Vec3[] CUBE = new Vec3[] {
            new Vec3(1.0D, 1.0D, -1.0D),   new Vec3(1.0D, 1.0D, 1.0D),   new Vec3(-1.0D, 1.0D, 1.0D),  new Vec3(-1.0D, 1.0D, -1.0D),
            new Vec3(-1.0D, -1.0D, -1.0D), new Vec3(-1.0D, -1.0D, 1.0D), new Vec3(1.0D, -1.0D, 1.0D),  new Vec3(1.0D, -1.0D, -1.0D),
            new Vec3(-1.0D, -1.0D, 1.0D),  new Vec3(-1.0D, 1.0D, 1.0D),  new Vec3(1.0D, 1.0D, 1.0D),   new Vec3(1.0D, -1.0D, 1.0D),
            new Vec3(1.0D, -1.0D, -1.0D),  new Vec3(1.0D, 1.0D, -1.0D),  new Vec3(-1.0D, 1.0D, -1.0D), new Vec3(-1.0D, -1.0D, -1.0D),
            new Vec3(-1.0D, -1.0D, -1.0D), new Vec3(-1.0D, 1.0D, -1.0D), new Vec3(-1.0D, 1.0D, 1.0D),  new Vec3(-1.0D, -1.0D, 1.0D),
            new Vec3(1.0D, -1.0D, 1.0D),   new Vec3(1.0D, 1.0D, 1.0D),   new Vec3(1.0D, 1.0D, -1.0D),  new Vec3(1.0D, -1.0D, -1.0D)
    };

    public static final Vec3[] CUBE_NORMALS = new Vec3[] {
            new Vec3(0.0D, 1.0D, 0.0D), new Vec3(0.0D, -1.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 0.0D, -1.0D),
            new Vec3(-1.0D, 0.0D, 0.0D), new Vec3(1.0D, 0.0D, 0.0D)
    };

    public static final Vec3 ANIMATION_TRANSLATION = new Vec3(0.1F, 0.1F, 0.1F);
    public static final Vec3 ANIMATION_ROTATION = new Vec3(0.0F, -0.07F, 0.07F);
    public static final Vec3 ANIMATION_PIVOT = new Vec3(0.25F, -0.25F, 0.25F);

    public static final Supplier<TextureAtlasSprite> FBP_PARTICLE_SPRITE = () -> Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WHITE_CONCRETE.defaultBlockState());

    public static final ParticleRenderType FBP_PARTICLE_RENDER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder buffer, @NotNull TextureManager manager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);

            if (FancyBlockParticles.CONFIG.global.isCullParticles())
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
