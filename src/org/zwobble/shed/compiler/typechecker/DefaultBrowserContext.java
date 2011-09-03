package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;

public class DefaultBrowserContext {
    public static StaticContext defaultBrowserContext() {
        StaticContext context = StaticContext.defaultContext();
        
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type importValueType = new ParameterisedFunctionType(
            CoreTypes.functionTypeOf(CoreTypes.STRING, formalTypeParameter),
            asList(formalTypeParameter)
        );
        ClassType javaScriptImporterType = new ClassType(
            asList("shed", "javascript"),
            "JavaScriptImporter",
            Collections.<InterfaceType>emptySet(),
            ImmutableMap.of("importValue", importValueType)
        );
        context.addGlobal(asList("shed", "javascript", "JavaScriptImporter"), javaScriptImporterType);
        
        return context;
    }
}
