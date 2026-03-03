package net.stonygeist.redbyte.interpreter.diagnostics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;

public final class DiagnosticBag extends ArrayList<Diagnostic> {
    public static final CompoundTag EMPTY = new DiagnosticBag().serializeNBT();

    public static DiagnosticBag deserializeNBT(CompoundTag tag) {
        DiagnosticBag diagnostics = new DiagnosticBag();
        ListTag diagnosticsList = tag.getList("diagnostics", Tag.TAG_COMPOUND);
        for (int i = 0; i < diagnosticsList.size(); ++i) {
            CompoundTag diagnosticTag = diagnosticsList.getCompound(i);
            Diagnostic diagnostic = Diagnostic.deserializeNBT(diagnosticTag, i);
            diagnostics.add(diagnostic);
        }

        return diagnostics;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag diagnosticsList = new ListTag();
        for (int i = 0; i < size(); i++)
            diagnosticsList.add(get(i).serializeNBT(i));

        tag.put("diagnostics", diagnosticsList);
        return tag;
    }

    public void serializeNBTToTag(CompoundTag tag) {
        ListTag diagnosticsList = new ListTag();
        for (int i = 0; i < size(); i++)
            diagnosticsList.add(get(i).serializeNBT(i));

        tag.put("diagnostics", diagnosticsList);
    }
}
