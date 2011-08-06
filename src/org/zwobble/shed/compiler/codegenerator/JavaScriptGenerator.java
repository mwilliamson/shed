package org.zwobble.shed.compiler.codegenerator;

import java.util.List;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;

import com.google.common.base.Function;

import static com.google.common.collect.Lists.transform;
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
        if (node instanceof StringLiteralNode) {
            return js.call(js.id("SHED.shed.lang.String"), js.string(((StringLiteralNode)node).getValue()));
        }
        if (node instanceof VariableDeclarationNode) {
            VariableDeclarationNode immutableVariable = (VariableDeclarationNode)node;
            return js.var(immutableVariable.getIdentifier(), generate(immutableVariable.getValue()));
        }
        if (node instanceof ShortLambdaExpressionNode) {
            ShortLambdaExpressionNode lambda = (ShortLambdaExpressionNode)node;
            List<String> argumentNames = transform(lambda.getFormalArguments(), toFormalArgumentName());
            List<JavaScriptNode> javaScriptBody = asList(generate(lambda.getBody()));
            return js.func(argumentNames, javaScriptBody);
        }
        if (node instanceof LongLambdaExpressionNode) {
            LongLambdaExpressionNode lambda = (LongLambdaExpressionNode)node;
            List<JavaScriptNode> javaScriptBody = transform(lambda.getBody(), toJavaScriptStatement());
            List<String> argumentNames = transform(lambda.getFormalArguments(), toFormalArgumentName());
            return js.func(argumentNames, javaScriptBody);
        }
        if (node instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode)node;
            return js.ret(generate(returnNode.getExpression()));
        }
        return null;
    }

    private Function<FormalArgumentNode, String> toFormalArgumentName() {
        return new Function<FormalArgumentNode, String>() {
            @Override
            public String apply(FormalArgumentNode input) {
                return input.getName();
            }
        };
    }

    private Function<StatementNode, JavaScriptNode> toJavaScriptStatement() {
        return new Function<StatementNode, JavaScriptNode>() {
            @Override
            public JavaScriptNode apply(StatementNode input) {
                return generate(input);
            }
        };
    }
}
