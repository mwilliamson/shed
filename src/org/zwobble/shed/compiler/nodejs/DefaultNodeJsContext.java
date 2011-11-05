package org.zwobble.shed.compiler.nodejs;

import org.zwobble.shed.compiler.typechecker.DefaultContext;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;

import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class DefaultNodeJsContext {
    public static StaticContext defaultNodeJsContext() {
        StaticContext context = DefaultContext.defaultContext();

        ClassType sysType = new ClassType(fullyQualifiedName("shed", "sys"));
        ScalarTypeInfo sysTypeInfo = new ScalarTypeInfo(
            interfaces(),
            members("print", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)))
        );
        
        context.addGlobal(fullyQualifiedName("shed", "sys"), sysType);
        context.addInfo(sysType, sysTypeInfo);
        return context;
    }
}
