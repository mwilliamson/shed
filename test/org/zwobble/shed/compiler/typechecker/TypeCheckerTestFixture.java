package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;

public class TypeCheckerTestFixture {
    public static TypeCheckerTestFixture build() {
        return new TypeCheckerTestFixture();
    }
    
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();
    private final FullyQualifiedNamesBuilder fullNames = new FullyQualifiedNamesBuilder();
    
    private TypeCheckerTestFixture() {
    }

    public <T> T get(Class<T> clazz) {
        return TypeCheckerInjector.build(nodeLocations, fullNames.build()).getInstance(clazz);
    }
    
    private StaticContext defaultContext() {
        return StaticContext.defaultContext(references.build());
    }
    
    public StaticContext blankContext() {
        return new StaticContext(references.build());
    }
}
