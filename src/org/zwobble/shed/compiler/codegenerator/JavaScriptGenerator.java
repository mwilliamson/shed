package org.zwobble.shed.compiler.codegenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.ShedSymbols;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.UnitLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

public class JavaScriptGenerator {
    public static final ImportNode CORE_TYPES_IMPORT_NODE = new ImportNode(asList("shed", "core"));
    public static final String CORE_VALUES_OBJECT_NAME = ShedSymbols.INTERNAL_PREFIX + "core";
    
    private final JavaScriptNodes js = new JavaScriptNodes();
    private final JavaScriptModuleWrapper wrapper;
    
    public JavaScriptGenerator(JavaScriptModuleWrapper wrapper) {
        this.wrapper = wrapper;
    }
    
    public JavaScriptNode generate(SourceNode node, Iterable<String> coreValues) {
        SourceNode source = (SourceNode)node;
        PackageDeclarationNode packageDeclaration = source.getPackageDeclaration();
        
        Iterable<JavaScriptStatementNode> sourceStatements = Iterables.concat(
            Iterables.transform(coreValues, coreValueToJavaScript()),
            Iterables.transform(source.getStatements(), toJavaScriptStatement()),
            Iterables.transform(Iterables.filter(source.getStatements(), isPublicDeclaration()), toJavaScriptExport(packageDeclaration))
        );
        
        return wrapper.wrap(packageDeclaration, node.getImports(), js.statements(sourceStatements));
    }

    private Function<String, JavaScriptStatementNode> coreValueToJavaScript() {
        return new Function<String, JavaScriptStatementNode>() {
            @Override
            public JavaScriptStatementNode apply(String input) {
                return js.var(input, js.id(CORE_VALUES_OBJECT_NAME + "." + input));
            }
        };
    }

    public JavaScriptExpressionNode generateExpression(ExpressionNode node) {
        if (node instanceof BooleanLiteralNode) {
            return js.call(js.id(CORE_VALUES_OBJECT_NAME + ".Boolean"), js.bool(((BooleanLiteralNode)node).getValue()));
        }
        if (node instanceof NumberLiteralNode) {
            return js.call(js.id(CORE_VALUES_OBJECT_NAME + ".Number"), js.number(((NumberLiteralNode)node).getValue()));
        }
        if (node instanceof StringLiteralNode) {
            return js.call(js.id(CORE_VALUES_OBJECT_NAME + ".String"), js.string(((StringLiteralNode)node).getValue()));
        }
        if (node instanceof UnitLiteralNode) {
            return js.call(js.id(CORE_VALUES_OBJECT_NAME + ".Unit"));
        }
        if (node instanceof VariableIdentifierNode) {
            return js.id(((VariableIdentifierNode) node).getIdentifier());
        }
        if (node instanceof ShortLambdaExpressionNode) {
            ShortLambdaExpressionNode lambda = (ShortLambdaExpressionNode)node;
            List<String> argumentNames = transform(lambda.getFormalArguments(), toFormalArgumentName());
            List<JavaScriptStatementNode> javaScriptBody = asList((JavaScriptStatementNode)js.ret(generateExpression(lambda.getBody())));
            return js.func(argumentNames, javaScriptBody);
        }
        if (node instanceof LongLambdaExpressionNode) {
            LongLambdaExpressionNode lambda = (LongLambdaExpressionNode)node;
            List<JavaScriptStatementNode> javaScriptBody = transform(lambda.getBody(), toJavaScriptStatement());
            List<String> argumentNames = transform(lambda.getFormalArguments(), toFormalArgumentName());
            return js.func(argumentNames, javaScriptBody);
        }
        if (node instanceof CallNode) {
            CallNode call = (CallNode) node;
            List<JavaScriptExpressionNode> jsArguments = transform(call.getArguments(), toJavaScriptExpression());
            return js.call(generateExpression(call.getFunction()), jsArguments);
        }
        if (node instanceof MemberAccessNode) {
            MemberAccessNode memberAccess = (MemberAccessNode) node;
            ExpressionNode expression = memberAccess.getExpression();
            String memberName = memberAccess.getMemberName();
            return js.propertyAccess(generateExpression(expression), memberName);
        }
        if (node instanceof TypeApplicationNode) {
            TypeApplicationNode typeApplication = (TypeApplicationNode)node;
            List<JavaScriptExpressionNode> jsArguments = transform(typeApplication.getParameters(), toJavaScriptExpression());
            return js.call(generateExpression(typeApplication.getBaseValue()), jsArguments);
        }
        throw new RuntimeException("Cannot generate JavaScript for " + node);
    }

