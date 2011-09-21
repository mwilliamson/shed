package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ReturnStatementTypeChecker.typeCheckReturnStatement;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ReturnStatementTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    @Test public void
    cannotReturnIfReturnTypeIsNone() {
        ReturnNode returnStatement = new ReturnNode(new BooleanLiteralNode(true));
        assertThat(
            errorStrings(typeCheck(returnStatement, staticContext(), Option.<Type>none())),
            is(asList("Cannot return from this scope"))
        );
    }
    
    @Test public void
    returnExpressionIsTypeChecked() {
        ReturnNode returnStatement = new ReturnNode(new VariableIdentifierNode("x"));
        assertThat(
            errorStrings(typeCheck(returnStatement, staticContext(), Option.some(CoreTypes.STRING))),
            is(asList("Could not determine type of reference: x"))
        );
    }
    
    @Test public void
    returnExpressionCanBeSubTypeOfReturnType() {
        InterfaceType iterableType = new InterfaceType(fullyQualifiedName("shed", "util", "Iterable"), ImmutableMap.<String, ValueInfo>of());
        ClassType listType = new ClassType(fullyQualifiedName("shed", "util", "List"), newHashSet(iterableType), ImmutableMap.<String, ValueInfo>of());
        
        VariableIdentifierNode reference = new VariableIdentifierNode("x");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("x");
        references.addReference(reference, declaration);
        StaticContext context = staticContext();
        context.add(declaration, unassignableValue(listType));
        
        ReturnNode returnStatement = new ReturnNode(reference);
        assertThat(
            typeCheck(returnStatement, context, Option.<Type>some(iterableType)),
            is(TypeResult.success(StatementTypeCheckResult.alwaysReturns()))
        );
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheck(ReturnNode returnNode, StaticContext context, Option<Type> returnType) {
        return typeCheckReturnStatement(returnNode, nodeLocations, context, returnType);
    }
    
    private StaticContext staticContext() {
        return StaticContext.defaultContext(references.build(), new FullyQualifiedNamesBuilder().build());
    }
}
