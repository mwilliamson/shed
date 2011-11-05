package org.zwobble.shed.compiler.typechecker;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.errors.NotCallableError;
import org.zwobble.shed.compiler.types.CoreTypes;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;

public class SourceTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
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
        assertThat(typeCheck(source), isFailureWithErrors(new NotCallableError(CoreTypes.STRING)));
    }

    private HasErrors typeCheck(SourceNode source) {
        return fixture.get(SourceTypeChecker.class).typeCheck(source);
    }
}
