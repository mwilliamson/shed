package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

@Data
public class TypeApplication {
    private final TypeFunction typeFunction;
    private final List<Type> typeParameters;
}
