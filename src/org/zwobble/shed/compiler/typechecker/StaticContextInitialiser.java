package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;


public interface StaticContextInitialiser {
    void initialise(StaticContext staticContext, BuiltIns builtIns, MetaClasses metaClasses);
}
