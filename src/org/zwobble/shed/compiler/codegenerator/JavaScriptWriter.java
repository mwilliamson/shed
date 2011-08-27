package org.zwobble.shed.compiler.codegenerator;

import java.util.Map;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptBooleanLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionStatement;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptFunctionCallNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptFunctionNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptIdentifierNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNumberLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptObjectLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptReturnNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStringLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptVariableDeclarationNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import static com.google.common.collect.Iterables.transform;

public class JavaScriptWriter {
    public String write(JavaScriptNode node) {
        StringBuilder builder = new StringBuilder();
        write(node, builder, 0);
        return builder.toString();
    }
    
    public void write(JavaScriptNode node, StringBuilder builder, int indentationLevel) {
        if (node instanceof JavaScriptBooleanLiteralNode) {
            builder.append(((JavaScriptBooleanLiteralNode) node).getValue() ? "true" : "false");
            return;
        }
        if (node instanceof JavaScriptNumberLiteralNode) {
            builder.append(((JavaScriptNumberLiteralNode) node).getValue());
            return;
        }
        if (node instanceof JavaScriptStringLiteralNode) {
            writeJavaScriptString(((JavaScriptStringLiteralNode) node).getValue(), builder);
            return;
        }
        if (node instanceof JavaScriptIdentifierNode) {
            builder.append(((JavaScriptIdentifierNode) node).getValue());
            return;
        }
        if (node instanceof JavaScriptFunctionCallNode) {
            JavaScriptFunctionCallNode call = (JavaScriptFunctionCallNode) node;
            boolean functionRequiresParentheses = call.getFunction() instanceof JavaScriptFunctionNode;
            if (functionRequiresParentheses) {
                builder.append("(");
            }
            write(call.getFunction(), builder, indentationLevel);
            if (functionRequiresParentheses) {
                builder.append(")");
            }
            builder.append("(");
            Joiner.on(", ").appendTo(builder, transform(call.getArguments(), stringify(indentationLevel)));
            builder.append(")");
            return;
        }
        if (node instanceof JavaScriptReturnNode) {
            builder.append(indentationAtLevel(indentationLevel));
            builder.append("return ");
            write(((JavaScriptReturnNode) node).getValue(), builder, indentationLevel);
            builder.append(";");
            return;
        }
        if (node instanceof JavaScriptVariableDeclarationNode) {
            JavaScriptVariableDeclarationNode declaration = (JavaScriptVariableDeclarationNode)node;
            builder.append(indentationAtLevel(indentationLevel));
            builder.append("var ");
            builder.append(declaration.getName());
            builder.append(" = ");
            write(declaration.getInitialValue(), builder, indentationLevel);
            builder.append(";");
            return;
        }
        if (node instanceof JavaScriptExpressionStatement) {
            builder.append(indentationAtLevel(indentationLevel));
            write(((JavaScriptExpressionStatement) node).getExpression(), builder, indentationLevel);
            builder.append(";");
            return;
        }
        if (node instanceof JavaScriptStatements) {
            Joiner.on("\n").appendTo(builder, transform(((JavaScriptStatements) node).getStatements(), stringify(indentationLevel)));
            return;
        }
        if (node instanceof JavaScriptFunctionNode) {
            JavaScriptFunctionNode function = (JavaScriptFunctionNode) node;
            
            builder.append("function(");
            Joiner.on(", ").appendTo(builder, function.getArguments());
            builder.append(") {\n");
            if (function.getStatements().size() == 0) {
                builder.append("}");
            } else {
                Joiner.on("\n").appendTo(builder, transform(function.getStatements(), stringify(indentationLevel + 1)));
                builder.append("\n");
                builder.append(indentationAtLevel(indentationLevel));
                builder.append("}");
            }
            return;   
        }
        if (node instanceof JavaScriptObjectLiteralNode) {
            builder.append("{");
            Map<String, JavaScriptExpressionNode> properties = ((JavaScriptObjectLiteralNode) node).getProperties();
            if (properties.size() == 0) {
                builder.append("}");
            } else {
                for (Map.Entry<String, JavaScriptExpressionNode> property : properties.entrySet()) {
                    builder.append("\n");
                    builder.append(indentationAtLevel(indentationLevel + 1));
                    writeJavaScriptString(property.getKey(), builder);
                    builder.append(": ");
                    write(property.getValue(), builder, indentationLevel + 1);
                }
                builder.append("\n");
                builder.append(indentationAtLevel(indentationLevel));
                builder.append("}");
            }
            return;
        }
        throw new RuntimeException("Don't know how to write JavaScript node: " + node);
    }

    private void writeJavaScriptString(String value, StringBuilder builder) {
        builder.append("\"");

        for (char c : Lists.charactersOf(value)) {
            builder.append(escapedCharacter(c));
        }
        
        builder.append("\"");
    }

    private String indentationAtLevel(int indentationLevel) {
        return Strings.repeat(" ", indentationLevel * 4);
    }

    private Function<JavaScriptNode, String> stringify(final int indentationLevel) {
        return new Function<JavaScriptNode, String>() {
            @Override
            public String apply(JavaScriptNode input) {
                StringBuilder builder = new StringBuilder();
                write(input, builder, indentationLevel);
                return builder.toString();
            }
        };
    }

    private String escapedCharacter(char c) {
        if (c == '\\') {
            return "\\\\";
        }
        if (c == '"') {
            return "\\\"";
        }
        if (c == '\n') {
            return "\\n";
        }
        if (c == '\t') {
            return "\\t";
        }
        if (c == '\r') {
            return "\\r";
        }
        if (c == '\b') {
            return "\\b";
        }
        if (c == '\f') {
            return "\\f";
        }
        return Character.toString(c);
    }
}
