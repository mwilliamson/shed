package org.zwobble.shed.compiler.types;

import lombok.Data;

@Data(staticConstructor="parameterisedType")
public class ParameterisedType implements TypeFunction {
    private final ScalarType baseType;
    private final FormalTypeParameters formalTypeParameters;
    
    @Override
    public String shortName() {
        String parameterString = formalTypeParameters.describe(); 
        return parameterString + " -> Class[" + baseType.shortName() + parameterString + "]";
    }
}
