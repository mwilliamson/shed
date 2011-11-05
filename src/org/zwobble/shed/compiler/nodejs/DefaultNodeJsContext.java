package org.zwobble.shed.compiler.nodejs;

import java.util.Collections;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.typechecker.DefaultContext;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class DefaultNodeJsContext {
    public static StaticContext defaultNodeJsContext() {
        StaticContext context = DefaultContext.defaultContext();

        addNodeJavaScriptImporter(context);
        
//        ClassType sysType = new ClassType(fullyQualifiedName("shed", "sys"));
//        ScalarTypeInfo sysTypeInfo = new ScalarTypeInfo(
//            interfaces(),
//            members("print", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)))
//        );
//        
//        context.addGlobal(fullyQualifiedName("shed", "sys"), sysType);
//        context.addInfo(sysType, sysTypeInfo);
        return context;
    }

    private static void addNodeJavaScriptImporter(StaticContext context) {
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type importValueFromModuleType = new ParameterisedFunctionType(
            asList(CoreTypes.STRING, CoreTypes.STRING, formalTypeParameter),
            asList(formalTypeParameter)
        );
        FullyQualifiedName importerName = fullyQualifiedName("shed", "javascript", "NodeJavaScriptImporter");
        ClassType importerType = new ClassType(importerName);
        ScalarTypeInfo javaScriptImporterTypeInfo = new ScalarTypeInfo(
            interfaces(),
            members("importValueFromModule", unassignableValue(importValueFromModuleType))
        );

        context.addClass(globalDeclaration(importerName), importerType, Collections.<Type>emptyList(), javaScriptImporterTypeInfo);
        context.addGlobal(importerName, context.getMetaClass(importerType));
    }
}
