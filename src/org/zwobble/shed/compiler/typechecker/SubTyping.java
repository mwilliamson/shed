package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

public class SubTyping {
    public static boolean isSubType(Type subType, Type superType, StaticContext context) {
        if (subType.equals(superType)) {
            return true;
        }
        if (subType instanceof ScalarType) {
            if (firstTypeImplementsSecondType(((ScalarType)subType), superType, context)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean firstTypeImplementsSecondType(ScalarType first, Type second, StaticContext context) {
        return context.getInfo(first).getSuperTypes().contains(second);
    }
}
