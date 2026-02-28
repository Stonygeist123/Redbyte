package net.stonygeist.redbyte.entity.robo;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.manager.PseudoRobo;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

public class BehaviourController {
    public enum State {
        Walk,
        WalkTo,
        Jump,
        Follow,
        StopFollow,
        Attack
    }

    public State state;
    public Object[] args;
    private WeakReference<RoboEntity> roboRef = new WeakReference<>(null);
    private final ServerLevel serverLevel;
    private int count;
    private Vec3 startPos;
    private final LinkedList<Map.Entry<State, Object[]>> stateQueue = new LinkedList<>();
    private ServerPlayer followPlayerTarget;

    public BehaviourController(PseudoRobo robo) {
        serverLevel = robo.serverLevel;
        startPos = robo.getPos();
    }

    public void tick(PseudoRobo robo) {
        switch (state) {
            case Walk:
                handleWalk(robo);
                break;
            case WalkTo:
                handleWalkTo(robo);
                break;
            case Jump:
                handleJump(robo);
                break;
            case Follow:
                handleFollow(robo);
                break;
            case StopFollow:
                handleStopFollow(robo);
                break;
            case Attack:
                handleAttack(robo);
                break;
            case null:
                robo.setTargetVelocity(Vec3.ZERO);
                if (!stateQueue.isEmpty())
                    done();
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void handleWalk(PseudoRobo robo) {
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity != null) {
            float speed = robo.getSpeed();
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

            if (count >= (Float) args[0]) {
                robo.setTargetVelocity(Vec3.ZERO);
                robo.setPos(targetPos);
                done();
            } else {
                if (collisionDetected) {
                    if (roboEntity.onGround()) {
                        roboEntity.jumpFromGround();
                        robo.setTargetVelocity(new Vec3(flatDir.x * speed, .4f, flatDir.z * speed));
                    }
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
            Vec3 targetPos = new Vec3((Float) args[0], (Float) args[1], (Float) args[2]);
            roboEntity.lookAt(EntityAnchorArgument.Anchor.EYES, targetPos);
            Vec3 dir = targetPos.subtract(robo.getPos());
            double dist = dir.length();
            if (dist < 0.05) {
                robo.setPos(targetPos);
                robo.setTargetVelocity(Vec3.ZERO);
                args = new Object[]{};
                done();
            } else {
                final float initialSpeed = robo.getSpeed();
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
                    if (roboEntity.onGround()) {
                        roboEntity.jumpFromGround();
                        robo.setTargetVelocity(new Vec3(flatDir.x * usedSpeed, .4f, flatDir.z * usedSpeed));
                    }
                } else
                    robo.setTargetVelocity(new Vec3(flatDir.x * usedSpeed, 0, flatDir.z * usedSpeed));
            }
        }
    }

    private void handleJump(PseudoRobo ignored) {
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity == null) return;

        if (roboEntity.onGround()) {
            if (count <= 0) {
                roboEntity.jumpFromGround();
                ++count;
            } else
                done();
        }
    }

    private void handleFollow(PseudoRobo ignored) {
        setPlayerTarget((String) args[0]);
        done();
    }

    private void handleStopFollow(PseudoRobo ignored) {
        setPlayerTarget(null);
        done();
    }

    private void handleAttack(PseudoRobo ignored) {
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity == null) return;
        ServerPlayer player = serverLevel.getPlayers(p -> p.getName().getString().equals(args[0])).getFirst();
        if (roboEntity.isWithinMeleeAttackRange(player))
            roboEntity.doHurtTarget(player);

        done();
    }

    private void done() {
        Map.Entry<State, Object[]> entry = stateQueue.peek();
        stateQueue.poll();
        if (entry == null) {
            state = null;
            args = new Object[][]{};
        } else {
            state = entry.getKey();
            args = entry.getValue();
        }

        count = 0;
        RoboEntity roboEntity = roboRef.get();
        if (roboEntity != null)
            startPos = roboEntity.position();
    }

    public void addState(State state, Object[] args) {
        stateQueue.add(new AbstractMap.SimpleEntry<>(state, args));
    }

    public void addState(State state) {
        stateQueue.add(new AbstractMap.SimpleEntry<>(state, new Object[]{}));
    }

    public void setRoboRef(WeakReference<RoboEntity> roboRef) {
        this.roboRef = roboRef;
    }

    public void setPlayerTarget(String playerName) {
        if (playerName == null)
            followPlayerTarget = null;
        else {
            var players = serverLevel.getPlayers(p -> p.getName().getString().equals(playerName));
            if (players.isEmpty())
                return;
            followPlayerTarget = players.getFirst();
        }
    }

    public ServerPlayer getPlayerTarget() {
        return followPlayerTarget;
    }
}
