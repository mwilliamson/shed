package org.zwobble.shed.compiler.referenceresolution;

import org.zwobble.shed.compiler.typechecker.CoreModule;

public class TopScope implements Scope {
    @Override
    public Result lookup(String identifier) {
        if (CoreModule.VALUES.containsKey(identifier)) {
            return new Success(CoreModule.GLOBAL_DECLARATIONS.get(identifier));
        } else {
            return new NotInScope();
        }
    }
}
