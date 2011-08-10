package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptBooleanLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptIdentifierNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNumberLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStringLiteralNode;

import com.google.common.collect.Lists;

public class JavaScriptWriter {
    public String write(JavaScriptNode node) {
        if (node instanceof JavaScriptBooleanLiteralNode) {
            return ((JavaScriptBooleanLiteralNode) node).getValue() ? "true" : "false";
        }
        if (node instanceof JavaScriptNumberLiteralNode) {
            return ((JavaScriptNumberLiteralNode) node).getValue();
        }
        if (node instanceof JavaScriptStringLiteralNode) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("\"");

            for (char c : Lists.charactersOf(((JavaScriptStringLiteralNode) node).getValue())) {
                buffer.append(escapedCharacter(c));
            }
            
            buffer.append("\"");
            return buffer.toString();
        }
        if (node instanceof JavaScriptIdentifierNode) {
            return ((JavaScriptIdentifierNode) node).getValue();
        }
        return "";
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
