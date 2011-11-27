package org.zwobble.shed.compiler.types;

import java.util.Iterator;
import java.util.Set;

import lombok.EqualsAndHashCode;

import com.google.common.collect.ImmutableSet;

@EqualsAndHashCode
public class Interfaces implements Iterable<ScalarType> {
    public static Interfaces interfaces(ScalarType... interfaces) {
        return new Interfaces(ImmutableSet.copyOf(interfaces));
    }
    
    public static Interfaces interfaces(Iterable<ScalarType> interfaces) {
        return new Interfaces(ImmutableSet.copyOf(interfaces));
    }
    
    private final Set<ScalarType> interfaces;
    
    private Interfaces(Set<ScalarType> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public Iterator<ScalarType> iterator() {
        return interfaces.iterator();
    }
}
