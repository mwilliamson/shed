package org.zwobble.shed.compiler.typechecker.statements;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerInjector;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.errors.CannotReturnHereError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.inject.Injector;

import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class ReturnStatementTypeCheckerTest {
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    @Test public void
    cannotReturnIfReturnTypeIsNone() {
        ReturnNode returnStatement = new ReturnNode(new BooleanLiteralNode(true));
        assertThat(
            typeCheck(returnStatement, staticContext(), Option.<Type>none()),
            isFailureWithErrors(new CannotReturnHereError())
        );
    }
    
    @Test public void
    returnExpressionIsTypeChecked() {
        ReturnNode returnStatement = new ReturnNode(new VariableIdentifierNode("x"));
        assertThat(
            errorStrings(typeCheck(returnStatement, staticContext(), Option.some((Type)CoreTypes.STRING))),
            is(asList("Could not determine type of reference: x"))
        );
    }
    
    @Test public void
    returnExpressionCanBeSubTypeOfReturnType() {
        InterfaceType iterableType = new InterfaceType(fullyQualifiedName("shed", "util", "Iterable"));
        ClassType listType = new ClassType(fullyQualifiedName("shed", "util", "List"));
        ScalarTypeInfo listTypeInfo = new ScalarTypeInfo(interfaces(iterableType), members());
        
        VariableIdentifierNode reference = new VariableIdentifierNode("x");
        GlobalDeclaration declaration = globalDeclaration("x");
        references.addReference(reference, declaration);
        StaticContext context = staticContext();
        context.add(declaration, unassignableValue(listType));
        context.addInfo(listType, listTypeInfo);
        
        ReturnNode returnStatement = new ReturnNode(reference);
        assertThat(
            typeCheck(returnStatement, context, Option.<Type>some(iterableType)),
            is(TypeResult.success(StatementTypeCheckResult.alwaysReturns()))
        );
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheck(ReturnNode returnNode, StaticContext context, Option<Type> returnType) {
        Injector injector = TypeCheckerInjector.build(new FullyQualifiedNamesBuilder().build(), context, references.build());
        ReturnStatementTypeChecker typeChecker = injector.getInstance(ReturnStatementTypeChecker.class);
        return typeChecker.typeCheck(returnNode, returnType);
    }
    
    private StaticContext staticContext() {
        return StaticContext.defaultContext();
    }
}
