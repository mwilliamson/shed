package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.errors.UnresolvedImportError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.typechecker.ImportStatementTypeChecker.typeCheckImportStatement;

public class ImportStatementTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    @Test public void
    importingValuesAssignsTypeToImportStatement() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        VariableIdentifierNode reference = new VariableIdentifierNode("DateTime");
        references.addReference(reference, importStatement);
        StaticContext staticContext = new StaticContext(references.build());
        
        Type dateTime = new ClassType(asList("shed", "time"), "DateTime", Collections.<InterfaceType>emptySet(), ImmutableMap.<String, Type>of());
        staticContext.addGlobal(asList("shed", "time", "DateTime"), CoreTypes.classOf(dateTime));
        
        assertThat(
            typeCheckImportStatement(importStatement, nodeLocations, staticContext),
            is(TypeResult.<Void>success(null))
        );
        assertThat(staticContext.get(reference), is(VariableLookupResult.success(unassignableValue(CoreTypes.classOf(dateTime)))));
    }
    
    @Test public void
    errorIfTryingToImportNonExistentGlobal() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        assertThat(
            typeCheckImportStatement(importStatement, nodeLocations, new StaticContext(references.build())),
            isFailureWithErrors(new UnresolvedImportError(asList("shed", "time", "DateTime")))
        );
    }
}
