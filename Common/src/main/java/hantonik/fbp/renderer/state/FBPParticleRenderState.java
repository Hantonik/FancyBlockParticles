package hantonik.fbp.renderer.state;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FBPParticleRenderState implements SubmitNodeCollector.ParticleGroupRenderer, ParticleGroupRenderState {
    private final Map<SingleQuadParticle.Layer, Storage> particles = Maps.newHashMap();

    private int particleCount;

    public void add(SingleQuadParticle.Layer layer, float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light) {
        this.particles.computeIfAbsent(layer, l -> new Storage()).add(posX, posY, posZ, rotX, rotY, rotZ, rotW, widthScale, heightScale, u0, u1, v0, v1, color, light);

        this.particleCount++;
    }

    @Override
    public void clear() {
        this.particles.values().forEach(Storage::clear);

        this.particleCount = 0;
    }

    @Nullable
    @Override
    public QuadParticleRenderState.PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache cache) {
        var verticesCount = this.particleCount * 4;

        try (ByteBufferBuilder builder = ByteBufferBuilder.exactlySized(verticesCount * DefaultVertexFormat.PARTICLE.getVertexSize())) {
            var buffer = new BufferBuilder(builder, VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            var prepared = new HashMap<SingleQuadParticle.Layer, QuadParticleRenderState.PreparedLayer>();

            var vertexOffset = 0;

            for (var entry : this.particles.entrySet()) {
                entry.getValue().forEachParticle((posX, posY, posZ, rotX, rotY, rotZ, rotW, widthScale, heightScale, u0, u1, v0, v1, color, light) -> this.renderRotatedQuad(buffer, posX, posY, posZ, rotX, rotY, rotZ, rotW, widthScale, heightScale, u0, u1, v0, v1, color, light));

                if (entry.getValue().count() > 0)
                    prepared.put(entry.getKey(), new QuadParticleRenderState.PreparedLayer(vertexOffset, entry.getValue().count() * 6));

                vertexOffset += entry.getValue().count() * 4;
            }

            var data = buffer.build();

            if (data != null) {
                cache.write(data.vertexBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(data.drawState().indexCount());

                return new QuadParticleRenderState.PreparedBuffers(data.drawState().indexCount(), RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f()), prepared);
            }
        }

        return null;
    }

    @Override
    public void render(QuadParticleRenderState.PreparedBuffers buffers, ParticleFeatureRenderer.ParticleBufferCache cache, RenderPass pass, TextureManager manager, boolean translucent) {
        var sequentialBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

        pass.setVertexBuffer(0, cache.get());
        pass.setIndexBuffer(sequentialBuffer.getBuffer(buffers.indexCount()), sequentialBuffer.type());
        pass.setUniform("DynamicTransforms", buffers.dynamicTransforms());

        for (var entry : buffers.layers().entrySet()) {
            if (translucent == entry.getKey().translucent()) {
                var texture = manager.getTexture(entry.getKey().textureAtlasLocation());

                pass.setPipeline(entry.getKey().pipeline());
                pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());

                pass.drawIndexed(entry.getValue().vertexOffset(), 0, entry.getValue().indexCount(), 1);
            }
        }
    }

    private void renderRotatedQuad(VertexConsumer consumer, float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light) {
        var rotation = new Quaternionf(rotX, rotY, rotZ, rotW);

        this.renderVertex(consumer, rotation, posX, posY, posZ, 1.0F, -1.0F, widthScale, heightScale, u1, v1, color, light);
        this.renderVertex(consumer, rotation, posX, posY, posZ, 1.0F, 1.0F, widthScale, heightScale, u1, v0, color, light);
        this.renderVertex(consumer, rotation, posX, posY, posZ, -1.0F, 1.0F, widthScale, heightScale, u0, v0, color, light);
        this.renderVertex(consumer, rotation, posX, posY, posZ, -1.0F, -1.0F, widthScale, heightScale, u0, v1, color, light);
    }

    private void renderVertex(VertexConsumer consumer, Quaternionf rotation, float posX, float posY, float posZ, float x, float y, float widthScale, float heightScale, float u, float v, int color, int light) {
        var vertexPos = new Vector3f(x, y, 0.0F).rotate(rotation).mul(widthScale, heightScale, widthScale).add(posX, posY, posZ);

        consumer.addVertex(vertexPos.x(), vertexPos.y(), vertexPos.z()).setUv(u, v).setColor(color).setLight(light);
    }

    @Override
    public void submit(SubmitNodeCollector nodeCollector, CameraRenderState state) {
        if (this.particleCount > 0)
            nodeCollector.submitParticleGroup(this);
    }

    @FunctionalInterface
    public interface ParticleConsumer {
        void consume(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light);
    }

    static class Storage {
        private int capacity = 1024;
        private int currentParticleIndex;

        private float[] floatValues = new float[this.capacity * 13];
        private int[] intValues = new int[this.capacity * 2];

        public void add(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light) {
            if (this.currentParticleIndex >= this.capacity)
                this.grow();

            var index = this.currentParticleIndex * 13;

            this.floatValues[index++] = posX;
            this.floatValues[index++] = posY;
            this.floatValues[index++] = posZ;
            this.floatValues[index++] = rotX;
            this.floatValues[index++] = rotY;
            this.floatValues[index++] = rotZ;
            this.floatValues[index++] = rotW;
            this.floatValues[index++] = widthScale;
            this.floatValues[index++] = heightScale;
            this.floatValues[index++] = u0;
            this.floatValues[index++] = u1;
            this.floatValues[index++] = v0;
            this.floatValues[index] = v1;

            index = this.currentParticleIndex * 2;

            this.intValues[index++] = color;
            this.intValues[index] = light;

            this.currentParticleIndex++;
        }

        public void forEachParticle(ParticleConsumer consumer) {
            for (var i = 0; i < this.currentParticleIndex; i++) {
                var floatIndex = i * 13;
                var intIndex = i * 2;

                consumer.consume(
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex],
                        this.intValues[intIndex++],
                        this.intValues[intIndex]
                );
            }
        }

        public void clear() {
            this.currentParticleIndex = 0;
        }

        private void grow() {
            this.capacity *= 2;

            this.floatValues = Arrays.copyOf(this.floatValues, this.capacity * 13);
            this.intValues = Arrays.copyOf(this.intValues, this.capacity * 2);
        }

        public int count() {
            return this.currentParticleIndex;
        }
    }
}
