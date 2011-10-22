package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class DefaultBrowserContext {
    public static StaticContext defaultBrowserContext() {
        StaticContext context = StaticContext.defaultContext();
        
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type importValueType = new ParameterisedFunctionType(
            asList(CoreTypes.STRING, formalTypeParameter),
            asList(formalTypeParameter)
        );
        // TODO: replace with metaclass that implements both Class and Function1
        ClassType javaScriptImporterType = new ClassType(fullyQualifiedName("shed", "javascript", "JavaScriptImporter"));
        ScalarTypeInfo javaScriptImporterTypeInfo = new ScalarTypeInfo(
            interfaces(CoreTypes.functionTypeOf(javaScriptImporterType), CoreTypes.classOf(javaScriptImporterType)),
            members("importValue", unassignableValue(importValueType))
        );
        
        context.addGlobal(fullyQualifiedName("shed", "javascript", "JavaScriptImporter"), CoreTypes.functionTypeOf(javaScriptImporterType));
        context.addInfo(javaScriptImporterType, javaScriptImporterTypeInfo);
        
        ClassType browserType = new ClassType(fullyQualifiedName("shed", "browser"));
        ScalarTypeInfo browserTypeInfo = new ScalarTypeInfo(
            interfaces(),
            members("alert", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)))
        );
        
        context.addGlobal(fullyQualifiedName("shed", "browser"), browserType);
        context.addInfo(browserType, browserTypeInfo);
        
        return context;
    }
}
