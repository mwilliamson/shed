package org.zwobble.shed.compiler.typechecker;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.SimpleErrorDescription;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.errors.CannotReturnHereError;
import org.zwobble.shed.compiler.typechecker.errors.ConditionNotBooleanError;
import org.zwobble.shed.compiler.typechecker.errors.WrongReturnTypeError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.typechecker.VariableLookupResult.success;

public class TypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();
    private final FullyQualifiedNamesBuilder fullNames = new FullyQualifiedNamesBuilder();
    
    private StaticContext staticContext() {
        return StaticContext.defaultContext(references.build(), fullNames.build());
    }
    
    @Test public void
    noErrorsIfEverythingTypeChecks() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            asList((StatementNode)Nodes.immutableVar("x", new BooleanLiteralNode(true)))
        );
        assertThat(typeCheck(source).isSuccess(), is(true));
    }
    
    @Test public void
    sourceNodeMayHaveNoMoreThanOnePublicNode() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            Arrays.<StatementNode>asList(
                Nodes.publik(Nodes.immutableVar("x", new BooleanLiteralNode(true))),
                Nodes.publik(Nodes.immutableVar("y", new BooleanLiteralNode(true)))
            )
        );
        assertThat(errorStrings(typeCheck(source)), is(asList("A module may have no more than one public value")));
    }
    
    @Test public void
    expressionStatementIsTypeCheckedByInferringTypeOfExpression() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            asList((StatementNode)Nodes.expressionStatement(Nodes.call(Nodes.string(""))))
        );
        assertThat(
            errorStrings(typeCheck(source)),
            is(asList("Cannot call objects that aren't functions"))
        );
    }
    
    @Test public void
    bodyOfObjectIsTypeChecked() {
        VariableIdentifierNode stringReference = Nodes.id("String");
        references.addReference(stringReference, CoreModule.GLOBAL_DECLARATIONS.get("String"));
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode(
            "browser",
            Nodes.block(Nodes.immutableVar("version", stringReference, Nodes.number("1.2")))
        );
        TypeResult<?> result =
            TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext());
        assertThat(result, isFailureWithErrors(
            new SimpleErrorDescription("Cannot initialise variable of type \"String\" with expression of type \"Number\"")
        ));
    }
    
    @Test public void
    objectDeclarationDoesNotReturnFromScope() {
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode("browser", Nodes.block());
        TypeResult<StatementTypeCheckResult> result = 
            TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext());
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
    }
    
    @Test public void
    objectDeclarationBodyCannotReturn() {
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode(
            "browser",
            Nodes.block(Nodes.returnStatement(Nodes.number("42")))
        );
        TypeResult<StatementTypeCheckResult> result = 
            TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext());
        assertThat(result, isFailureWithErrors(new CannotReturnHereError()));
    }
    
    @Test public void
    objectDeclarationCreatesNewTypeWithPublicMembers() {
        ObjectDeclarationNode objectDeclarationNode = 
            new ObjectDeclarationNode("browser", Nodes.block(
                Nodes.immutableVar("version", Nodes.number("1.2")),
                Nodes.publik(Nodes.immutableVar("name", Nodes.string("firefox")))
            ));
        fullNames.addFullyQualifiedName(objectDeclarationNode, fullyQualifiedName("shed", "browser"));
        StaticContext staticContext = staticContext();
        TypeResult<StatementTypeCheckResult> result = 
            TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext);
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
        ScalarType browserType = (ScalarType)staticContext.get(objectDeclarationNode).getType();
        assertThat(browserType.getMembers(), is((Object)ImmutableMap.of("name", ValueInfo.unassignableValue(CoreTypes.STRING))));
        assertThat(browserType.getFullyQualifiedName(), is(fullyQualifiedName("shed", "browser")));
    }
    
    @Test public void
    conditionAndBothBranchesOfIfThenElseStatementAreTypeChecked() {
        IfThenElseStatementNode ifThenElseNode = 
            Nodes.ifThenElse(
                Nodes.id("isMorning"),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("eatCereal")))),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("eatPudding"))))
            );
        TypeResult<?> result = TypeChecker.typeCheckStatement(ifThenElseNode, nodeLocations, staticContext(), Option.<Type>none());
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
        TypeResult<?> result = TypeChecker.typeCheckStatement(ifThenElseNode, nodeLocations, staticContext(), Option.<Type>none());
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
            TypeChecker.typeCheckStatement(ifThenElseNode, nodeLocations, staticContext(), some(CoreTypes.STRING));
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
            TypeChecker.typeCheckStatement(ifThenElseNode, nodeLocations, staticContext(), some(CoreTypes.STRING));
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
            TypeChecker.typeCheckStatement(ifThenElseNode, nodeLocations, staticContext(), some(CoreTypes.STRING));
        assertThat(result, is(success(StatementTypeCheckResult.alwaysReturns())));
    }
    
    @Test public void
    whileLoopNeverReturnsFromFunction() {
        WhileStatementNode loop = Nodes.whileLoop(Nodes.bool(true), Nodes.block());
        TypeResult<StatementTypeCheckResult> result = 
            TypeChecker.typeCheckStatement(loop, nodeLocations, staticContext(), some(CoreTypes.STRING));
        assertThat(result, is(success(StatementTypeCheckResult.noReturn())));
    }
    
    @Test public void
    conditionOfWhileLoopMustBeBoolean() {
        WhileStatementNode loop = Nodes.whileLoop(Nodes.number("42"), Nodes.block());
        TypeResult<StatementTypeCheckResult> result = 
            TypeChecker.typeCheckStatement(loop, nodeLocations, staticContext(), some(CoreTypes.STRING));
        assertThat(result, isFailureWithErrors(new ConditionNotBooleanError(CoreTypes.NUMBER)));
    }
    
    @Test public void
    bodyOfWhileLoopIsTypeChecked() {
        WhileStatementNode loop = Nodes.whileLoop(Nodes.bool(true), Nodes.block(Nodes.returnStatement(Nodes.number("42"))));
        TypeResult<StatementTypeCheckResult> result = 
            TypeChecker.typeCheckStatement(loop, nodeLocations, staticContext(), some(CoreTypes.STRING));
        assertThat(result, isFailureWithErrors(new WrongReturnTypeError(CoreTypes.STRING, CoreTypes.NUMBER)));
    }
    
    @Test public void
    functionDeclarationAddsFunctionTypeToScope() {
        GlobalDeclarationNode numberDeclaration = new GlobalDeclarationNode("Number");
        VariableIdentifierNode numberReference = Nodes.id("Number");
        FunctionDeclarationNode functionDeclaration = new FunctionDeclarationNode(
            "now",
            Collections.<FormalArgumentNode>emptyList(),
            numberReference,
            Nodes.block(Nodes.returnStatement(Nodes.number("42")))
        );
        references.addReference(numberReference, numberDeclaration);
        StaticContext staticContext = staticContext();
        staticContext.add(numberDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.NUMBER)));
        TypeResult<StatementTypeCheckResult> result = 
            TypeChecker.typeCheckStatement(functionDeclaration, nodeLocations, staticContext, Option.<Type>none());
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
        assertThat(staticContext.get(functionDeclaration), is(success(unassignableValue(CoreTypes.functionTypeOf(CoreTypes.NUMBER)))));
    }
    
    private TypeResult<Void> typeCheck(SourceNode source) {
        return TypeChecker.typeCheck(source, nodeLocations, staticContext());
    }
}