    public JavaScriptStatementNode generateStatement(StatementNode node) {
        if (node instanceof VariableDeclarationNode) {
            VariableDeclarationNode immutableVariable = (VariableDeclarationNode)node;
            return js.var(immutableVariable.getIdentifier(), generateExpression(immutableVariable.getValue()));
        }
        if (node instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode)node;
            return js.ret(generateExpression(returnNode.getExpression()));
        }
        if (node instanceof ExpressionStatementNode) {
            return js.expressionStatement(generateExpression(((ExpressionStatementNode) node).getExpression()));
        }
        if (node instanceof PublicDeclarationNode) {
            return generateStatement(((PublicDeclarationNode) node).getDeclaration());
        }
        if (node instanceof ObjectDeclarationNode) {
            ObjectDeclarationNode objectDeclaration = (ObjectDeclarationNode) node;
            List<StatementNode> statements = objectDeclaration.getStatements();
            List<JavaScriptStatementNode> javaScriptBody = newArrayList(transform(statements, toJavaScriptStatement()));

            Set<String> publicMembers = new HashSet<String>();
            for (StatementNode statement : statements) {
                if (statement instanceof PublicDeclarationNode) {
                    publicMembers.add(((PublicDeclarationNode) statement).getDeclaration().getIdentifier());
                }
            }
            
            Map<String, JavaScriptExpressionNode> javaScriptProperties = new HashMap<String, JavaScriptExpressionNode>();
            for (String publicMember : publicMembers) {
                javaScriptProperties.put(publicMember, js.id(publicMember));
            }
            
            javaScriptBody.add(js.ret(js.object(javaScriptProperties)));
            return js.var(objectDeclaration.getIdentifier(), js.call(js.func(
                Collections.<String>emptyList(),
                javaScriptBody
            )));
        }
        if (node instanceof IfThenElseStatementNode) {
            IfThenElseStatementNode ifThenElse = (IfThenElseStatementNode) node;
            return js.ifThenElse(
                generateExpression(ifThenElse.getCondition()),
                transform(ifThenElse.getIfTrue(), toJavaScriptStatement()),
                transform(ifThenElse.getIfFalse(), toJavaScriptStatement())
            );
        }
        throw new RuntimeException("Cannot generate JavaScript for " + node);
    }

    private Function<FormalArgumentNode, String> toFormalArgumentName() {
        return new Function<FormalArgumentNode, String>() {
            @Override
            public String apply(FormalArgumentNode input) {
                return input.getName();
            }
        };
    }

    private Function<StatementNode, JavaScriptStatementNode> toJavaScriptStatement() {
        return new Function<StatementNode, JavaScriptStatementNode>() {
            @Override
            public JavaScriptStatementNode apply(StatementNode input) {
                return generateStatement(input);
            }
        };
    }

    private Function<ExpressionNode, JavaScriptExpressionNode> toJavaScriptExpression() {
        return new Function<ExpressionNode, JavaScriptExpressionNode>() {
            @Override
            public JavaScriptExpressionNode apply(ExpressionNode input) {
                return generateExpression(input);
            }
        };
    }

    private Predicate<StatementNode> isPublicDeclaration() {
        return new Predicate<StatementNode>() {
            @Override
            public boolean apply(StatementNode input) {
                return input instanceof PublicDeclarationNode;
            }
        };
    }

    private Function<StatementNode, JavaScriptStatementNode> toJavaScriptExport(final PackageDeclarationNode packageDeclaration) {
        return new Function<StatementNode, JavaScriptStatementNode>() {
            @Override
            public JavaScriptStatementNode apply(StatementNode input) {
                String identifier = ((PublicDeclarationNode)input).getDeclaration().getIdentifier();
                String fullIdentifier = Joiner.on(".").join(packageDeclaration.getPackageNames()) + "." + identifier;
                return js.expressionStatement(js.call(js.id("SHED.exportValue"), js.string(fullIdentifier), js.id(identifier)));
            }
        };
    }
}
