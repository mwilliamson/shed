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
        return new FullyQualifiedName(asList(identifiers));
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
    
    @Override
    public String toString() {
        return Joiner.on(".").join(identifiers);
    }
}
