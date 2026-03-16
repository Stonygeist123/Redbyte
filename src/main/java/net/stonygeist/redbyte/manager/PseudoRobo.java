package net.stonygeist.redbyte.manager;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.index.RedbyteEntities;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.interpreter.analysis.Lexer;
import net.stonygeist.redbyte.interpreter.analysis.Parser;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.stmt.Stmt;
import net.stonygeist.redbyte.interpreter.binder.Binder;
import net.stonygeist.redbyte.interpreter.binder.stmt.BoundStmt;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import net.stonygeist.redbyte.interpreter.lowerer.Lowerer;
import net.stonygeist.redbyte.server.C2SBuildResultPacket;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;

public final class PseudoRobo {
    private final UUID redbyteID;
    private WeakReference<RoboEntity> entityRef = new WeakReference<>(null);
    private ServerLevel serverLevel;
    private Evaluator evaluator;
    private Vec3 currentPos;
    private String code;
    private boolean buildDone;
    private DiagnosticBag diagnostics;
    private ImmutableList<BoundStmt> buildResult = ImmutableList.of();
    private float speed;
    private ItemStackHandler inventory;
    private final Stack<LivingEntity> followPlayerGoalProp = new Stack<>();
    private final Stack<Float> walkGoalProp = new Stack<>();
    private final Stack<Vec3> walkToGoalProp = new Stack<>();
    private final Stack<BlockPos> destroyBlockGoalProp = new Stack<>();
    private final Stack<LivingEntity> attackGoalProp = new Stack<>();

    public PseudoRobo(ServerLevel serverLevel, UUID redbyteID, BlockPos currentPos, String code, ItemStackHandler inventory, boolean buildDone, DiagnosticBag diagnostics) {
        this.redbyteID = redbyteID;
        this.serverLevel = serverLevel;
        this.currentPos = currentPos.getCenter().subtract(0, 0.5, 0);
        this.code = code;
        this.inventory = inventory;
        this.buildDone = buildDone;
        this.diagnostics = diagnostics;
        speed = RedbyteConfigs.ROBO_DEFAULT_SPEED;
    }

