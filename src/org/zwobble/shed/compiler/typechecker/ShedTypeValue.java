package org.zwobble.shed.compiler.typechecker;

import lombok.Data;

import org.zwobble.shed.compiler.types.Type;

@Data(staticConstructor="shedTypeValue")
public class ShedTypeValue implements ShedValue {
    private final Type type;
}
