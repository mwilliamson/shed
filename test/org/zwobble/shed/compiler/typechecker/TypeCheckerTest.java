package org.zwobble.shed.compiler.typechecker;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;

public class TypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final StaticContext staticContext = new StaticContext();
    
    @Test public void
    noErrorsIfEverythingTypeChecks() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            asList((StatementNode)new ImmutableVariableNode("x", none(ExpressionNode.class), new BooleanLiteralNode(true)))
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
    newScopeIsCreatedBySource() {
        Type customString = new ClassType(asList("shed", "custom"), "String", Collections.<InterfaceType>emptySet(), ImmutableMap.<String, Type>of());
        staticContext.addGlobal(asList("shed", "custom", "String"), CoreTypes.classOf(customString));
        
        staticContext.add("String", CoreTypes.STRING);
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            asList(new ImportNode(asList("shed", "custom", "String"))),
            asList((StatementNode)new ImmutableVariableNode(
                "x",
                none(ExpressionNode.class),
                new BooleanLiteralNode(true)
            ))
        );
        assertThat(
            typeCheck(source),
            is(TypeResult.<Void>success(null))
        );
    }
    
    @Test public void
    sourceScopeIsClosed() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            asList((StatementNode)new ImmutableVariableNode("x", none(ExpressionNode.class), new BooleanLiteralNode(true)))
        );
        typeCheck(source);
        assertThat(staticContext.isDeclaredInCurrentScope("x"), is(false));
    }
    
    @Test public void
    lambdaExpressionDefinesANewScope() {
        staticContext.add("String", CoreTypes.classOf(CoreTypes.STRING));
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            Arrays.<StatementNode>asList(
                new ImmutableVariableNode("x", none(ExpressionNode.class), new BooleanLiteralNode(true)),
                new ImmutableVariableNode(
                    "func",
                    none(ExpressionNode.class),
                    new LongLambdaExpressionNode(
                        Collections.<FormalArgumentNode>emptyList(),
                        new VariableIdentifierNode("String"),
                        Arrays.<StatementNode>asList(
                            new ImmutableVariableNode("x", none(ExpressionNode.class), new BooleanLiteralNode(true)),
                            new ReturnNode(new StringLiteralNode("Stop!"))
                        )
                    )
                )
            )
        );
        assertThat(
            typeCheck(source),
            is(TypeResult.<Void>success(null))
        );
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
    declaringObjectAddsItToScope() {
        ObjectDeclarationNode objectDeclarationNode = 
            new ObjectDeclarationNode("browser", asList((StatementNode)Nodes.immutableVar("version", Nodes.number("1.2"))));
        TypeResult<Void> result = TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext);
        assertThat(result, is(TypeResult.<Void>success(null)));
        assertThat(staticContext.isDeclaredInCurrentScope("browser"), is(true));
    }
    
    @Test public void
    bodyOfObjectIsTypeChecked() {
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode(
            "browser",
            asList((StatementNode)Nodes.immutableVar("version", Nodes.id("String"), Nodes.number("1.2")))
        );
        TypeResult<Void> result = TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext);
        assertThat(result.isSuccess(), is(false));
    }
    
    @Test public void
    bodyOfObjectIsInSeparateScope() {
        ObjectDeclarationNode objectDeclarationNode = 
            new ObjectDeclarationNode("browser", asList((StatementNode)Nodes.immutableVar("browser", Nodes.number("1.2"))));
        TypeResult<Void> result = TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext);
        assertThat(result, is(TypeResult.<Void>success(null)));
    }
    
    @Test public void
    objectDeclarationCreatesNewTypeWithPublicMembers() {
        ObjectDeclarationNode objectDeclarationNode = 
            new ObjectDeclarationNode("browser", Arrays.<StatementNode>asList(
                Nodes.immutableVar("version", Nodes.number("1.2")),
                Nodes.publik(Nodes.immutableVar("name", Nodes.string("firefox")))
            ));
        TypeResult<Void> result = TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext);
        assertThat(result, is(TypeResult.<Void>success(null)));
        ScalarType browserType = (ScalarType)staticContext.get("browser").getType();
        assertThat(browserType.getMembers(), is((Object)ImmutableMap.of("name", CoreTypes.STRING)));
    }
    
    @Test public void
    cannotAccessVariableFromSuperScopeIfCurrentScopeDefinesVariableWithSameNameLater() {
        ObjectDeclarationNode objectDeclarationNode = 
            Nodes.object("person", Arrays.<StatementNode>asList(
                Nodes.immutableVar("name", Nodes.string("Bob")),
                Nodes.object("parent", Arrays.<StatementNode>asList(
                    Nodes.immutableVar("identifier", Nodes.id("name")),
                    Nodes.immutableVar("name", Nodes.string("Jim"))
                ))
            ));
        TypeResult<Void> result = TypeChecker.typeCheckObjectDeclaration(objectDeclarationNode, nodeLocations, staticContext);
        assertThat(errorStrings(result), is((asList("Cannot access variable \"name\" before it is declared"))));
    }
    
    @Test public void
    conditionAndBothBranchesOfIfThenElseStatementAreTypeChecked() {
        IfThenElseStatementNode ifThenElseNode = 
            Nodes.ifThenElse(
                Nodes.id("isMorning"),
                Arrays.<StatementNode>asList(Nodes.expressionStatement(Nodes.call(Nodes.id("eatCereal")))),
                Arrays.<StatementNode>asList(Nodes.expressionStatement(Nodes.call(Nodes.id("eatPudding"))))
            );
        TypeResult<Void> result = TypeChecker.typeCheckStatement(ifThenElseNode, nodeLocations, staticContext);
        assertThat(
            errorStrings(result),
            is((asList(
                "No variable \"isMorning\" in scope",
                "No variable \"eatCereal\" in scope",
                "No variable \"eatPudding\" in scope"
            )))
        );
    }
    
    private TypeResult<Void> typeCheck(SourceNode source) {
        return TypeChecker.typeCheck(source, nodeLocations, staticContext);
    }
}
