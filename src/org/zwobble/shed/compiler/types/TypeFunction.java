package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

@Data
public class TypeFunction implements Type {
    private final List<String> scope;
    private final String name;
    private final List<FormalTypeParameter> typeParameters;
}
