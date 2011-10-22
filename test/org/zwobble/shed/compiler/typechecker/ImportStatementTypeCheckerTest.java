package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.typechecker.errors.UnresolvedImportError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ImportStatementTypeCheckerTest {
    @Test public void
    importingValuesAssignsTypeToImportStatement() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        StaticContext staticContext = new StaticContext();
        
        FullyQualifiedName dateTimeName = fullyQualifiedName("shed", "time", "DateTime");
        ClassType dateTime = new ClassType(dateTimeName);
        staticContext.addClass(globalDeclaration(dateTimeName), dateTime, ScalarTypeInfo.EMPTY);
        Type dateTimeMetaClass = staticContext.getMetaClass(dateTime);
        staticContext.addGlobal(dateTimeName, dateTimeMetaClass);
        
        assertThat(typeCheckImportStatement(importStatement, staticContext), is(isSuccess()));
        assertThat(staticContext.get(importStatement), is(VariableLookupResult.success(unassignableValue(dateTimeMetaClass))));
    }

    @Test public void
    errorIfTryingToImportNonExistentGlobal() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        assertThat(
            typeCheckImportStatement(importStatement, new StaticContext()),
            isFailureWithErrors(new UnresolvedImportError(asList("shed", "time", "DateTime")))
        );
    }
    
    private TypeResult<Void> typeCheckImportStatement(ImportNode importStatement, StaticContext staticContext) {
        return new ImportStatementTypeChecker(staticContext).typeCheck(importStatement);
    }
}
