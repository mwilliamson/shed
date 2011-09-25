package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.NodeLocations;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TypeCheckerInjector {
    public static Injector build(NodeLocations nodeLocations) {
        return Guice.createInjector();
    }
}
