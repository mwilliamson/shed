package org.zwobble.shed.compiler.typechecker;

import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.FormalTypeParameters;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.Variance;
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
        
        if (superType.equals(CoreTypes.ANY)) {
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
        if (!subType.getParameterisedType().equals(superType.getParameterisedType())) {
            return false;
        }
        ParameterisedType parameterisedType = subType.getParameterisedType();
        FormalTypeParameters formalTypeParameters = parameterisedType.getFormalTypeParameters();
        
        // TODO: these two cases aren't tested properly
        // TODO: is special-casing tuple here the right solution, or should the tuple class itself be a unique type in the type system?
        if (parameterisedType.equals(CoreTypes.TUPLE)) {
            return all(zip(formalTypeParameters, subType.getTypeParameters(), superType.getTypeParameters()), typeParametersMatch());
        } else {
            Map<FormalTypeParameter, Type> subTypeBindings = formalTypeParameters.replacementMap(subType.getTypeParameters());
            Map<FormalTypeParameter, Type> superTypeBindings = formalTypeParameters.replacementMap(superType.getTypeParameters());
            
            for (FormalTypeParameter formalTypeParameter : formalTypeParameters) {
                Type subTypeBinding = subTypeBindings.get(formalTypeParameter);
                Type superTypeBinding = superTypeBindings.get(formalTypeParameter);
                if (!typeParametersMatch(formalTypeParameter, subTypeBinding, superTypeBinding)) {
                    return false;
                }
            }
            return true;
        }
        
    }

    private boolean typeParametersMatch(FormalTypeParameter formalTypeParameter, Type subTypeParameter, Type superTypeParameter) {
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

    private boolean firstTypeImplementsSecondType(ScalarType first, Type second) {
        Iterable<ScalarType> superTypes = context.getInfo(first).getInterfaces();
        for (Type superType : superTypes) {
            if (isSubType(superType, second)) {
                return true;
            }
        }
        return false;
    }
    
    private Predicate<Triple<FormalTypeParameter, Type, Type>> typeParametersMatch() {
        return ShedIterables.unpack(new Predicate3<FormalTypeParameter, Type, Type>() {
            @Override
            public boolean apply(FormalTypeParameter formalTypeParameter, Type subType, Type superType) {
                return typeParametersMatch(formalTypeParameter, subType, superType);
            }
        });
    }
}
