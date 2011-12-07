package org.zwobble.shed.compiler.typechecker;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;

public class TypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    canInferTypeOfBooleanLiteralsAsBoolean() {
        assertThat(inferType(new BooleanLiteralNode(true)), isType(CoreTypes.BOOLEAN));
        assertThat(inferType(new BooleanLiteralNode(false)), isType(CoreTypes.BOOLEAN));
    }
    
    @Test public void
    canInferTypeOfNumberLiteralsAsNumber() {
        assertThat(inferType(new NumberLiteralNode("2.2")), isType(CoreTypes.DOUBLE));
    }
    
    @Test public void
    canInferTypeOfStringLiteralsAsString() {
        assertThat(inferType(new StringLiteralNode("Everything's as if we never said")), isType(CoreTypes.STRING));
    }
    
    @Test public void
    canInferTypeOfUnitLiteralsAsUnit() {
        assertThat(inferType(Nodes.unit()), isType(CoreTypes.UNIT));
    }
    
    private TypeResult<Type> inferType(ExpressionNode expression) {
        return typeInferer().inferType(expression);
    }

    private TypeInferer typeInferer() {
        return fixture.get(TypeInferer.class);
    }
    
    private Matcher<TypeResult<Type>> isType(Type type) {
        return isSuccessWithValue(type);
    }
}
