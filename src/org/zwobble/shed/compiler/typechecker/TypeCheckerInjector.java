package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.naming.FullyQualifiedNames;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TypeCheckerInjector {
    public static Injector build(FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
        return Guice.createInjector(new TypeCheckerModule(fullyQualifiedNames, context));
    }
    
    private static class TypeCheckerModule extends AbstractModule {
        private final FullyQualifiedNames fullyQualifiedNames;
        private final StaticContext context;

        public TypeCheckerModule(FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
            this.fullyQualifiedNames = fullyQualifiedNames;
            this.context = context;
        }

        @Override
        protected void configure() {
            bind(FullyQualifiedNames.class).toInstance(fullyQualifiedNames);
            bind(StaticContext.class).toInstance(context);
            bind(TypeLookup.class).to(TypeLookupImpl.class);
            bind(TypeInferer.class).to(TypeInfererImpl.class);
        }
    }
}
