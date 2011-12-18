package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.modules.Module;
import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.typechecker.errors.UnresolvedImportError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
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
    importingValuesFromGlobalStaticContextAssignsTypeToImportStatement() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        
        FullyQualifiedName dateTimeName = fullyQualifiedName("shed", "time", "DateTime");
        ClassType dateTime = new ClassType(dateTimeName);
        staticContext.addClass(globalDeclaration(dateTimeName), dateTime, ScalarTypeInfo.EMPTY);
        Type dateTimeMetaClass = metaClasses.metaClassOf(dateTime);
        staticContext.addGlobal(dateTimeName, dateTimeMetaClass);
        
        assertThat(typeCheckImportStatement(importStatement, Modules.build()), is(isSuccess()));
        assertThat(staticContext.getValueInfoFor(importStatement), is(some(unassignableValue(dateTimeMetaClass))));
    }
    
    @Test public void
    importingValuesFromModulesAssignsTypeToImportStatement() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        
        FullyQualifiedName dateTimeName = fullyQualifiedName("shed", "time", "DateTime");
        ClassType dateTime = new ClassType(dateTimeName);
        GlobalDeclaration declaration = globalDeclaration(dateTimeName);
        staticContext.addClass(declaration, dateTime, ScalarTypeInfo.EMPTY);
        Type dateTimeMetaClass = metaClasses.metaClassOf(dateTime);
        Modules modules = Modules.build(Module.create(dateTimeName, declaration));
        
        assertThat(typeCheckImportStatement(importStatement, modules), is(isSuccess()));
        assertThat(staticContext.getValueInfoFor(importStatement), is(some(unassignableValue(dateTimeMetaClass))));
    }

    @Test public void
    errorIfTryingToImportNonExistentGlobal() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        assertThat(
            typeCheckImportStatement(importStatement, Modules.build()),
            isFailureWithErrors(new UnresolvedImportError(asList("shed", "time", "DateTime")))
        );
    }

    @Test public void
    errorIfModuleIsUntyped() {
        ImportNode importStatement = new ImportNode(asList("shed", "time", "DateTime"));
        
        FullyQualifiedName dateTimeName = fullyQualifiedName("shed", "time", "DateTime");
        GlobalDeclaration declaration = globalDeclaration(dateTimeName);
        Modules modules = Modules.build(Module.create(dateTimeName, declaration));
        
        assertThat(
            typeCheckImportStatement(importStatement, modules),
            isFailureWithErrors(new UntypedReferenceError("shed.time.DateTime"))
        );
    }
    
    private TypeResult<Void> typeCheckImportStatement(ImportNode importStatement, Modules modules) {
        return new ImportStatementTypeChecker(staticContext, modules).typeCheck(importStatement);
    }
}
