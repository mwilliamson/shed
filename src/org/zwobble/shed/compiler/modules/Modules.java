package org.zwobble.shed.compiler.modules;

import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.util.ShedMaps;

import com.google.common.base.Function;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.util.ShedMaps.getOrNone;

@EqualsAndHashCode
@ToString
public class Modules {
    public static Modules build(Iterable<Module> modules) {
        return new Modules(ShedMaps.toMapWithKeys(modules, useName()));
    }
    
    public static Modules build(Module... modules) {
        return build(asList(modules));
    }
    
    private static Function<Module, FullyQualifiedName> useName() {
        return new Function<Module, FullyQualifiedName>() {
            @Override
            public FullyQualifiedName apply(Module input) {
                return input.getName();
            }
        };
    }

    private final Map<FullyQualifiedName, Module> modules;
    
    private Modules(Map<FullyQualifiedName, Module> modules) {
        this.modules = modules;
    }
    
    public Option<Module> lookup(FullyQualifiedName name) {
        return getOrNone(modules, name);
    }
}
