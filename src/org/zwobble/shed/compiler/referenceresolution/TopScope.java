package org.zwobble.shed.compiler.referenceresolution;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.typechecker.BuiltIns;

public class TopScope implements Scope {
    private final BuiltIns builtIns;
    
    public TopScope(BuiltIns builtIns) {
        this.builtIns = builtIns;
    }
    
    @Override
    public Result lookup(String identifier) {
        Option<GlobalDeclaration> declarationOption = builtIns.get(identifier);
        if (declarationOption.hasValue()) {
            return new Success(declarationOption.get());
        } else {
            return new NotInScope();
        }
    }
}
