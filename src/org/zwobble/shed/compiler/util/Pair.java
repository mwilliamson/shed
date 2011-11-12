package org.zwobble.shed.compiler.util;

import lombok.Data;

@Data(staticConstructor="pair")
public class Pair<T, U> {
    private final T first;
    private final U second;
}
