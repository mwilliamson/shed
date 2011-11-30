package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TypeCheckerInjector {
    public static Injector build(TypeStore typeStore, MetaClasses metaClasses, StaticContext context, References references) {
        return Guice.createInjector(new TypeCheckerModule(typeStore, metaClasses, context, references));
    }
    
    private static class TypeCheckerModule extends AbstractModule {
        private final TypeStore typeStore;
        private final StaticContext context;
        private final MetaClasses metaClasses;
        private final References references;

        public TypeCheckerModule(TypeStore typeStore, MetaClasses metaClasses, StaticContext context, References references) {
            this.typeStore = typeStore;
            this.metaClasses = metaClasses;
            this.context = context;
            this.references = references;
        }

        @Override
        protected void configure() {
            bind(TypeStore.class).toInstance(typeStore);
            bind(MetaClasses.class).toInstance(metaClasses);
            bind(StaticContext.class).toInstance(context);
            bind(References.class).toInstance(references);
            bind(TypeLookup.class).to(TypeLookupImpl.class);
            bind(TypeInferer.class).to(TypeInfererImpl.class);
            bind(ArgumentTypeInferer.class).to(ArgumentTypeInfererImpl.class);
        }
    }
}
