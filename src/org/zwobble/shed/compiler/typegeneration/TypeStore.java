package org.zwobble.shed.compiler.typegeneration;

import java.util.Map;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

@AllArgsConstructor
public class TypeStore {
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
}
