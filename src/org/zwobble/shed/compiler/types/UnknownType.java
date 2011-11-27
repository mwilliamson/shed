package org.zwobble.shed.compiler.types;

public class UnknownType implements Type {
    @Override
    public String shortName() {
        return "<unknown>";
    }
}