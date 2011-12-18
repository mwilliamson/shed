package org.zwobble.shed.compiler.modules;

import java.util.Map;

import lombok.Data;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.util.ShedMaps;

import com.google.common.base.Function;

import static java.util.Arrays.asList;

@Data
public class Modules {
    public static Modules build(Iterable<Module> modules) {
        return new Modules(ShedMaps.toMapWithKeys(modules, useIdentifier()));
    }
    
    public static Modules build(Module... modules) {
        return build(asList(modules));
    }
    
    private static Function<Module, FullyQualifiedName> useIdentifier() {
        return new Function<Module, FullyQualifiedName>() {
            @Override
            public FullyQualifiedName apply(Module input) {
                return input.getIdentifier();
            }
        };
    }

    private final Map<FullyQualifiedName, Module> modules;
    
    private Modules(Map<FullyQualifiedName, Module> modules) {
        this.modules = modules;
    }
}
