package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.types.CoreTypes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;

public class TypeInfererTest {
    @Test public void
    canInferTypeOfBooleanLiteralsAsBoolean() {
        assertThat(inferType(new BooleanLiteralNode(true)), is(CoreTypes.BOOLEAN));
        assertThat(inferType(new BooleanLiteralNode(false)), is(CoreTypes.BOOLEAN));
    }
}
