package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.NodeLocations;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TypeCheckerInjector {
    public static Injector build(NodeLocations nodeLocations, FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
        return Guice.createInjector(new TypeCheckerModule(nodeLocations, fullyQualifiedNames, context));
    }
    
    private static class TypeCheckerModule extends AbstractModule {
        private final NodeLocations nodeLocations;
        private final FullyQualifiedNames fullyQualifiedNames;
        private final StaticContext context;

        public TypeCheckerModule(NodeLocations nodeLocations, FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
            this.nodeLocations = nodeLocations;
            this.fullyQualifiedNames = fullyQualifiedNames;
            this.context = context;
        }

        @Override
        protected void configure() {
            bind(NodeLocations.class).toInstance(nodeLocations);
            bind(FullyQualifiedNames.class).toInstance(fullyQualifiedNames);
            bind(StaticContext.class).toInstance(context);
            bind(TypeLookup.class).to(TypeLookupImpl.class);
            bind(TypeInferer.class).to(TypeInfererImpl.class);
        }
    }
}
