package hantonik.fbp.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.platform.Services;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.joml.Vector3d;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPRenderHelper {
    public static void renderCubeShaded(VertexConsumer buffer, Vector2f[] uv, double xPos, double yPos, double zPos, double scale, Vector3d rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        var radX = (float) Math.toRadians(rotation.x);
        var radY = (float) Math.toRadians(rotation.y);
        var radZ = (float) Math.toRadians(rotation.z);

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);
            var v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);
            var v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);
            var v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ).mul(scale).add(xPos, yPos, zPos);

            var normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            var shade = Services.CLIENT.getShade((float) normal.x, (float) normal.y, (float) normal.z, true);

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

    public static void renderCubeShaded(VertexConsumer buffer, Vector2f[] uv, double xPos, double yPos, double zPos, double width, double height, Vector3d rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        var radX = (float) Math.toRadians(rotation.x);
        var radY = (float) Math.toRadians(rotation.y);
        var radZ = (float) Math.toRadians(rotation.z);

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ).mul(width, height, width).add(xPos, yPos, zPos);
            var v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ).mul(width, height, width).add(xPos, yPos, zPos);
            var v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ).mul(width, height, width).add(xPos, yPos, zPos);
            var v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ).mul(width, height, width).add(xPos, yPos, zPos);

            var normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            var shade = Services.CLIENT.getShade((float) normal.x, (float) normal.y, (float) normal.z, true);

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

    public static void addVertex(VertexConsumer buffer, Vector3d pos, float u, float v, int light, float red, float green, float blue, float alpha, Vector3d normal) {
        buffer.addVertex((float) pos.x, (float) pos.y, (float) pos.z).setColor(red, green, blue, alpha).setUv(u, v).setLight(light).setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }

    public static Vector3d rotate(Vector3d vector, float angleX, float angleY, float angleZ) {
        var sin = new Vector3d(Mth.sin(angleX), Mth.sin(angleY), Mth.sin(angleZ));
        var cos = new Vector3d(Mth.cos(angleX), Mth.cos(angleY), Mth.cos(angleZ));

        vector = new Vector3d(vector.x, vector.y * cos.x - vector.z * sin.x, vector.y * sin.x + vector.z * cos.x);
        vector = new Vector3d(vector.x * cos.z - vector.y * sin.z, vector.x * sin.z + vector.y * cos.z, vector.z);
        vector = new Vector3d(vector.x * cos.y + vector.z * sin.y, vector.y, vector.x * sin.y - vector.z * cos.y);

        return vector;
    }
}
