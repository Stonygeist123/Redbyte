package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;

public record BoundGotoStmt(LabelSymbol label) implements BoundStmt {
}
