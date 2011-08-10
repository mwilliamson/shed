package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptBooleanLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptFunctionCallNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptIdentifierNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNumberLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptReturnNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStringLiteralNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import static com.google.common.collect.Iterables.transform;

public class JavaScriptWriter {
    public String write(JavaScriptNode node) {
        StringBuilder builder = new StringBuilder();
        write(node, builder);
        return builder.toString();
    }
    
    public void write(JavaScriptNode node, StringBuilder builder) {
        if (node instanceof JavaScriptBooleanLiteralNode) {
            builder.append(((JavaScriptBooleanLiteralNode) node).getValue() ? "true" : "false");
            return;
        }
        if (node instanceof JavaScriptNumberLiteralNode) {
            builder.append(((JavaScriptNumberLiteralNode) node).getValue());
            return;
        }
        if (node instanceof JavaScriptStringLiteralNode) {
            builder.append("\"");

            for (char c : Lists.charactersOf(((JavaScriptStringLiteralNode) node).getValue())) {
                builder.append(escapedCharacter(c));
            }
            
            builder.append("\"");
            return;
        }
        if (node instanceof JavaScriptIdentifierNode) {
            builder.append(((JavaScriptIdentifierNode) node).getValue());
            return;
        }
        if (node instanceof JavaScriptFunctionCallNode) {
            JavaScriptFunctionCallNode call = (JavaScriptFunctionCallNode) node;
            write(call.getFunction(), builder);
            builder.append("(");
            Joiner.on(", ").appendTo(builder, transform(call.getArguments(), stringify()));
            builder.append(")");
            return;
        }
        if (node instanceof JavaScriptReturnNode) {
            builder.append("return ");
            write(((JavaScriptReturnNode) node).getValue(), builder);
            builder.append(";");
            return;
        }
    }

    private Function<JavaScriptNode, String> stringify() {
        return new Function<JavaScriptNode, String>() {
            @Override
            public String apply(JavaScriptNode input) {
                return write(input);
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
