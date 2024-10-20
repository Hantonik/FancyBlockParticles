package hantonik.fbp.particle;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class FBPPlacingAnimationParticle extends Particle implements IKillableParticle {
    private final BlockState state;
    private final BlockPos pos;

    private final IBakedModel model;

    private final Vector3d rotation;
    private final Vector2f slide;

    private final float angleY;

    private boolean killToggle;

    public FBPPlacingAnimationParticle(ClientWorld level, BlockState state, BlockPos pos, LivingEntity placer, Hand hand) {
        super(level, pos.getX(), pos.getY(), pos.getZ());

        this.state = state;
        this.pos = pos;

        this.model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.animations.getMinLifetime(), FancyBlockParticles.CONFIG.animations.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.animations.getMinLifetime(), FancyBlockParticles.CONFIG.animations.getMaxLifetime()) + 0.5D);

        Vector3f horizontalLook = new Vector3f(placer.getLookAngle().multiply(-1.0F, 0.0F, -1.0F).normalize());
        float handMultiplier = placer.getMainArm() == HandSide.RIGHT ? 1.0F : -1.0F;

        Vector3f vec = new Vector3f(horizontalLook.z(), 0.0F, -horizontalLook.x());
        vec.mul(hand == Hand.MAIN_HAND ? handMultiplier : -handMultiplier);

        Matrix3f changeOfBasis = new Matrix3f();

        changeOfBasis.set(0, 0, vec.x());
        changeOfBasis.set(0, 1, vec.y());
        changeOfBasis.set(0, 2, vec.z());
        changeOfBasis.set(1, 0, 0.0F);
        changeOfBasis.set(1, 1, 1.0F);
        changeOfBasis.set(1, 2, 0.0F);
        changeOfBasis.set(2, 0, horizontalLook.x());
        changeOfBasis.set(2, 1, horizontalLook.y());
        changeOfBasis.set(2, 2, horizontalLook.z());

        this.rotation = FBPConstants.ANIMATION_ROTATION;

        Vector3f translation = new Vector3f(FBPConstants.ANIMATION_TRANSLATION);
        double slidePow = FBPConstants.ANIMATION_TRANSLATION.length();

        if (placer.getRotationVector().x <= 0.0F)
            translation.mul(1.0F, -1.0F, 1.0F);

        translation.transform(changeOfBasis);

        Vector3f slideDir = this.adjustDirection(level, placer, translation);
        slideDir.normalize();

        Vector3d animationDir = new Vector3d(slideDir);

        this.angleY = (float) Math.atan2(slideDir.x(), slideDir.z());
        Vector3d yRot = animationDir.yRot(-this.angleY);

        this.slide = new Vector2f((float) (yRot.z * slidePow), (float) (yRot.y * slidePow));

        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (!FancyBlockParticles.CONFIG.animations.isEnabled() || !FancyBlockParticles.CONFIG.isBlockAnimationsEnabled(this.state.getBlock()))
            this.remove();

        if (!Minecraft.getInstance().isPaused()) {
            if (this.killToggle)
                this.remove();

            this.age++;

            if (this.age == this.lifetime + 1)
                FBPPlacingAnimationManager.showBlock(this.pos, false);

            if (this.age >= this.lifetime + 2)
                this.remove();
        }

        if (this.level.getBlockState(this.pos) != this.state)
            this.remove();
    }

    @Override
    public void remove() {
        FBPPlacingAnimationManager.showBlock(this.pos, true);

        super.remove();
    }

    @Override
    public void killParticle() {
        this.killToggle = true;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.CUSTOM;
    }

    @Override
    public int getLightColor(float partialTick) {
        return this.level.hasChunkAt(this.pos) ? WorldRenderer.getLightColor(this.level, this.state, this.pos) : 0;
    }

    @Override
    public void render(IVertexBuilder builder, ActiveRenderInfo info, float partialTick) {
        MatrixStack stack = new MatrixStack();

        double posX = MathHelper.lerp(partialTick, this.xo, this.x) - info.getPosition().x + 0.5D;
        double posY = MathHelper.lerp(partialTick, this.yo, this.y) - info.getPosition().y + 0.5D;
        double posZ = MathHelper.lerp(partialTick, this.zo, this.z) - info.getPosition().z + 0.5D;

        stack.translate(posX, posY, posZ);

        float progress = Math.min(1.0F, (this.age + partialTick) / (this.lifetime + 1.0F));

        Vector3d offset = this.state.getOffset(this.level, this.pos);
        stack.translate(offset.x, offset.y, offset.z);

        stack.mulPose(Vector3f.YP.rotation(this.angleY));

        this.slideIn(stack, progress);
        this.rotate(stack, progress);
        this.scale(stack, progress);

        stack.mulPose(Vector3f.YP.rotation(-this.angleY));

        stack.translate(-offset.x, -offset.y, -offset.z);
        stack.translate(-0.5F, -0.5F, -0.5F);

        IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        this.renderBlock(this.level, this.model, this.state, this.pos, stack, bufferSource);
        bufferSource.endBatch();
    }

    private void slideIn(MatrixStack stack, float progress) {
        Vector2f translate = new Vector2f(this.slide.x * (1.0F - this.exponent(0.9F, progress)), this.slide.y * (1.0F - this.exponent(0.9F, progress)));

        stack.translate(0.0F, translate.y, translate.x);
    }

    private void rotate(MatrixStack stack, float progress) {
        Vector3f rotation = new Vector3f(this.rotation.scale(1.0F - this.exponent(-0.08F, progress)));
        Vector3f pivot = new Vector3f(FBPConstants.ANIMATION_PIVOT);

        if (this.slide.y < 0.0D) {
            pivot.mul(1.0F, -1.0F, 1.0F);
            rotation.mul(-1.0F, 1.0F, -1.0F);
        }

        stack.translate(pivot.x(), pivot.y(), pivot.z());

        stack.mulPose(Vector3f.XP.rotation(rotation.x()));
        stack.mulPose(Vector3f.YP.rotation(rotation.y()));
        stack.mulPose(Vector3f.ZP.rotation(rotation.z()));

        stack.translate(-pivot.x(), -pivot.y(), -pivot.z());
    }

    private void scale(MatrixStack stack, float progress) {
        float startScale = FancyBlockParticles.CONFIG.animations.getSizeMultiplier();
        float scale = startScale + (1.0F - startScale) * this.exponent(-0.7F, progress);

        stack.scale(scale, scale, scale);
    }

    private void renderBlock(ClientWorld level, IBakedModel model, BlockState state, BlockPos pos, MatrixStack stack, IRenderTypeBuffer.Impl bufferSource) {
        RenderType type = RenderTypeLookup.getMovingBlockRenderType(state);
        IVertexBuilder buffer = bufferSource.getBuffer(type);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(level, model, state, pos, stack, buffer, false, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
    }

    private Vector3f adjustDirection(ClientWorld level, LivingEntity placer, Vector3f slideDir) {
        List<Direction> emptyDirections = Lists.newArrayList();

        for (Direction side : Direction.values()) {
            BlockPos sidePos = this.pos.relative(side);
            VoxelShape sideCollision = level.getBlockState(sidePos).getCollisionShape(level, sidePos);

            if (sideCollision.isEmpty())
                emptyDirections.add(side);
            else {
                if (side.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    if (sideCollision.min(side.getAxis()) > 0.25F)
                        emptyDirections.add(side);
                } else {
                    if (sideCollision.min(side.getAxis()) < 0.75F)
                        emptyDirections.add(side);
                }
            }
        }

        List<Direction> affectedDirections = getAffectedDirections(slideDir.x(), slideDir.y(), slideDir.z());

        for (Direction side : affectedDirections) {
            if (!emptyDirections.contains(side)) {
                Vector3f step = side.step();

                step.mul(side.step().x(), side.step().y(), side.step().z());
                step.mul(slideDir.x(), slideDir.y(), slideDir.z());
                slideDir.sub(step);
            }
        }

        if (!emptyDirections.isEmpty() && new Vector3d(slideDir).length() == 0) {
            List<Direction> nearestDirections = Lists.newArrayList(Direction.orderedByNearest(placer));
            emptyDirections.sort(Comparator.comparingInt(nearestDirections::indexOf));

            slideDir = emptyDirections.get(0).step();
        }

        return slideDir;
    }

    private static List<Direction> getAffectedDirections(float x, float y, float z) {
        return Util.make(Lists.newArrayList(), list -> {
            if (x > 0)
                list.add(Direction.EAST);
            if (x < 0)
                list.add(Direction.WEST);
            if (y > 0)
                list.add(Direction.UP);
            if (y < 0)
                list.add(Direction.DOWN);
            if (z > 0)
                list.add(Direction.SOUTH);
            if (z < 0)
                list.add(Direction.NORTH);
        });
    }

    private float exponent(float curve, float time) {
        double base = curve > 0.0F ? -Math.log(curve) : Math.log(-curve) - 1.0D;

        return (float) (base * Math.pow(1.0D / base + 1.0D, time) - base);
    }
}
