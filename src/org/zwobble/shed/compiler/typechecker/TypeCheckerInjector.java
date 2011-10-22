package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.referenceresolution.References;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TypeCheckerInjector {
    public static Injector build(FullyQualifiedNames fullyQualifiedNames, StaticContext context, References references) {
        return Guice.createInjector(new TypeCheckerModule(fullyQualifiedNames, context, references));
    }
    
    private static class TypeCheckerModule extends AbstractModule {
        private final FullyQualifiedNames fullyQualifiedNames;
        private final StaticContext context;
        private final References references;

        public TypeCheckerModule(FullyQualifiedNames fullyQualifiedNames, StaticContext context, References references) {
            this.fullyQualifiedNames = fullyQualifiedNames;
            this.context = context;
            this.references = references;
        }

        @Override
        protected void configure() {
            bind(FullyQualifiedNames.class).toInstance(fullyQualifiedNames);
            bind(StaticContext.class).toInstance(context);
            bind(References.class).toInstance(references);
            bind(TypeLookup.class).to(TypeLookupImpl.class);
            bind(TypeInferer.class).to(TypeInfererImpl.class);
        }
    }
}
