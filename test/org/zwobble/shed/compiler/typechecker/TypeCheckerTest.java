package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;

public class TypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final TypeLookup typeLookup = new TypeLookup(nodeLocations);
    private final TypeInferer typeInferer = new TypeInferer(nodeLocations, typeLookup);
    private final TypeChecker typeChecker = new TypeChecker(nodeLocations, typeLookup, typeInferer);
    
    private final StaticContext staticContext = new StaticContext();
    
    @Test public void
    noErrorsIfEverythingTypeChecks() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            new PublicDeclarationNode(asList("x")),
            asList((StatementNode)new ImmutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true)))
        );
        assertThat(typeChecker.typeCheck(source, staticContext).isSuccess(), is(true));
    }
    
    @Test public void
    errorsIfAttemptingToInitialiseAVariableWithExpressionOfWrongType() {
        StatementNode variableNode = new ImmutableVariableNode(
            "x",
            some((TypeReferenceNode)new TypeIdentifierNode("String")),
            new BooleanLiteralNode(true)
        );
        nodeLocations.put(variableNode, range(position(4, 12), position(6, 6)));
        
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            new PublicDeclarationNode(asList("x")),
            asList(variableNode)
        );
        staticContext.add("String", CoreTypes.classOf(CoreTypes.STRING));
        assertThat(
            typeChecker.typeCheck(source, staticContext),
            is(TypeResult.<Void>failure(asList(new CompilerError(
                range(position(4, 12), position(6, 6)),
                "Cannot initialise variable of type \"String\" with expression of type \"Boolean\""
            ))))
        );
    }
    
    @Test public void
    canImportValues() {
        Type dateTime = new ScalarType(asList("shed", "time"), "DateTime");
        staticContext.addGlobal(asList("shed", "time", "DateTime"), CoreTypes.classOf(dateTime));
        
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            asList(new ImportNode(asList("shed", "time", "DateTime"))),
            new PublicDeclarationNode(asList("x")),
            asList((StatementNode)new ImmutableVariableNode(
                "x",
                none(TypeReferenceNode.class),
                new ShortLambdaExpressionNode(
                    asList(new FormalArgumentNode("time", new TypeIdentifierNode("DateTime"))),
                    none(TypeReferenceNode.class),
                    new BooleanLiteralNode(true)
                )
            ))
        );
        assertThat(
            typeChecker.typeCheck(source, staticContext),
            is(TypeResult.<Void>success(null))
        );
    }
    
    @Test public void
    errorIfTryingToImportNonExistentGlobal() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            asList(
                new ImportNode(asList("shed", "time", "DateTime"))
            ),
            new PublicDeclarationNode(asList("x")),
            asList((StatementNode)new ImmutableVariableNode(
                "x",
                none(TypeReferenceNode.class),
                new BooleanLiteralNode(true)
            ))
        );
        assertThat(
            errorStrings(typeChecker.typeCheck(source, staticContext)),
            is(asList("The import \"shed.time.DateTime\" cannot be resolved"))
        );
    }
    
    @Test public void
    errorIfImportingTwoValuesWithTheSameName() {
        Type dateTime = new ScalarType(asList("shed", "time"), "DateTime");
        staticContext.addGlobal(asList("shed", "time", "DateTime"), CoreTypes.classOf(dateTime));
        
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            asList(
                new ImportNode(asList("shed", "time", "DateTime")),
                new ImportNode(asList("shed", "time", "DateTime"))
            ),
            new PublicDeclarationNode(asList("x")),
            asList((StatementNode)new ImmutableVariableNode(
                "x",
                none(TypeReferenceNode.class),
                new BooleanLiteralNode(true)
            ))
        );
        assertThat(
            errorStrings(typeChecker.typeCheck(source, staticContext)),
            is(asList("The variable \"DateTime\" has already been declared in this scope"))
        );
    }
    
    @Test public void
    canOverrideCoreTypeWithImport() {
        Type customString = new ScalarType(asList("shed", "custom"), "String");
        staticContext.addGlobal(asList("shed", "custom", "String"), CoreTypes.classOf(customString));
        
        staticContext.add("String", CoreTypes.STRING);
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            asList(new ImportNode(asList("shed", "custom", "String"))),
            new PublicDeclarationNode(asList("x")),
            asList((StatementNode)new ImmutableVariableNode(
                "x",
                none(TypeReferenceNode.class),
                new BooleanLiteralNode(true)
            ))
        );
        assertThat(
            typeChecker.typeCheck(source, staticContext),
            is(TypeResult.<Void>success(null))
        );
    }
}
