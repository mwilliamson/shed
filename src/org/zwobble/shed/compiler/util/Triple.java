package org.zwobble.shed.compiler.util;

import lombok.Data;

@Data(staticConstructor="triple")
public class Triple<T, U, V> {
    private final T first;
    private final U second;
    private final V third;
}
