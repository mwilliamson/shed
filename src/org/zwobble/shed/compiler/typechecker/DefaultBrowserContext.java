package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.types.FormalTypeParameter.invariantFormalTypeParameter;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class DefaultBrowserContext {
    public static StaticContext defaultBrowserContext() {
        StaticContext context = DefaultContext.defaultContext();
        
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        Type importValueType = new ParameterisedFunctionType(
            asList(CoreTypes.STRING, formalTypeParameter),
            asList(formalTypeParameter)
        );
        FullyQualifiedName javaScriptImporterName = fullyQualifiedName("shed", "javascript", "JavaScriptImporter");
        ClassType javaScriptImporterType = new ClassType(javaScriptImporterName);
        ScalarTypeInfo javaScriptImporterTypeInfo = new ScalarTypeInfo(
            interfaces(),
            members("importValue", unassignableValue(importValueType))
        );
        
        context.addClass(globalDeclaration(javaScriptImporterName), javaScriptImporterType, Collections.<Type>emptyList(), javaScriptImporterTypeInfo);
        context.addGlobal(javaScriptImporterName, context.getMetaClass(javaScriptImporterType));
        
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
