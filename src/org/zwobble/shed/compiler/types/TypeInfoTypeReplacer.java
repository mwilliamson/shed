package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.types.Member.member;
import static org.zwobble.shed.compiler.types.Members.members;

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
        return new ScalarTypeInfo(buildSuperTypes(info.getSuperTypes(), replacements), buildMembers(info.getMembers(), replacements));
    }

    private Set<Type> buildSuperTypes(Set<Type> superTypes, Map<FormalTypeParameter, Type> replacements) {
        return ImmutableSet.copyOf(transform(superTypes, replaceTypes(replacements)));
    }

    private Members buildMembers(Members members, Map<FormalTypeParameter, Type> replacements) {
        return members(transform(members, replaceTypesInMember(replacements)));
    }

    private Function<Member, Member> replaceTypesInMember(final Map<FormalTypeParameter, Type> replacements) {
        return new Function<Member, Member>() {
            @Override
            public Member apply(Member input) {
                Type type = typeReplacer.replaceTypes(input.getType(), replacements);
                ValueInfo valueInfo = input.getValueInfo();
                if (valueInfo.isAssignable()) {
                    return member(input.getName(), ValueInfo.assignableValue(type));
                } else {
                    return member(input.getName(), ValueInfo.unassignableValue(type));
                }
            }
        };
    }

    private Function<Type, Type> replaceTypes(final Map<FormalTypeParameter, Type> replacements) {
        return new Function<Type, Type>() {
            @Override
            public Type apply(Type input) {
                return typeReplacer.replaceTypes(input, replacements);
            }
        };
    }
}
