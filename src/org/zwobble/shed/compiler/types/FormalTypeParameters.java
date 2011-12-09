package org.zwobble.shed.compiler.types;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

@RequiredArgsConstructor(staticName="formalTypeParameters")
@ToString
public class FormalTypeParameters implements Iterable<FormalTypeParameter> {
    public static FormalTypeParameters formalTypeParameters(FormalTypeParameter... formalTypeParameters) {
        return formalTypeParameters(asList(formalTypeParameters));
    }
    
    private final List<FormalTypeParameter> formalTypeParameters;
    
    @Override
    public Iterator<FormalTypeParameter> iterator() {
        return formalTypeParameters.iterator();
    }
    
    public String describe() {
        return "[" + Joiner.on(", ").join(transform(formalTypeParameters, toName())) + "]";
    }
    
    public Map<FormalTypeParameter, Type> replacementMap(Iterable<Type> actualTypeParameters) {
        ImmutableMap.Builder<FormalTypeParameter, Type> replacements = ImmutableMap.builder();
        
        Deque<FormalTypeParameter> remainingFormalTypeParameters = Lists.newLinkedList(formalTypeParameters);
        Deque<Type> remainingActualTypeParameters = Lists.newLinkedList(actualTypeParameters);
        
        while (!remainingFormalTypeParameters.isEmpty() && !isVariadic(remainingFormalTypeParameters.peekFirst())) {
            replacements.put(remainingFormalTypeParameters.removeFirst(), remainingActualTypeParameters.removeFirst());
        }
        
        while (!remainingFormalTypeParameters.isEmpty() && !isVariadic(remainingFormalTypeParameters.peekLast())) {
            replacements.put(remainingFormalTypeParameters.removeLast(), remainingActualTypeParameters.removeLast());
        }
        
        if (!remainingFormalTypeParameters.isEmpty()) {
            replacements.put(remainingFormalTypeParameters.removeFirst(), CoreTypes.tupleOf(remainingActualTypeParameters));
        }
        
        return replacements.build();
    }
    
    private boolean isVariadic(FormalTypeParameter formalTypeParameter) {
        return formalTypeParameter instanceof VariadicFormalTypeParameter;
    }

    private Function<Type, String> toName() {
        return new Function<Type, String>() {
            @Override
            public String apply(Type input) {
                return input.shortName();
            }
        };
    }
}
