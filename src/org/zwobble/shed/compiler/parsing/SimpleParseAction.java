package org.zwobble.shed.compiler.parsing;

public interface SimpleParseAction<F, T> {
    T apply(F result);
}
