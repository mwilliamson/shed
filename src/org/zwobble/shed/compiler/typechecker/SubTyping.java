package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

public class SubTyping {
    public static boolean isSubType(Type subType, Type superType) {
        if (subType.equals(superType)) {
            return true;
        }
        if (subType instanceof ScalarType) {
            if (((ScalarType)subType).superTypes().contains(superType)) {
                return true;
            }
        }
        return false;
    }
}
