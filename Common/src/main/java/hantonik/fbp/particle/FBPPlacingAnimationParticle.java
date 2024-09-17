package hantonik.fbp.particle;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Vector3f;
import hantonik.fbp.FancyBlockParticles;
import hantonik.fbp.animation.FBPPlacingAnimationManager;
import hantonik.fbp.platform.Services;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class FBPPlacingAnimationParticle extends Particle implements IKillableParticle {
    private final BlockState state;
    private final BlockPos pos;

    private final BakedModel model;

    private final Vec3 rotation;
    private final Vec2 slide;

    private final float angleY;

    private boolean killToggle;

    public FBPPlacingAnimationParticle(ClientLevel level, BlockState state, BlockPos pos, LivingEntity placer, InteractionHand hand) {
        super(level, pos.getX(), pos.getY(), pos.getZ());

        this.state = state;
        this.pos = pos;

        this.model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        this.lifetime = (int) FBPConstants.RANDOM.nextDouble(Math.min(FancyBlockParticles.CONFIG.animations.getMinLifetime(), FancyBlockParticles.CONFIG.animations.getMaxLifetime()), Math.max(FancyBlockParticles.CONFIG.animations.getMinLifetime(), FancyBlockParticles.CONFIG.animations.getMaxLifetime()) + 0.5D);

        var horizontalLook = new Vector3f(placer.getLookAngle().multiply(-1.0F, 0.0F, -1.0F).normalize());
        var handMultiplier = placer.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;

        var vec = new Vector3f(horizontalLook.z(), 0.0F, -horizontalLook.x());
        vec.mul(hand == InteractionHand.MAIN_HAND ? handMultiplier : -handMultiplier);

        var changeOfBasis = new Matrix3f();

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

        var translation = new Vector3f(FBPConstants.ANIMATION_TRANSLATION);
        var slidePow = FBPConstants.ANIMATION_TRANSLATION.length();

        if (placer.getXRot() <= 0.0F)
            translation.mul(1.0F, -1.0F, 1.0F);

        translation.transform(changeOfBasis);

        var slideDir = this.adjustDirection(level, placer, translation);
        slideDir.normalize();

        var animationDir = new Vec3(slideDir);

        this.angleY = (float) Math.atan2(slideDir.x(), slideDir.z());
        var yRot = animationDir.yRot(-this.angleY);

        this.slide = new Vec2((float) yRot.z, (float) yRot.y).scale((float) slidePow);

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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public int getLightColor(float partialTick) {
        return this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.state, this.pos) : 0;
    }

    @Override
    public void render(VertexConsumer buffer, Camera info, float partialTick) {
        var stack = new PoseStack();

        var posX = Mth.lerp(partialTick, this.xo, this.x) - info.getPosition().x + 0.5D;
        var posY = Mth.lerp(partialTick, this.yo, this.y) - info.getPosition().y + 0.5D;
        var posZ = Mth.lerp(partialTick, this.zo, this.z) - info.getPosition().z + 0.5D;

        stack.translate(posX, posY, posZ);

        var progress = Math.min(1.0F, (this.age + partialTick) / (this.lifetime + 1.0F));

        var offset = this.state.getOffset(this.level, this.pos);
        stack.translate(offset.x, offset.y, offset.z);

        stack.mulPose(Vector3f.YP.rotation(this.angleY));

        this.slideIn(stack, progress);
        this.rotate(stack, progress);
        this.scale(stack, progress);

        stack.mulPose(Vector3f.YP.rotation(-this.angleY));

        stack.translate(-offset.x, -offset.y, -offset.z);
        stack.translate(-0.5F, -0.5F, -0.5F);

        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Services.CLIENT.renderBlock(this.level, this.model, this.state, this.pos, stack, bufferSource);
        bufferSource.endBatch();
    }

    private void slideIn(PoseStack stack, float progress) {
        var translate = this.slide.scale(1.0F - this.exponent(0.9F, progress));

        stack.translate(0.0F, translate.y, translate.x);
    }

    private void rotate(PoseStack stack, float progress) {
        var rotation = new Vector3f(this.rotation.scale(1.0F - this.exponent(-0.08F, progress)));
        var pivot = new Vector3f(FBPConstants.ANIMATION_PIVOT);

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

    private void scale(PoseStack stack, float progress) {
        var startScale = FancyBlockParticles.CONFIG.animations.getSizeMultiplier();
        var scale = startScale + (1.0F - startScale) * this.exponent(-0.7F, progress);

        stack.scale(scale, scale, scale);
    }

    private Vector3f adjustDirection(ClientLevel level, LivingEntity placer, Vector3f slideDir) {
        List<Direction> emptyDirections = Lists.newArrayList();

        for (var side : Direction.values()) {
            var sidePos = this.pos.relative(side);
            var sideCollision = level.getBlockState(sidePos).getCollisionShape(level, sidePos);

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

        var affectedDirections = getAffectedDirections(slideDir.x(), slideDir.y(), slideDir.z());

        for (var side : affectedDirections) {
            if (!emptyDirections.contains(side)) {
                var step = side.step();

                step.mul(side.step().x(), side.step().y(), side.step().z());
                step.mul(slideDir.x(), slideDir.y(), slideDir.z());
                slideDir.sub(step);
            }
        }

        if (!emptyDirections.isEmpty() && new Vec3(slideDir).length() == 0) {
            var nearestDirections = List.of(Direction.orderedByNearest(placer));
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
        var base = curve > 0.0F ? -Math.log(curve) : Math.log(-curve) - 1.0D;

        return (float) (base * Math.pow(1.0D / base + 1.0D, time) - base);
    }
}
