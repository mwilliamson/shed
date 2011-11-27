package org.zwobble.shed.compiler.types;

public class AnyType implements Type {
    public static final Type ANY = new AnyType();

    private AnyType() {}
    
    @Override
    public String shortName() {
        return "<any type>";
    }
}
