package hantonik.fbp.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.util.FBPConstants;
import hantonik.fbp.util.FBPRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.LiquidBlock;
import org.joml.Vector2f;
import org.joml.Vector3d;

@Environment(EnvType.CLIENT)
public class FBPRainParticle extends WaterDropParticle {
    private final double angleY;

    private final float uo;
    private final float vo;

    private double height;

    private double lastAlpha;
    private double lastScale;
    private double lastHeight;

    private float multiplier;

    public FBPRainParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z);

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.sprite = sprite;

        this.angleY = FBPConstants.RANDOM.nextDouble() * 45.0D;

        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;

        this.gravity = 0.025F;
        this.lifetime = FBPConstants.RANDOM.nextInt(50, 70);

        this.alpha = 0.0F;
        this.quadSize = 0.0F;

        this.hasPhysics = true;

        this.multiplier = 1.0F;

        if (FancyBlockParticles.CONFIG.isRandomFadingSpeed())
            this.multiplier *= FBPConstants.RANDOM.nextFloat(0.85F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lastAlpha = this.alpha;
        this.lastScale = this.quadSize;
        this.lastHeight = this.height;

        if (!Minecraft.getInstance().isPaused()) {
            this.age++;

            if (this.y < Minecraft.getInstance().player.getY() - (Minecraft.getInstance().options.renderDistance().get() * 9.0D))
                this.remove();

            if (!this.onGround) {
                if (this.age < this.lifetime) {
                    var max = FancyBlockParticles.CONFIG.getScaleMultiplier() * 0.5D;

                    if (this.quadSize < max) {
                        this.quadSize += 0.05F * this.multiplier;

                        if (this.quadSize > max)
                            this.quadSize = (float) max;

                        this.height = this.quadSize;
                    }

                    if (this.alpha < 0.6F) {
                        this.alpha += 0.085F * this.multiplier;

                        if (this.alpha > 0.6F)
                            this.alpha = 0.6F;
                    }
                } else
                    this.remove();
            }

            if (this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).getBlock() instanceof LiquidBlock)
                this.remove();

            this.yd -= 0.04D * this.gravity;

            this.move(this.xd, this.yd, this.zd);

            this.yd *= 1.00025D;

            if (this.onGround) {
                this.xd = 0.0D;
                this.yd = -0.25D;
                this.zd = 0.0D;

                if (this.height > 0.075F)
                    this.height *= 0.725F;

                var max = (float) FancyBlockParticles.CONFIG.getScaleMultiplier() * 4.25F;

                if (this.quadSize < max) {
                    this.quadSize += max / 10.0F;

                    if (this.quadSize > max)
                        this.quadSize = max;
                }

                if (this.quadSize >= max / 2.0F) {
                    this.alpha *= 0.75F * this.multiplier;

                    if (this.alpha <= 0.001F)
                        this.remove();
                }
            }
        }

        var rgb = this.level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), 0.0F);

        this.rCol = (float) rgb.x;
        this.gCol = (float) Mth.clamp(rgb.y + 0.1D, 0.1D, 1.0D);
        this.bCol = (float) Mth.clamp(rgb.z + 0.5D, 0.5D, 1.0D);
    }

    @Override
    public void move(double x, double y, double z) {
        var xo = x;
        var yo = y;
        var zo = z;

        var collisions = this.level.getCollisions(null, this.getBoundingBox().expandTowards(x, y, z));

        for (var shape : collisions)
            x = shape.collide(Direction.Axis.X, this.getBoundingBox(), x);

        this.setBoundingBox(this.getBoundingBox().move(x, 0.0D, 0.0D));

        for (var shape : collisions)
            y = shape.collide(Direction.Axis.Y, this.getBoundingBox(), y);

        this.setBoundingBox(this.getBoundingBox().move(0.0D, y, 0.0D));

        for (var shape : collisions)
            z = shape.collide(Direction.Axis.Z, this.getBoundingBox(), z);

        this.setBoundingBox(this.getBoundingBox().move(0.0D, 0.0D, z));

        this.setLocationFromBoundingbox();

        this.onGround = y != yo && yo < 0.0D;

        if (x != xo)
            this.xd *= 0.699D;
        if (z != zo)
            this.zd *= 0.699D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FBPConstants.FBP_TERRAIN_RENDER;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTicks) {
        if (!FancyBlockParticles.CONFIG.isEnabled())
            this.lifetime = 0;

        var u0 = 0.0F;
        var v0 = 0.0F;

        if (!FancyBlockParticles.CONFIG.isCartoonMode()) {
            u0 = this.sprite.getU(this.uo / 4.0F);
            v0 = this.sprite.getV(this.vo / 4.0F);
        }

        var u1 = this.sprite.getU((this.uo + 1.0F) / 4.0F);
        var v1 = this.sprite.getV((this.vo + 1.0F) / 4.0F);

        var posX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - info.getPosition().x);
        var posY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - info.getPosition().y);
        var posZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - info.getPosition().z);

        var light = this.getLightColor(partialTicks);

        var alpha = (float) Mth.lerp(partialTicks, this.lastAlpha, this.alpha);
        var width = (float) Mth.lerp(partialTicks, this.lastScale, this.quadSize) / 10.0F;
        var height = (float) Mth.lerp(partialTicks, this.lastHeight, this.height) / 10.0F;

        var smoothRotation = new Vector3d(0.0D, this.angleY, 0.0D);

        FBPRenderHelper.renderCubeShaded(buffer, new Vector2f[]{ new Vector2f(u1, v1), new Vector2f(u1, v0), new Vector2f(u0, v0), new Vector2f(u0, v1) }, posX, posY + height, posZ, width, height, smoothRotation, light, this.rCol, this.gCol, this.bCol, alpha, FancyBlockParticles.CONFIG.isCartoonMode());
    }

    @Override
    public int getLightColor(float partialTick) {
        var i = super.getLightColor(partialTick);

        if (!FancyBlockParticles.CONFIG.isFancySmoke())
            return i;

        var j = 0;

        var pos = BlockPos.containing(this.x, this.y, this.z);

        if (this.level.isLoaded(pos))
            j = this.level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(pos);

        return i == 0 ? j : i;
    }
}
