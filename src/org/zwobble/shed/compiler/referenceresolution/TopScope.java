package org.zwobble.shed.compiler.referenceresolution;

public class TopScope implements Scope {
    @Override
    public Result lookup(String identifier) {
        return new NotInScope();
    }
}
