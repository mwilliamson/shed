package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.FormalTypeParameter.Variance;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.util.Predicate3;
import org.zwobble.shed.compiler.util.ShedIterables;
import org.zwobble.shed.compiler.util.Triple;

import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.all;
import static org.zwobble.shed.compiler.util.ShedIterables.zip;

public class SubTyping {
    private final StaticContext context;

    @Inject
    public SubTyping(StaticContext context) {
        this.context = context;
    }
    
    public boolean isSubType(Type subType, Type superType) {
        if (subType.equals(superType)) {
            return true;
        }
        
        if (subType instanceof TypeApplication && superType instanceof TypeApplication) {
            if (typeApplicationIsSubType((TypeApplication)subType, (TypeApplication)superType)) {
                return true;
            }
        }
        
        if (subType instanceof ScalarType) {
            if (firstTypeImplementsSecondType(((ScalarType)subType), superType)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean typeApplicationIsSubType(TypeApplication subType, TypeApplication superType) {
        ParameterisedType subTypeParameterisedType = subType.getParameterisedType();
        return all(
            zip(subTypeParameterisedType.getFormalTypeParameters(), subType.getTypeParameters(), superType.getTypeParameters()),
            typeParametersMatch()
        );
    }
    
    private Predicate<Triple<FormalTypeParameter, Type, Type>> typeParametersMatch() {
        return ShedIterables.unpack(new Predicate3<FormalTypeParameter, Type, Type>() {
            @Override
            public boolean apply(FormalTypeParameter formalTypeParameter, Type subTypeParameter, Type superTypeParameter) {
                Variance variance = formalTypeParameter.getVariance();
                if (variance == Variance.INVARIANT) {
                    return subTypeParameter.equals(superTypeParameter);
                } else if (variance == Variance.COVARIANT) {
                    return isSubType(subTypeParameter, superTypeParameter);
                } else if (variance == Variance.CONTRAVARIANT) {
                    return isSubType(superTypeParameter, subTypeParameter);
                } else {
                    throw new RuntimeException("Unrecognised variance");
                }
            }
        });
    }

    private boolean firstTypeImplementsSecondType(ScalarType first, Type second) {
        Iterable<ScalarType> superTypes = context.getInfo(first).getSuperTypes();
        for (Type superType : superTypes) {
            if (isSubType(superType, second)) {
                return true;
            }
        }
        return false;
    }
}
