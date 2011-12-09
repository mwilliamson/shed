package org.zwobble.shed.compiler.typechecker.expressions;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultWithValue;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.FormalTypeParameters;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeReplacer;
import org.zwobble.shed.compiler.util.Eager;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static org.zwobble.shed.compiler.Results.isSuccess;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfos.toUnassignableValueInfo;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;

public class TypeApplicationTypeInferer implements ExpressionTypeInferer<TypeApplicationNode> {
    private final TypeInferer typeInferer;
    private final TypeLookup typeLookup;
    private final MetaClasses metaClasses;

    @Inject
    public TypeApplicationTypeInferer(TypeInferer typeInferer, TypeLookup typeLookup, MetaClasses metaClasses) {
        this.typeInferer = typeInferer;
        this.typeLookup = typeLookup;
        this.metaClasses = metaClasses;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(final TypeApplicationNode typeApplication) {
        return typeInferer.inferType(typeApplication.getBaseValue()).ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type baseType) {
                List<Type> actualTypeParameters = Lists.transform(typeApplication.getParameters(), toParameterType());
                
                if (baseType instanceof ParameterisedFunctionType) {
                    ParameterisedFunctionType functionType = (ParameterisedFunctionType)baseType;
                    FormalTypeParameters formalTypeParameters = functionType.getFormalTypeParameters();
                    Map<FormalTypeParameter, Type> replacements = formalTypeParameters.replacementMap(actualTypeParameters);
                    return success((Type)CoreTypes.functionTypeOf(Eager.transform(functionType.getFunctionTypeParameters(), toReplacement(replacements))));
                } else if (baseType instanceof ParameterisedType) {
                    return success((Type)metaClasses.metaClassOf(applyTypes((ParameterisedType)baseType, actualTypeParameters)));   
                } else {
                    throw new RuntimeException("Don't know how to apply types " + actualTypeParameters + " to " + baseType);
                }
            }
        }).ifValueThen(toUnassignableValueInfo());
    }

    private Function<Type, Type> toReplacement(final Map<FormalTypeParameter, Type> replacements) {
        return new Function<Type, Type>() {
            @Override
            public Type apply(Type input) {
                return new TypeReplacer().replaceTypes(input, replacements);
            }
        };
    }

    private Function<ExpressionNode, Type> toParameterType() {
        return new Function<ExpressionNode, Type>() {
            @Override
            public Type apply(ExpressionNode expression) {
                TypeResultWithValue<Type> result = typeLookup.lookupTypeReference(expression);
                if (!isSuccess(result)) {
                    // TODO: handle failure
                    throw new RuntimeException(result.getErrors().toString());
                }
                return result.get();
            }
        };
    }
}
