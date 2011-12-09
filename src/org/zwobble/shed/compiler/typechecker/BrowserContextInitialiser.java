package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import javax.inject.Inject;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
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
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;

public class BrowserContextInitialiser implements StaticContextInitialiser {
    private final DefaultContextInitialiser defaultInitialiser;

    @Inject
    public BrowserContextInitialiser(DefaultContextInitialiser defaultInitialiser) {
        this.defaultInitialiser = defaultInitialiser;
    }

    @Override
    public void initialise(StaticContext context, BuiltIns builtIns, MetaClasses metaClasses) {
        defaultInitialiser.initialise(context, builtIns, metaClasses);
        
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        Type importValueType = new ParameterisedFunctionType(
            asList(CoreTypes.STRING, formalTypeParameter),
            formalTypeParameters(formalTypeParameter)
        );
        FullyQualifiedName javaScriptImporterName = fullyQualifiedName("shed", "javascript", "JavaScriptImporter");
        ClassType javaScriptImporterType = new ClassType(javaScriptImporterName);
        ScalarTypeInfo javaScriptImporterTypeInfo = new ScalarTypeInfo(
            interfaces(),
            members("importValue", unassignableValue(importValueType))
        );
        
        context.addClass(globalDeclaration(javaScriptImporterName), javaScriptImporterType, Collections.<Type>emptyList(), javaScriptImporterTypeInfo);
        context.addGlobal(javaScriptImporterName, metaClasses.metaClassOf(javaScriptImporterType));
        
        ClassType browserType = new ClassType(fullyQualifiedName("shed", "browser"));
        ScalarTypeInfo browserTypeInfo = new ScalarTypeInfo(
            interfaces(),
            members("alert", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)))
        );
        
        context.addGlobal(fullyQualifiedName("shed", "browser"), browserType);
        context.addInfo(browserType, browserTypeInfo);
    }


}
