package org.zwobble.shed.compiler.typechecker;

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
    public static boolean isSubType(Type subType, Type superType, StaticContext context) {
        if (subType.equals(superType)) {
            return true;
        }
        
        if (subType instanceof TypeApplication && superType instanceof TypeApplication) {
            if (typeApplicationIsSubType((TypeApplication)subType, (TypeApplication)superType, context)) {
                return true;
            }
        }
        
        if (subType instanceof ScalarType) {
            if (firstTypeImplementsSecondType(((ScalarType)subType), superType, context)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean typeApplicationIsSubType(TypeApplication subType, TypeApplication superType, StaticContext context) {
        ParameterisedType subTypeParameterisedType = subType.getParameterisedType();
        return all(
            zip(subTypeParameterisedType.getFormalTypeParameters(), subType.getTypeParameters(), superType.getTypeParameters()),
            typeParametersMatch(context)
        );
    }
    
    private static Predicate<Triple<FormalTypeParameter, Type, Type>> typeParametersMatch(final StaticContext context) {
        return ShedIterables.unpack(new Predicate3<FormalTypeParameter, Type, Type>() {
            @Override
            public boolean apply(FormalTypeParameter formalTypeParameter, Type subTypeParameter, Type superTypeParameter) {
                Variance variance = formalTypeParameter.getVariance();
                if (variance == Variance.INVARIANT) {
                    return subTypeParameter.equals(superTypeParameter);
                } else if (variance == Variance.COVARIANT) {
                    return isSubType(subTypeParameter, superTypeParameter, context);
                } else if (variance == Variance.CONTRAVARIANT) {
                    return isSubType(superTypeParameter, subTypeParameter, context);
                } else {
                    throw new RuntimeException("Unrecognised variance");
                }
            }
        });
    }

    private static boolean firstTypeImplementsSecondType(ScalarType first, Type second, StaticContext context) {
        Iterable<Type> superTypes = context.getInfo(first).getSuperTypes();
        for (Type superType : superTypes) {
            if (isSubType(superType, second, context)) {
                return true;
            }
        }
        return false;
    }
}
