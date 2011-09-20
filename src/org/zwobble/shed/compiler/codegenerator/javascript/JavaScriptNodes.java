package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

public class JavaScriptNodes {
    public JavaScriptBooleanLiteralNode bool(boolean value) {
        return new JavaScriptBooleanLiteralNode(value);
    }
    
    public JavaScriptNumberLiteralNode number(String value) {
        return new JavaScriptNumberLiteralNode(value);
    }
    
    public JavaScriptFunctionCallNode call(JavaScriptExpressionNode function, JavaScriptExpressionNode... arguments) {
        return new JavaScriptFunctionCallNode(function, asList(arguments));
    }
    
    public JavaScriptFunctionCallNode call(JavaScriptExpressionNode function, List<JavaScriptExpressionNode> arguments) {
        return new JavaScriptFunctionCallNode(function, arguments);
    }
    
    public JavaScriptIdentifierNode id(String identifier) {
        return new JavaScriptIdentifierNode(identifier);
    }

    public JavaScriptVariableDeclarationNode var(String name, JavaScriptNode initialValue) {
        return new JavaScriptVariableDeclarationNode(name, initialValue);
    }
    
    public JavaScriptFunctionNode func(List<String> arguments, List<JavaScriptStatementNode> statements) {
        return new JavaScriptFunctionNode(arguments, statements);
    }
    
    public JavaScriptReturnNode ret(JavaScriptNode value) {
        return new JavaScriptReturnNode(value);
    }

    public JavaScriptStringLiteralNode string(String value) {
        return new JavaScriptStringLiteralNode(value);
    }
    
    public JavaScriptStatements statements(JavaScriptStatementNode... statements) {
        return new JavaScriptStatements(asList(statements));
    }
    
    public JavaScriptStatements statements(Iterable<JavaScriptStatementNode> statements) {
        return new JavaScriptStatements(ImmutableList.copyOf(statements));
    }
    
    public JavaScriptExpressionStatement expressionStatement(JavaScriptExpressionNode expression) {
        return new JavaScriptExpressionStatement(expression);
    }

    public JavaScriptNode object(Map<String, JavaScriptExpressionNode> properties) {
        return new JavaScriptObjectLiteralNode(properties);
    }

    public JavaScriptExpressionNode propertyAccess(JavaScriptExpressionNode expression, String propertyName) {
        return new JavaScriptPropertyAccessNode(expression, propertyName);
    }
    
    public JavaScriptStatementNode forIn(String name, JavaScriptExpressionNode object, List<JavaScriptStatementNode> statements) {
        return new JavaScriptForInNode(name, object, statements);
    }

    public JavaScriptStatementNode ifThen(JavaScriptExpressionNode condition, JavaScriptStatementNode... ifTrue) {
        return new JavaScriptIfThenElseNode(condition, asList(ifTrue), Collections.<JavaScriptStatementNode>emptyList());
    }

    public JavaScriptStatementNode ifThenElse(JavaScriptExpressionNode condition, List<JavaScriptStatementNode> ifTrue, List<JavaScriptStatementNode> ifFalse) {
        return new JavaScriptIfThenElseNode(condition, ifTrue, ifFalse);
    }

    public JavaScriptStatementNode whileLoop(JavaScriptExpressionNode condition, JavaScriptStatementNode... body) {
        return new JavaScriptWhileNode(condition, asList(body));
    }
    
    public JavaScriptStatementNode scope(JavaScriptStatementNode... statements) {
        return scope(asList(statements));
    }
    
    public JavaScriptStatementNode scope(List<JavaScriptStatementNode> statements) {
        return expressionStatement(call(func(Collections.<String>emptyList(), statements)));
    }

    public JavaScriptExpressionNode undefined() {
        return id("undefined");
    }

    public JavaScriptExpressionNode operator(String operator, JavaScriptExpressionNode firstOperand, JavaScriptExpressionNode secondOperand) {
        return new JavaScriptBinaryOperatorNode(operator, firstOperand, secondOperand);
    }
    
    public JavaScriptExpressionNode assign(JavaScriptExpressionNode target, JavaScriptExpressionNode value) {
        return new JavaScriptAssignmentNode(target, value);
    }
}
