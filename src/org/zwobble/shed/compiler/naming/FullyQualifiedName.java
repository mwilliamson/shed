package org.zwobble.shed.compiler.naming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

import lombok.Data;

import static java.util.Arrays.asList;

@Data
public class FullyQualifiedName {
    public static final FullyQualifiedName EMPTY = fullyQualifiedName();

    public static FullyQualifiedName fullyQualifiedName(String... identifiers) {
        return fullyQualifiedName(asList(identifiers));
    }
    
    public static FullyQualifiedName fullyQualifiedName(List<String> identifiers) {
        return new FullyQualifiedName(identifiers);
    }
    
    private final List<String> identifiers;

    public FullyQualifiedName extend(String identifier) {
        return extend(Collections.singletonList(identifier));
    }
    
    public FullyQualifiedName extend(List<String> extraIdentifiers) {
        List<String> extendedIdentifiers = new ArrayList<String>(identifiers);
        extendedIdentifiers.addAll(extraIdentifiers);
        return new FullyQualifiedName(extendedIdentifiers);
    }
    
    public String last() {
        return identifiers.get(identifiers.size() - 1);
    }
    
    public FullyQualifiedName replaceLast(String last) {
        List<String> replacedIdentifiers = new ArrayList<String>(identifiers);
        replacedIdentifiers.set(replacedIdentifiers.size() - 1, last);
        return new FullyQualifiedName(replacedIdentifiers);
    }
    
    @Override
    public String toString() {
        return asString();
    }
    
    public String asString() {
        return Joiner.on(".").join(identifiers);
    }
}
