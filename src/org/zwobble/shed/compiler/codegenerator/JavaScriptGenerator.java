package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;

import static java.util.Arrays.asList;

public class JavaScriptGenerator {
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    public JavaScriptNode generate(SyntaxNode node) {
        if (node instanceof BooleanLiteralNode) {
            return js.call(js.id("SHED.shed.lang.Boolean"), js.bool(((BooleanLiteralNode)node).getValue()));
        }
        if (node instanceof NumberLiteralNode) {
            return js.call(js.id("SHED.shed.lang.Number"), js.number(((NumberLiteralNode)node).getValue()));
        }
        if (node instanceof VariableDeclarationNode) {
            VariableDeclarationNode immutableVariable = (VariableDeclarationNode)node;
            return js.var(immutableVariable.getIdentifier(), generate(immutableVariable.getValue()));
        }
        if (node instanceof ShortLambdaExpressionNode) {
            ShortLambdaExpressionNode lambda = (ShortLambdaExpressionNode)node;
            return js.func(asList(generate(lambda.getBody())));
        }
        return null;
    }
}
