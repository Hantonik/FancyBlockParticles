package hantonik.fbp.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.renderer.state.FBPParticleRenderState;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPRenderHelper {
    public static void renderCubeShaded(FBPParticleRenderState renderState, SingleQuadParticle.Layer layer, float x, float y, float z, float width, float height, Vector3f rotationRad, float u0, float u1, float v0, float v1, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        var rotation = new Quaternionf().rotateXYZ(rotationRad.x, rotationRad.y, rotationRad.z);

        var rotationX = new Quaternionf().rotateX(rotationRad.x);
        var rotationY = new Quaternionf().rotateY(rotationRad.y);
        var rotationZ = new Quaternionf().rotateZ(rotationRad.z);

        for (var i = 0; i < FBPConstants.CUBE_NORMALS.length; i++) {
            var normal = FBPConstants.CUBE_NORMALS[i].rotate(rotation, new Vector3f());
            var face = new Vector3f(normal).mul(width, height, width).add(x, y, z);
            var faceRotation = new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, 1.0F), normal);

            if (i < 2)
                rotationY.mul(faceRotation, faceRotation);
            else if (i < 4)
                rotationZ.mul(faceRotation, faceRotation);
            else
                rotationX.mul(faceRotation, faceRotation);

            var shade = getShade(normal.x, normal.y, normal.z, true);

            if (cartoon)
                renderState.add(layer, face.x, face.y, face.z, faceRotation.x, faceRotation.y, faceRotation.z, faceRotation.w, width, i < 2 ? width : height, u1, u1, v1, v1, ARGB.colorFromFloat(alpha, red * shade, green * shade, blue * shade), light);
            else
                renderState.add(layer, face.x, face.y, face.z, faceRotation.x, faceRotation.y, faceRotation.z, faceRotation.w, width, i < 2 ? width : height, u0, u1, v0, v1, ARGB.colorFromFloat(alpha, red * shade, green * shade, blue * shade), light);
        }
    }

    public static void renderCubeShadedLegacy(VertexConsumer buffer, float x, float y, float z, float scale, Vector3f rotationRad, float u0, float u1, float v0, float v1, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var vec1 = rotate(FBPConstants.CUBE[i], rotationRad.x, rotationRad.y, rotationRad.z).mul(scale).add(x, y, z);
            var vec2 = rotate(FBPConstants.CUBE[i + 1], rotationRad.x, rotationRad.y, rotationRad.z).mul(scale).add(x, y, z);
            var vec3 = rotate(FBPConstants.CUBE[i + 2], rotationRad.x, rotationRad.y, rotationRad.z).mul(scale).add(x, y, z);
            var vec4 = rotate(FBPConstants.CUBE[i + 3], rotationRad.x, rotationRad.y, rotationRad.z).mul(scale).add(x, y, z);

            var normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], rotationRad.x, rotationRad.y, rotationRad.z);

            var shade = getShade(normal.x, normal.y, normal.z, true);

            if (cartoon) {
                addVertex(buffer, vec1, u1, v1, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, vec2, u1, v1, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, vec3, u1, v1, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, vec4, u1, v1, light, red * shade, green * shade, blue * shade, alpha, normal);
            } else {
                addVertex(buffer, vec1, u1, v1, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, vec2, u1, v0, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, vec3, u0, v0, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, vec4, u0, v1, light, red * shade, green * shade, blue * shade, alpha, normal);
            }
        }
    }

    public static Vector3f rotate(Vector3f vector, float angleX, float angleY, float angleZ) {
        var sin = new Vector3f(Mth.sin(angleX), Mth.sin(angleY), Mth.sin(angleZ));
        var cos = new Vector3f(Mth.cos(angleX), Mth.cos(angleY), Mth.cos(angleZ));

        vector = new Vector3f(vector.x, vector.y * cos.x - vector.z * sin.x, vector.y * sin.x + vector.z * cos.x);
        vector = new Vector3f(vector.x * cos.z - vector.y * sin.z, vector.x * sin.z + vector.y * cos.z, vector.z);
        vector = new Vector3f(vector.x * cos.y + vector.z * sin.y, vector.y, vector.x * sin.y - vector.z * cos.y);

        return vector;
    }

    public static float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        var constantAmbientLight = Minecraft.getInstance().level.effects().constantAmbientLight();

        if (shade)
            return Math.min(normalX * normalX * 0.6F + normalY * normalY * (constantAmbientLight ? 0.9F : (3.0F + normalY) / 4.0F) + normalZ * normalZ * 0.8F, 1.0F);
        else
            return constantAmbientLight ? 0.9F : 1.0F;
    }

    public static void addVertex(VertexConsumer buffer, Vector3f pos, float u, float v, int light, float red, float green, float blue, float alpha, Vector3f normal) {
        buffer.addVertex(pos.x, pos.y, pos.z).setColor(red, green, blue, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x, normal.y, normal.z);
    }
}
