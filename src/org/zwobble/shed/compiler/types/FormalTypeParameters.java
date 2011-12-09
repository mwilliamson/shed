package org.zwobble.shed.compiler.types;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.util.Eager;
import org.zwobble.shed.compiler.util.Function2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static org.zwobble.shed.compiler.util.ShedIterables.unpack;
import static org.zwobble.shed.compiler.util.ShedIterables.zip;

import lombok.RequiredArgsConstructor;

import static java.util.Arrays.asList;

@RequiredArgsConstructor(staticName="formalTypeParameters")
public class FormalTypeParameters implements Iterable<FormalTypeParameter> {
    public static FormalTypeParameters formalTypeParameters(FormalTypeParameter... formalTypeParameters) {
        return formalTypeParameters(asList(formalTypeParameters));
    }
    
    private final List<FormalTypeParameter> formalTypeParameters;
    
    @Override
    public Iterator<FormalTypeParameter> iterator() {
        return formalTypeParameters.iterator();
    }
    
    public Map<FormalTypeParameter, Type> replacementMap(Iterable<Type> actualTypeParameters) {
        ImmutableMap.Builder<FormalTypeParameter, Type> replacements = ImmutableMap.builder();
        Eager.transform(zip(formalTypeParameters, actualTypeParameters), unpack(putReplacement(replacements)));
        return replacements.build();
    }

    private Function2<FormalTypeParameter, Type, Void> putReplacement(final Builder<FormalTypeParameter, Type> replacements) {
        return new Function2<FormalTypeParameter, Type, Void>() {
            @Override
            public Void apply(FormalTypeParameter first, Type second) {
                replacements.put(first, second);
                return null;
            }
        };
    }
}
