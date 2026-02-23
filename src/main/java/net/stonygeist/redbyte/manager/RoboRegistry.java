package net.stonygeist.redbyte.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RoboRegistry extends SavedData {
    public final Map<UUID, PseudoRobo> robos = new ConcurrentHashMap<>();

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag robosList = new ListTag();
        for (PseudoRobo robo : robos.values())
            robosList.add(robo.serializeNBT());
        tag.put("robos", robosList);
        return tag;
    }

    public static RoboRegistry load(CompoundTag tag, ServerLevel level) {
        RoboRegistry manager = new RoboRegistry();
        ListTag robosList = tag.getList("robos", Tag.TAG_COMPOUND);
        for (int i = 0; i < robosList.size(); i++) {
            CompoundTag roboTag = robosList.getCompound(i);
            PseudoRobo robo = PseudoRobo.deserializeNBT(level, roboTag);
            manager.robos.put(robo.getRedbyteID(), robo);
        }
        return manager;
    }

    public static RoboRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(RoboRegistry::new, (tag, provider) -> load(tag, level), DataFixTypes.ENTITY_CHUNK), "redbyte_robo_registry"
        );
    }

    public @Nullable PseudoRobo get(UUID redbyteID) {
        return robos.get(redbyteID);
    }

    public void remove(UUID redbyteID) {
        robos.remove(redbyteID);
        setDirty();
    }

    public void add(PseudoRobo robo) {
        robos.put(robo.getRedbyteID(), robo);
        setDirty();
    }

    public void newRobo(ServerLevel level, BlockPos spawnPos) {
        UUID id = UUID.randomUUID();
        PseudoRobo robo = new PseudoRobo(level, id, spawnPos, "");
        add(robo);
        setDirty();
    }

    public void tick(ServerLevel level) {
        robos.values().forEach(robo -> robo.tick(level));
        setDirty();
    }

    public void ensureExists(UUID redbyteID, RoboEntity entity) {
        PseudoRobo robo = robos.computeIfAbsent(redbyteID, (id) -> new PseudoRobo((ServerLevel) entity.level(), id, entity.blockPosition(), entity.getCode()));
        robo.setEntity(entity);
    }
}
