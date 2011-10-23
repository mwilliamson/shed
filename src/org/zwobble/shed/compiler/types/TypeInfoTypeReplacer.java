package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class TypeInfoTypeReplacer {
    private final TypeReplacer typeReplacer;

    public TypeInfoTypeReplacer(TypeReplacer typeReplacer) {
        this.typeReplacer = typeReplacer;
    }
    
    public ScalarTypeInfo buildTypeInfo(TypeApplication typeApplication, ScalarTypeInfo info) {
        ImmutableMap.Builder<FormalTypeParameter, Type> replacements = ImmutableMap.builder();
        
        List<Type> actualTypeParameters = typeApplication.getTypeParameters();
        List<FormalTypeParameter> formalTypeParameters = typeApplication.getParameterisedType().getFormalTypeParameters();
        for (int i = 0; i < actualTypeParameters.size(); i += 1) {
            replacements.put(formalTypeParameters.get(i), actualTypeParameters.get(i));
        }
        
        return buildTypeInfo(info, replacements.build());
    }
    
    private ScalarTypeInfo buildTypeInfo(ScalarTypeInfo info, Map<FormalTypeParameter, Type> replacements) {
        return new ScalarTypeInfo(interfaces(), buildMembers(info.getMembers(), replacements));
    }

    private Map<String, ValueInfo> buildMembers(Map<String, ValueInfo> members, Map<FormalTypeParameter, Type> replacements) {
        return Maps.transformValues(members, replaceTypes(replacements));
    }

    private Function<ValueInfo, ValueInfo> replaceTypes(final Map<FormalTypeParameter, Type> replacements) {
        return new Function<ValueInfo, ValueInfo>() {
            @Override
            public ValueInfo apply(ValueInfo input) {
                Type type = typeReplacer.replaceTypes(input.getType(), replacements);
                if (input.isAssignable()) {
                    return ValueInfo.assignableValue(type);
                } else {
                    return ValueInfo.unassignableValue(type);
                }
            }
        };
    }

}
