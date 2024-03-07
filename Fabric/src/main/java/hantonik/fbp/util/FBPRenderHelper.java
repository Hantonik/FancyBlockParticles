package hantonik.fbp.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPRenderHelper {
    public static void renderCubeShaded(VertexConsumer buffer, Vec2[] uv, float xPos, float yPos, float zPos, float scale, Vector3f rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        var radX = (float) Math.toRadians(rotation.x());
        var radY = (float) Math.toRadians(rotation.y());
        var radZ = (float) Math.toRadians(rotation.z());

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ);
            v1.mul(scale);
            v1.add(xPos, yPos, zPos);

            var v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ);
            v2.mul(scale);
            v2.add(xPos, yPos, zPos);

            var v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ);
            v3.mul(scale);
            v3.add(xPos, yPos, zPos);

            var v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ);
            v4.mul(scale);
            v4.add(xPos, yPos, zPos);

            var normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            var shade = Math.min(normal.x() * normal.x() * 0.6F + normal.y() * normal.y() * (Minecraft.getInstance().level.effects().constantAmbientLight() ? 0.9F : (3.0F + normal.y()) / 4.0F) + normal.z() * normal.z() * 0.8F, 1.0F);

            if (cartoon) {
                addVertex(buffer, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v2, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v3, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v4, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            } else {
                addVertex(buffer, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v2, uv[1].x, uv[1].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v3, uv[2].x, uv[2].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v4, uv[3].x, uv[3].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            }
        }
    }

    public static void renderCubeShaded(VertexConsumer buffer, Vec2[] uv, float xPos, float yPos, float zPos, float width, float height, Vector3f rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        var radX = (float) Math.toRadians(rotation.x());
        var radY = (float) Math.toRadians(rotation.y());
        var radZ = (float) Math.toRadians(rotation.z());

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ);
            v1.mul(width, height, width);
            v1.add(xPos, yPos, zPos);

            var v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ);
            v2.mul(width, height, width);
            v2.add(xPos, yPos, zPos);

            var v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ);
            v3.mul(width, height, width);
            v3.add(xPos, yPos, zPos);

            var v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ);
            v4.mul(width, height, width);
            v4.add(xPos, yPos, zPos);

            var normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            var shade = Math.min(normal.x() * normal.x() * 0.6F + normal.y() * normal.y() * (Minecraft.getInstance().level.effects().constantAmbientLight() ? 0.9F : (3.0F + normal.y()) / 4.0F) + normal.z() * normal.z() * 0.8F, 1.0F);

            if (cartoon) {
                addVertex(buffer, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v2, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v3, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v4, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            } else {
                addVertex(buffer, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v2, uv[1].x, uv[1].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v3, uv[2].x, uv[2].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(buffer, v4, uv[3].x, uv[3].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            }
        }
    }

    public static void addVertex(VertexConsumer buffer, Vector3f pos, float u, float v, int light, float red, float green, float blue, float alpha, Vector3f normal) {
        buffer.vertex(pos.x(), pos.y(), pos.z()).color(red, green, blue, alpha).uv(u, v).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
    }

    public static Vector3f rotate(Vector3f vector, float angleX, float angleY, float angleZ) {
        var sin = new Vector3f(Mth.sin(angleX), Mth.sin(angleY), Mth.sin(angleZ));
        var cos = new Vector3f(Mth.cos(angleX), Mth.cos(angleY), Mth.cos(angleZ));

        vector = new Vector3f(vector.x(), vector.y() * cos.x() - vector.z() * sin.x(), vector.y() * sin.x() + vector.z() * cos.x());
        vector = new Vector3f(vector.x() * cos.z() - vector.y() * sin.z(), vector.x() * sin.z() + vector.y() * cos.z(), vector.z());
        vector = new Vector3f(vector.x() * cos.y() + vector.z() * sin.y(), vector.y(), vector.x() * sin.y() - vector.z() * cos.y());

        return vector;
    }
}
