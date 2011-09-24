package org.zwobble.shed.compiler.typechecker;

import com.google.inject.Guice;

public class TypeCheckerInjector {
    public static <T> T inject(Class<T> clazz) {
        return Guice.createInjector().getInstance(clazz);
    }
}
