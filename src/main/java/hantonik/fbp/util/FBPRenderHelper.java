package hantonik.fbp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FBPRenderHelper {
    public static void renderCubeShaded(IVertexBuilder builder, Vector2f[] uv, double xPos, double yPos, double zPos, double scale, Vector3d rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        float radX = (float) Math.toRadians(rotation.x);
        float radY = (float) Math.toRadians(rotation.y);
        float radZ = (float) Math.toRadians(rotation.z);

        for (int i = 0; i < FBPConstants.CUBE.length; i += 4) {
            Vector3d v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);
            Vector3d v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);
            Vector3d v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);
            Vector3d v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ).scale(scale).add(xPos, yPos, zPos);

            Vector3d normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            float shade = (float) Math.min(normal.x * normal.x * 0.6F + normal.y * normal.y * (Minecraft.getInstance().level.effects().constantAmbientLight() ? 0.9F : (3.0F + normal.y) / 4.0F) + normal.z * normal.z * 0.8F, 1.0F);

            if (cartoon) {
                addVertex(builder, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v2, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v3, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v4, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            } else {
                addVertex(builder, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v2, uv[1].x, uv[1].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v3, uv[2].x, uv[2].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v4, uv[3].x, uv[3].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            }
        }
    }

    public static void renderCubeShaded(IVertexBuilder builder, Vector2f[] uv, double xPos, double yPos, double zPos, double width, double height, Vector3d rotation, int light, float red, float green, float blue, float alpha, boolean cartoon) {
        float radX = (float) Math.toRadians(rotation.x);
        float radY = (float) Math.toRadians(rotation.y);
        float radZ = (float) Math.toRadians(rotation.z);

        for (int i = 0; i < FBPConstants.CUBE.length; i += 4) {
            Vector3d v1 = rotate(FBPConstants.CUBE[i], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);
            Vector3d v2 = rotate(FBPConstants.CUBE[i + 1], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);
            Vector3d v3 = rotate(FBPConstants.CUBE[i + 2], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);
            Vector3d v4 = rotate(FBPConstants.CUBE[i + 3], radX, radY, radZ).multiply(width, height, width).add(xPos, yPos, zPos);

            Vector3d normal = rotate(FBPConstants.CUBE_NORMALS[i / 4], radX, radY, radZ);

            float shade = (float) Math.min(normal.x * normal.x * 0.6F + normal.y * normal.y * (Minecraft.getInstance().level.effects().constantAmbientLight() ? 0.9F : (3.0F + normal.y) / 4.0F) + normal.z * normal.z * 0.8F, 1.0F);

            if (cartoon) {
                addVertex(builder, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v2, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v3, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v4, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            } else {
                addVertex(builder, v1, uv[0].x, uv[0].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v2, uv[1].x, uv[1].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v3, uv[2].x, uv[2].y, light, red * shade, green * shade, blue * shade, alpha, normal);
                addVertex(builder, v4, uv[3].x, uv[3].y, light, red * shade, green * shade, blue * shade, alpha, normal);
            }
        }
    }

    public static void addVertex(IVertexBuilder builder, Vector3d pos, float u, float v, int light, float red, float green, float blue, float alpha, Vector3d normal) {
        builder.vertex(pos.x, pos.y, pos.z).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }

    public static Vector3d rotate(Vector3d vector, float angleX, float angleY, float angleZ) {
        Vector3d sin = new Vector3d(MathHelper.sin(angleX), MathHelper.sin(angleY), MathHelper.sin(angleZ));
        Vector3d cos = new Vector3d(MathHelper.cos(angleX), MathHelper.cos(angleY), MathHelper.cos(angleZ));

        vector = new Vector3d(vector.x, vector.y * cos.x - vector.z * sin.x, vector.y * sin.x + vector.z * cos.x);
        vector = new Vector3d(vector.x * cos.z - vector.y * sin.z, vector.x * sin.z + vector.y * cos.z, vector.z);
        vector = new Vector3d(vector.x * cos.y + vector.z * sin.y, vector.y, vector.x * sin.y - vector.z * cos.y);

        return vector;
    }

    public static void enableScissor(int minX, int minY, int maxX, int maxY) {
        MainWindow window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();

        RenderSystem.enableScissor((int) ((double) minX * scale), (int) ((double) window.getHeight() - (double) maxY * scale), Math.max(0, (int) ((double) (maxX - minX) * scale)), Math.max(0, (int) ((double) (maxY - minY) * scale)));
    }

    public static void disableScissor() {
        RenderSystem.disableScissor();
    }
}
