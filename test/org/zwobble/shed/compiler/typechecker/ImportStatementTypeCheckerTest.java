package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.ImportStatementTypeChecker.typeCheckImportStatement;

public class ImportStatementTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final StaticContext staticContext = new StaticContext();    
    
    @Test public void
    importingValuesAddsThemToCurrentScope() {
        Type dateTime = new ScalarType(asList("shed", "time"), "DateTime");
        staticContext.addGlobal(asList("shed", "time", "DateTime"), CoreTypes.classOf(dateTime));
        
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        
        assertThat(
            typeCheckImportStatement(importStatement, nodeLocations, staticContext),
            is(TypeResult.<Void>success(null))
        );
        assertThat(staticContext.get("DateTime"), is(some((Type)CoreTypes.classOf(dateTime))));
    }
    
    @Test public void
    errorIfTryingToImportNonExistentGlobal() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        assertThat(
            errorStrings(typeCheckImportStatement(importStatement, nodeLocations, staticContext)),
            is(asList("The import \"shed.time.DateTime\" cannot be resolved"))
        );
    }
    
    @Test public void
    errorIfImportingTwoValuesWithTheSameName() {
        Type dateTime = new ScalarType(asList("shed", "time"), "DateTime");
        staticContext.addGlobal(asList("shed", "time", "DateTime"), CoreTypes.classOf(dateTime));
        staticContext.add("DateTime", CoreTypes.classOf(dateTime));
        
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        assertThat(
            errorStrings(typeCheckImportStatement(importStatement, nodeLocations, staticContext)),
            is(asList("The variable \"DateTime\" has already been declared in this scope"))
        );
    }
}
