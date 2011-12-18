package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.errors.HasErrors;
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
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;

public class SourceTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    noErrorsIfEverythingTypeChecks() {
        SourceNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            asList((StatementNode)Nodes.immutableVar("x", new BooleanLiteralNode(true)))
        );
        assertThat(typeCheck(source), isSuccess());
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
