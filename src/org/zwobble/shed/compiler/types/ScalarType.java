package org.zwobble.shed.compiler.types;

public class ScalarType implements Type {
    private final String name;

    public ScalarType(String name) {
        this.name = name;
    }
}
