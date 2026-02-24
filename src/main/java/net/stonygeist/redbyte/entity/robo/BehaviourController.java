package net.stonygeist.redbyte.entity.robo;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.manager.PseudoRobo;

import java.lang.ref.WeakReference;

public class BehaviourController {
    public enum State {
        Idle,
        Walk,
        WalkTo,
        Jump
    }

    public State state = State.Idle;
    public float[] args;
    private WeakReference<RoboEntity> roboRef;
    private final ServerLevel serverLevel;
    private int count;
    private Vec3 startPos;

    public BehaviourController(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public void tick(PseudoRobo robo) {
        switch (state) {
            case Idle:
                break;
            case Walk:
                handleWalk(robo);
                break;
            case WalkTo:
                handleWalkTo(robo);
                break;
            case Jump:
                handleJump();
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void handleWalk(PseudoRobo robo) {
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity != null) {
            float speed = robo.getSpeed() / 20f;
            Vec3 flatDir = Vec3.directionFromRotation(0f, roboEntity.getYRot()).normalize();
            Vec3 targetPos = roboEntity.position().add(flatDir);

            Vec3 probe = flatDir.scale(.55f);
            AABB movedBox = roboEntity.getBoundingBox().move(probe);
            boolean collisionDetected = !serverLevel.noCollision(roboEntity, movedBox);
            if (collisionDetected) {
                AABB steppedBox = movedBox.move(0, 1, 0);
                if (serverLevel.noCollision(roboEntity, steppedBox)) {
                    targetPos = targetPos.add(0, 1, 0);
                    speed *= .55f;
                } else {
                    robo.setTargetVelocity(Vec3.ZERO);
                    done();
                    return;
                }
            }

            if (count >= args[0]) {
                robo.setTargetVelocity(Vec3.ZERO);
                robo.setPos(targetPos);
                done();
            } else {
                if (collisionDetected) {
                    roboEntity.jumpFromGround();
                    robo.setTargetVelocity(new Vec3(flatDir.x * speed, .4f, flatDir.z * speed));
                } else
                    robo.setTargetVelocity(new Vec3(flatDir.x * speed, 0, flatDir.z * speed));
                roboEntity.lookAt(EntityAnchorArgument.Anchor.EYES, targetPos);
                if (startPos.distanceTo(roboEntity.position()) >= 1f + count)
                    ++count;
            }
        }
    }

    private void handleWalkTo(PseudoRobo robo) {
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity != null) {
            Vec3 targetPos = new Vec3(args[0], args[1], args[2]);
            roboEntity.lookAt(EntityAnchorArgument.Anchor.EYES, targetPos);
            Vec3 dir = targetPos.subtract(robo.getPos());
            double dist = dir.length();
            if (dist < 0.05) {
                robo.setPos(targetPos);
                robo.setTargetVelocity(Vec3.ZERO);
                args = new float[]{};
                done();
            } else {
                final float initialSpeed = robo.getSpeed() / 20f;
                float usedSpeed = initialSpeed;

                Vec3 probe = dir.normalize().scale(.55f);
                AABB movedBox = roboEntity.getBoundingBox().move(probe);
                boolean collisionDetected = !serverLevel.noBlockCollision(roboEntity, movedBox);
                if (collisionDetected) {
                    AABB steppedBox = movedBox.move(0, 1, 0);
                    if (serverLevel.noBlockCollision(roboEntity, steppedBox)) {
                        targetPos = targetPos.add(0, 1, 0);
                        usedSpeed *= .55f;
                    } else {
                        robo.setTargetVelocity(Vec3.ZERO);
                        done();
                        return;
                    }
                }

                if (robo.getPos().distanceTo(targetPos) < initialSpeed * 20) {
                    robo.setTargetVelocity(Vec3.ZERO);
                    robo.setPos(targetPos);
                    done();
                    return;
                }

                Vec3 flatDir = Vec3.directionFromRotation(0f, roboEntity.getYRot()).normalize();
                if (collisionDetected) {
                    roboEntity.jumpFromGround();
                    robo.setTargetVelocity(new Vec3(flatDir.x * usedSpeed, .4f, flatDir.z * usedSpeed));
                } else
                    robo.setTargetVelocity(new Vec3(flatDir.x * usedSpeed, 0, flatDir.z * usedSpeed));
            }
        }
    }

    private void handleJump() {
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity != null) {
            roboEntity.jumpFromGround();
            done();
        }
    }

    public void done() {
        setState(State.Idle);
    }

    public void setRoboRef(WeakReference<RoboEntity> roboRef) {
        this.roboRef = roboRef;
    }

    public void setState(State state, float[] args) {
        this.state = state;
        this.args = args;
        count = 0;
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity != null)
            startPos = roboEntity.position();
    }

    public void setState(State state) {
        setState(state, new float[]{});
    }
}
