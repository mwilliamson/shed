package org.zwobble.shed.compiler.nodejs;

import java.util.Collections;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.typechecker.BuiltIns;
import org.zwobble.shed.compiler.typechecker.DefaultContextInitialiser;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.StaticContextInitialiser;
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
import static org.zwobble.shed.compiler.types.FormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class NodeJsContextInitialiser implements StaticContextInitialiser {
    private final DefaultContextInitialiser defaultContextInitialiser;

    public NodeJsContextInitialiser(DefaultContextInitialiser defaultContextInitialiser) {
        this.defaultContextInitialiser = defaultContextInitialiser;
    }
    
    @Override
    public void initialise(StaticContext context, BuiltIns builtIns, MetaClasses metaClasses) {
        defaultContextInitialiser.initialise(context, builtIns, metaClasses);
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
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
        context.addGlobal(importerName, metaClasses.metaClassOf(importerType));
    }
}
