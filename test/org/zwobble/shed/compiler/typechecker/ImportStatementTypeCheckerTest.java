package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
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
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ImportStatementTypeCheckerTest {
    private final MetaClasses metaClasses = MetaClasses.create();
    private final StaticContext staticContext = new StaticContext(metaClasses);
    
    @Test public void
    importingValuesAssignsTypeToImportStatement() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        
        FullyQualifiedName dateTimeName = fullyQualifiedName("shed", "time", "DateTime");
        ClassType dateTime = new ClassType(dateTimeName);
        staticContext.addClass(globalDeclaration(dateTimeName), dateTime, ScalarTypeInfo.EMPTY);
        Type dateTimeMetaClass = metaClasses.metaClassOf(dateTime);
        staticContext.addGlobal(dateTimeName, dateTimeMetaClass);
        
        assertThat(typeCheckImportStatement(importStatement), is(isSuccess()));
        assertThat(staticContext.getValueInfoFor(importStatement), is(some(unassignableValue(dateTimeMetaClass))));
    }

    @Test public void
    errorIfTryingToImportNonExistentGlobal() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        assertThat(
            typeCheckImportStatement(importStatement),
            isFailureWithErrors(new UnresolvedImportError(asList("shed", "time", "DateTime")))
        );
    }
    
    private TypeResult<Void> typeCheckImportStatement(ImportNode importStatement) {
        return new ImportStatementTypeChecker(staticContext).typeCheck(importStatement);
    }
}
