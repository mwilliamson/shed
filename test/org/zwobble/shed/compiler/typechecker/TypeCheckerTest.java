package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.CompilerTesting;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.errors.ConditionNotBooleanError;
import org.zwobble.shed.compiler.typechecker.errors.WrongReturnTypeError;
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import com.google.inject.Injector;

import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.typechecker.VariableLookupResult.success;

public class TypeCheckerTest {
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    private StaticContext staticContext() {
        return StaticContext.defaultContext();
    }
    
    @Test public void
    conditionAndBothBranchesOfIfThenElseStatementAreTypeChecked() {
        IfThenElseStatementNode ifThenElseNode = 
            Nodes.ifThenElse(
                Nodes.id("isMorning"),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("eatCereal")))),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("eatPudding"))))
            );
        TypeResult<?> result = typeCheckStatement(ifThenElseNode, staticContext(), Option.<Type>none());
        assertThat(
            errorStrings(result),
            is((asList(
                "Could not determine type of reference: isMorning",
                "Could not determine type of reference: eatCereal",
                "Could not determine type of reference: eatPudding"
            )))
        );
    }
    
    @Test public void
    conditionOfIfThenElseStatementMustBeABoolean() {
        IfThenElseStatementNode ifThenElseNode = 
            Nodes.ifThenElse(
                Nodes.string("isMorning"),
                Nodes.block(),
                Nodes.block()
            );
        TypeResult<?> result = typeCheckStatement(ifThenElseNode, staticContext(), Option.<Type>none());
        assertThat(
            errorStrings(result),
            is((asList(
                "Condition must be of type Boolean, was of type String"
            )))
        );
    }
    
    @Test public void
    ifElseDoesNotReturnIfNeitherBranchReturns() {
        IfThenElseStatementNode ifThenElseNode = 
            Nodes.ifThenElse(
                Nodes.bool(true),
                Nodes.block(Nodes.expressionStatement(Nodes.string("eatCereal"))),
                Nodes.block(Nodes.expressionStatement(Nodes.string("eatPudding")))
            );
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(ifThenElseNode, staticContext(), some((Type)CoreTypes.STRING));
        assertThat(result, is(success(StatementTypeCheckResult.noReturn())));
    }
    
    @Test public void
    ifElseDoesNotReturnIfOnlyOneBranchReturns() {
        IfThenElseStatementNode ifThenElseNode = 
            Nodes.ifThenElse(
                Nodes.bool(true),
                Nodes.block(Nodes.returnStatement(Nodes.string("eatCereal"))),
                Nodes.block(Nodes.expressionStatement(Nodes.string("eatPudding")))
            );
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(ifThenElseNode, staticContext(), some((Type)CoreTypes.STRING));
        assertThat(result, is(success(StatementTypeCheckResult.noReturn())));
    }
    
    @Test public void
    ifElseDoesReturnsIfBothBranchesReturn() {
        IfThenElseStatementNode ifThenElseNode = 
            Nodes.ifThenElse(
                Nodes.bool(true),
                Nodes.block(Nodes.returnStatement(Nodes.string("eatCereal"))),
                Nodes.block(Nodes.returnStatement(Nodes.string("eatPudding")))
            );
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(ifThenElseNode, staticContext(), some((Type)CoreTypes.STRING));
        assertThat(result, is(success(StatementTypeCheckResult.alwaysReturns())));
    }
    
    @Test public void
    whileLoopNeverReturnsFromFunction() {
        WhileStatementNode loop = Nodes.whileLoop(Nodes.bool(true), Nodes.block());
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(loop, staticContext(), some((Type)CoreTypes.STRING));
        assertThat(result, is(success(StatementTypeCheckResult.noReturn())));
    }
    
    @Test public void
    conditionOfWhileLoopMustBeBoolean() {
        WhileStatementNode loop = Nodes.whileLoop(Nodes.number("42"), Nodes.block());
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(loop, staticContext(), some((Type)CoreTypes.STRING));
        assertThat(result, isFailureWithErrors(new ConditionNotBooleanError(CoreTypes.DOUBLE)));
    }
    
    @Test public void
    bodyOfWhileLoopIsTypeChecked() {
        WhileStatementNode loop = Nodes.whileLoop(Nodes.bool(true), Nodes.block(Nodes.returnStatement(Nodes.number("42"))));
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(loop, staticContext(), some((Type)CoreTypes.STRING));
        assertThat(result, isFailureWithErrors(new WrongReturnTypeError(CoreTypes.STRING, CoreTypes.DOUBLE)));
    }
    
    @Test public void
    functionDeclarationAddsFunctionTypeToScope() {
        GlobalDeclaration numberDeclaration = globalDeclaration("Double");
        VariableIdentifierNode numberReference = Nodes.id("Double");
        FunctionDeclarationNode functionDeclaration = new FunctionDeclarationNode(
            "now",
            Collections.<FormalArgumentNode>emptyList(),
            numberReference,
            Nodes.block(Nodes.returnStatement(Nodes.number("42")))
        );
        references.addReference(numberReference, numberDeclaration);
        StaticContext staticContext = staticContext();
        staticContext.add(numberDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.DOUBLE)));
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(functionDeclaration, staticContext, Option.<Type>none());
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
        assertThat(staticContext.get(functionDeclaration), is(success(unassignableValue(CoreTypes.functionTypeOf(CoreTypes.DOUBLE)))));
    }
    
    @Test public void
    functionDeclarationBodyIsTypeChecked() {
        GlobalDeclaration numberDeclaration = globalDeclaration("Double");
        VariableIdentifierNode numberReference = Nodes.id("Double");
        
        FunctionDeclarationNode functionDeclaration = new FunctionDeclarationNode(
            "now",
            Collections.<FormalArgumentNode>emptyList(),
            numberReference,
            Nodes.block(Nodes.returnStatement(Nodes.bool(true)))
        );
        references.addReference(numberReference, numberDeclaration);
        StaticContext staticContext = staticContext();
        staticContext.add(numberDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.DOUBLE)));
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(functionDeclaration, staticContext, Option.<Type>none());
        assertThat(result, CompilerTesting.isFailureWithErrors(new WrongReturnTypeError(CoreTypes.DOUBLE, CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    functionDeclarationCanCallItself() {
        GlobalDeclaration numberDeclaration = globalDeclaration("Double");
        VariableIdentifierNode numberReference = Nodes.id("Double");
        
        VariableIdentifierNode functionReference = Nodes.id("now");
        FunctionDeclarationNode functionDeclaration = new FunctionDeclarationNode(
            "now",
            Collections.<FormalArgumentNode>emptyList(),
            numberReference,
            Nodes.block(Nodes.returnStatement(Nodes.call(functionReference)))
        );
        references.addReference(numberReference, numberDeclaration);
        references.addReference(functionReference, functionDeclaration);
        StaticContext staticContext = staticContext();
        staticContext.add(numberDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.DOUBLE)));
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckStatement(functionDeclaration, staticContext, Option.<Type>none());
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
        assertThat(staticContext.get(functionDeclaration), is(success(unassignableValue(CoreTypes.functionTypeOf(CoreTypes.DOUBLE)))));
    }
    
    @Test public void
    functionDeclarationsCanBeMutuallyRecursive() {
        GlobalDeclaration numberDeclaration = globalDeclaration("Double");
        VariableIdentifierNode numberReference = Nodes.id("Double");
        references.addReference(numberReference, numberDeclaration);
        
        VariableIdentifierNode firstFunctionReference = Nodes.id("first");
        VariableIdentifierNode secondFunctionReference = Nodes.id("second");
        VariableIdentifierNode thirdFunctionReference = Nodes.id("third");
        
        FunctionDeclarationNode firstFunctionDeclaration = new FunctionDeclarationNode(
            "first", Collections.<FormalArgumentNode>emptyList(), numberReference,
            Nodes.block(Nodes.returnStatement(Nodes.call(secondFunctionReference)))
        );
        FunctionDeclarationNode secondFunctionDeclaration = new FunctionDeclarationNode(
            "second", Collections.<FormalArgumentNode>emptyList(), numberReference,
            Nodes.block(Nodes.returnStatement(Nodes.call(thirdFunctionReference)))
        );
        FunctionDeclarationNode thirdFunctionDeclaration = new FunctionDeclarationNode(
            "third", Collections.<FormalArgumentNode>emptyList(), numberReference,
            Nodes.block(Nodes.returnStatement(Nodes.call(firstFunctionReference)))
        );
        references.addReference(firstFunctionReference, firstFunctionDeclaration);
        references.addReference(secondFunctionReference, secondFunctionDeclaration);
        references.addReference(thirdFunctionReference, thirdFunctionDeclaration);
        
        StaticContext staticContext = staticContext();
        staticContext.add(numberDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.DOUBLE)));
        BlockNode block = Nodes.block(firstFunctionDeclaration, secondFunctionDeclaration, thirdFunctionDeclaration);
        TypeResult<StatementTypeCheckResult> result = typeCheckBlock(block, staticContext, Option.<Type>none());
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
        assertThat(staticContext.get(firstFunctionDeclaration), is(success(unassignableValue(CoreTypes.functionTypeOf(CoreTypes.DOUBLE)))));
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheckBlock(
        BlockNode block, StaticContext context, Option<Type> returnType
    ) {
        Injector injector = TypeCheckerInjector.build(new FullyQualifiedNamesBuilder().build(), context, references.build());
        BlockTypeChecker typeChecker = injector.getInstance(BlockTypeChecker.class);
        return typeChecker.forwardDeclareAndTypeCheck(block, returnType);
    }

    private TypeResult<StatementTypeCheckResult> typeCheckStatement(StatementNode statement, StaticContext context, Option<Type> returnType) {
        return typeCheckBlock(Nodes.block(statement), context, returnType);
    }
}
