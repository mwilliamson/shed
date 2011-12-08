package org.zwobble.shed.compiler.typegeneration;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.util.Pair;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Iterators.transform;
import static org.zwobble.shed.compiler.util.Pair.pair;

@AllArgsConstructor
public class TypeStore implements Iterable<Pair<TypeDeclarationNode, Type>> {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ImmutableMap.Builder<Identity<TypeDeclarationNode>, Type> builder = ImmutableMap.builder();
        
        public void add(TypeDeclarationNode declaration, Type type) {
            builder.put(new Identity<TypeDeclarationNode>(declaration), type);
        }
        
        public TypeStore build() {
            return new TypeStore(builder.build());
        }
    }

    private final Map<Identity<TypeDeclarationNode>, Type> types;
    
    public Type typeDeclaredBy(TypeDeclarationNode declaration) {
        return types.get(new Identity<TypeDeclarationNode>(declaration));
    }
    
    @Override
    public Iterator<Pair<TypeDeclarationNode, Type>> iterator() {
        return transform(types.entrySet().iterator(), toPair());
    }
    
    private Function<Map.Entry<Identity<TypeDeclarationNode>, Type>, Pair<TypeDeclarationNode, Type>> toPair() {
        return new Function<Map.Entry<Identity<TypeDeclarationNode>,Type>, Pair<TypeDeclarationNode,Type>>() {
            @Override
            public Pair<TypeDeclarationNode, Type> apply(Entry<Identity<TypeDeclarationNode>, Type> input) {
                return pair(input.getKey().get(), input.getValue());
            }
        };
    }
}