    public static PseudoRobo deserializeNBT(ServerLevel level, CompoundTag tag) {
        UUID redbyteID = tag.getUUID("redbyteID");
        String code = tag.getString("code");
        Vec3 pos = readVec3FromTag(tag, "pos");
        DiagnosticBag diagnostics = new DiagnosticBag();
        boolean buildDone = tag.getBoolean("buildDone");
        ListTag diagnosticsList = tag.getList("diagnostics", Tag.TAG_COMPOUND);
        for (int i = 0; i < diagnosticsList.size(); ++i) {
            CompoundTag diagnosticTag = diagnosticsList.getCompound(i);
            Diagnostic diagnostic = Diagnostic.deserializeNBT(diagnosticTag, i);
            diagnostics.add(diagnostic);
        }


        CompoundTag inventoryTag = tag.getCompound("inventory");
        ListTag listTag = inventoryTag.getList("slots", Tag.TAG_COMPOUND);
        ItemStackHandler inventory;
        if (!inventoryTag.contains("slots"))
            inventory = new ItemStackHandler(9);
        else {
            inventory = new ItemStackHandler(listTag.size());
            for (int i = 0; i < listTag.size(); ++i) {
                Optional<ItemStack> itemStack;
                if (listTag.get(i) instanceof CompoundTag ct && ct.isEmpty())
                    itemStack = Optional.of(ItemStack.EMPTY);
                else
                    itemStack = ItemStack.parse(level.registryAccess(), listTag.get(i));
                inventory.setStackInSlot(i, itemStack.orElse(ItemStack.EMPTY));
            }
        }

        return new PseudoRobo(level, redbyteID, BlockPos.containing(pos), code, inventory, buildDone, diagnostics);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("redbyteID", redbyteID);
        writeVec3ToTag(tag, "pos", currentPos);
        tag.putString("code", code);
        tag.putBoolean("buildDone", buildDone);
        diagnostics.serializeNBTToTag(tag);

        CompoundTag inventoryTag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack itemStack = getItem(i);
            if (itemStack.isEmpty())
                listTag.add(new CompoundTag());
            else
                listTag.add(itemStack.save(serverLevel.registryAccess()));
        }
        inventoryTag.put("slots", listTag);
        tag.put("inventory", inventoryTag);
        return tag;
    }

    public RoboEntity resolveEntity(ServerLevel level) {
        RoboEntity entity = getEntity();
        if (entity != null && !entity.isRemoved()) return entity;
        for (Entity e : level.getAllEntities())
            if (e instanceof RoboEntity robo && redbyteID.equals(robo.getRedbyteID().orElse(null))) {
                setEntity(robo);
                return robo;
            }

        return null;
    }

    private void updateEntity() {
        RoboEntity roboEntity = resolveEntity(serverLevel);
        if (roboEntity != null) {
            currentPos = roboEntity.position();
            inventory = roboEntity.getInventory();
            RoboRegistry.get(serverLevel).setDirty();
        }
    }

    public void tick(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;

        // Spawn / despawn RoboEntity if needed
        BlockPos pos = BlockPos.containing(currentPos);
        if (serverLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            if (getEntity() == null)
                spawnAndRememberEntity();
        } else if (getEntity() != null)
            despawnEntity();

        updateEntity();
        if (evaluator != null) {
            @Nullable Evaluator.EvaluationError error = evaluator.tick(this);
            if (error != null) {
                getEntity().setRuntimeError(error);
                stopEvaluation();
            } else if (evaluator.getFinished())
                stopEvaluation();
        }
    }

    public void build() {
        stopEvaluation();
        diagnostics = new DiagnosticBag();
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.lex();

        Parser parser = new Parser(tokens.toArray(new Token[0]), lexer.getDiagnostics());
        Stmt[] stmts = parser.parse();
        diagnostics = parser.getDiagnostics();
        if (diagnostics.isEmpty()) {
            Binder binder = new Binder(stmts);
            buildResult = binder.bind();
            diagnostics = binder.getDiagnostics();
        }

        buildDone = true;
        getEntity().setDiagnostics(diagnostics);
        if (diagnostics.isEmpty())
            Redbyte.CHANNEL.send(new C2SBuildResultPacket(redbyteID, true, DiagnosticBag.EMPTY), PacketDistributor.SERVER.noArg());
        else
            Redbyte.CHANNEL.send(new C2SBuildResultPacket(redbyteID, true, diagnostics.serializeNBT()), PacketDistributor.SERVER.noArg());
        getEntity().setRuntimeError(null);
        getEntity().clearPrint();
    }

    public void evaluate() {
        getEntity().setRuntimeError(null);
        if (diagnostics.isEmpty() && !buildResult.isEmpty()) {
            destroyBlockGoalProp.clear();
            evaluator = new Evaluator(buildResult.stream().map(Lowerer::lower).collect(ImmutableList.toImmutableList()), this);
            getEntity().setIsRuntime(true);
        }

        getEntity().clearPrint();
    }

    private void despawnEntity() {
        RoboEntity entity = getEntity();
        if (entity != null)
            entity.discard();
        entityRef.clear();
    }

    private void spawnAndRememberEntity() {
        RoboEntity robo = new RoboEntity(RedbyteEntities.ROBO.get(), serverLevel);
        robo.setRedbyteID(redbyteID);
        robo.setPos(currentPos.x, currentPos.y, currentPos.z);
        serverLevel.addFreshEntity(robo);
        setEntity(robo);
    }

    public static Vec3 readVec3FromTag(CompoundTag tag, String key) {
        double x = tag.getDouble(key + "X");
        double y = tag.getDouble(key + "Y");
        double z = tag.getDouble(key + "Z");
        return new Vec3(x, y, z);
    }

    public static void writeVec3ToTag(CompoundTag tag, String key, Vec3 vec) {
        tag.putDouble(key + "X", vec.x);
        tag.putDouble(key + "Y", vec.y);
        tag.putDouble(key + "Z", vec.z);
    }

    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    public float getSpeed() {
        return speed / 3f;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public UUID getRedbyteID() {
        return redbyteID;
    }

    public RoboEntity getEntity() {
        return entityRef.get();
    }

    public void setEntity(RoboEntity entity) {
        entity.setRedbyteID(redbyteID);
        entity.setCode(code);
        entity.setBuildDone(buildDone);
        entity.setDiagnostics(diagnostics);
        entityRef = new WeakReference<>(entity);
    }

    public void setCode(String code) {
        this.code = code;
        getEntity().setCode(code);
    }

    public void setBuildDone(boolean buildDone) {
        this.buildDone = buildDone;
        getEntity().setBuildDone(buildDone);
    }

    public void setDiagnostics(DiagnosticBag diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Stack<LivingEntity> getFollowPlayerGoalProp() {
        return followPlayerGoalProp;
    }

    public void addFollowEntityGoalProp(LivingEntity followPlayerGoalProp) {
        this.followPlayerGoalProp.add(followPlayerGoalProp);
    }

    public void popFollowEntityGoalProp() {
        followPlayerGoalProp.pop();
    }

    public @Nullable Float popWalkGoalProp() {
        return walkGoalProp.isEmpty() ? null : walkGoalProp.pop();
    }

    public void addWalkGoalProp(Float walkGoalProp) {
        this.walkGoalProp.add(walkGoalProp);
    }

    public @Nullable Vec3 popWalkToGoalProp() {
        return walkGoalProp.isEmpty() ? null : walkToGoalProp.pop();
    }

    public void addWalkToGoalProp(Vec3 walkToGoalProp) {
        this.walkToGoalProp.add(walkToGoalProp);
    }

    public @Nullable BlockPos popDestroyBlockGoalProp() {
        return destroyBlockGoalProp.isEmpty() ? null : destroyBlockGoalProp.pop();
    }

    public void addDestroyBlockGoalProp(BlockPos destroyBlockGoalProp) {
        this.destroyBlockGoalProp.add(destroyBlockGoalProp);
    }

    public @Nullable LivingEntity popAttackGoalProp() {
        return attackGoalProp.isEmpty() ? null : attackGoalProp.pop();
    }

    public void addAttackGoalProp(LivingEntity attackGoalProp) {
        this.attackGoalProp.add(attackGoalProp);
    }

    public void stopEvaluation() {
        getEntity().setIsRuntime(false);
        evaluator = null;
    }
}
