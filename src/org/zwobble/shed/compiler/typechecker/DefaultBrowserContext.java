package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class DefaultBrowserContext {
    public static StaticContext defaultBrowserContext(References references) {
        StaticContext context = StaticContext.defaultContext(references);
        
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type importValueType = new ParameterisedFunctionType(
            CoreTypes.functionTypeOf(CoreTypes.STRING, formalTypeParameter),
            asList(formalTypeParameter)
        );
        // TODO: replace with metaclass that implements both Class and Function1
        ClassType javaScriptImporterType = new ClassType(
            fullyQualifiedName("shed", "javascript", "JavaScriptImporter"),
            Collections.<InterfaceType>emptySet(),
            ImmutableMap.of("importValue", unassignableValue(importValueType))
        );
        
        context.addGlobal(asList("shed", "javascript", "JavaScriptImporter"), CoreTypes.functionTypeOf(javaScriptImporterType));
        
        ClassType browserType = new ClassType(
            fullyQualifiedName("shed", "browser"),
            Collections.<InterfaceType>emptySet(),
            ImmutableMap.of("alert", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)))
        );
        
        context.addGlobal(asList("shed", "browser"), browserType);
        
        return context;
    }
}
