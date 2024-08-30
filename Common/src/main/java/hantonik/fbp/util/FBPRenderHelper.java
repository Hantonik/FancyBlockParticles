package hantonik.fbp.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPRenderHelper {
    public static void renderCubeShaded(VertexConsumer buffer, Vec2[] uv, double xPos, double yPos, double zPos, double scale, Vec3 rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        var radX = (float) Math.toRadians(rotation.x);
        var radY = (float) Math.toRadians(rotation.y);
        var radZ = (float) Math.toRadians(rotation.z);

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);
            var v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);
            var v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);
            var v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);

            var normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            var shade = (float) Math.min(normal.x * normal.x * 0.6F + normal.y * normal.y * (Minecraft.getInstance().level.effects().constantAmbientLight() ? 0.9F : (3.0F + normal.y) / 4.0F) + normal.z * normal.z * 0.8F, 1.0F);

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

    public static void renderCubeShaded(VertexConsumer buffer, Vec2[] uv, double xPos, double yPos, double zPos, double width, double height, Vec3 rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        var radX = (float) Math.toRadians(rotation.x);
        var radY = (float) Math.toRadians(rotation.y);
        var radZ = (float) Math.toRadians(rotation.z);

        for (var i = 0; i < FBPConstants.CUBE.length; i += 4) {
            var v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);
            var v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);
            var v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);
            var v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);

            var normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            var shade = (float) Math.min(normal.x * normal.x * 0.6F + normal.y * normal.y * (Minecraft.getInstance().level.effects().constantAmbientLight() ? 0.9F : (3.0F + normal.y) / 4.0F) + normal.z * normal.z * 0.8F, 1.0F);

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

    public static void addVertex(VertexConsumer buffer, Vec3 pos, float u, float v, int light, float red, float green, float blue, float alpha, Vec3 normal) {
        buffer.vertex(pos.x, pos.y, pos.z).color(red, green, blue, alpha).uv(u, v).uv2(light).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }

    public static Vec3 rotate(Vec3 vector, float angleX, float angleY, float angleZ) {
        var sin = new Vec3(Mth.sin(angleX), Mth.sin(angleY), Mth.sin(angleZ));
        var cos = new Vec3(Mth.cos(angleX), Mth.cos(angleY), Mth.cos(angleZ));

        vector = new Vec3(vector.x, vector.y * cos.x - vector.z * sin.x, vector.y * sin.x + vector.z * cos.x);
        vector = new Vec3(vector.x * cos.z - vector.y * sin.z, vector.x * sin.z + vector.y * cos.z, vector.z);
        vector = new Vec3(vector.x * cos.y + vector.z * sin.y, vector.y, vector.x * sin.y - vector.z * cos.y);

        return vector;
    }
}
