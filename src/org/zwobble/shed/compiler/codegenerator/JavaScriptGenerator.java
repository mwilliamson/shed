package org.zwobble.shed.compiler.codegenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.ShedSymbols;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptFunctionCallNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionWithBodyNode;
import org.zwobble.shed.compiler.parsing.nodes.HoistableStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LiteralNode;
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
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.referenceresolution.References;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Eager.transform;

public class JavaScriptGenerator {
    public static final ImportNode CORE_TYPES_IMPORT_NODE = new ImportNode(asList("shed", "core"));
    public static final String CORE_VALUES_OBJECT_NAME = ShedSymbols.INTERNAL_PREFIX + "core";
    
    private final JavaScriptNodes js = new JavaScriptNodes();
    private final JavaScriptModuleWrapper wrapper;
    private final JavaScriptNamer namer;
    
    public JavaScriptGenerator(JavaScriptModuleWrapper wrapper, References references) {
        this.wrapper = wrapper;
        this.namer = new JavaScriptNamer(references);
    }
    
    public JavaScriptNode generate(SourceNode node, Iterable<String> coreValues) {
        SourceNode source = (SourceNode)node;
        PackageDeclarationNode packageDeclaration = source.getPackageDeclaration();
        
        Iterable<JavaScriptStatementNode> sourceStatements = Iterables.concat(
            Iterables.transform(coreValues, coreValueToJavaScript()),
            Iterables.transform(source.getStatements(), toJavaScriptStatement()),
            Iterables.transform(Iterables.filter(source.getStatements(), isPublicDeclaration()), toJavaScriptExport(packageDeclaration))
        );
        
        return wrapper.wrap(packageDeclaration, node.getImports(), js.statements(sourceStatements), namer);
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
        if (node instanceof LiteralNode) {
            return generateLiteral((LiteralNode) node);            
        }
        if (node instanceof VariableIdentifierNode) {
            return js.id(namer.javaScriptIdentifierFor((VariableIdentifierNode) node));
        }
        if (node instanceof ShortLambdaExpressionNode) {
            ShortLambdaExpressionNode lambda = (ShortLambdaExpressionNode)node;
            List<String> argumentNames = transform(lambda.getFormalArguments(), toFormalArgumentName());
            List<JavaScriptStatementNode> javaScriptBody = asList((JavaScriptStatementNode)js.ret(generateExpression(lambda.getBody())));
            return js.func(argumentNames, javaScriptBody);
        }
        if (node instanceof LongLambdaExpressionNode) {
            LongLambdaExpressionNode lambda = (LongLambdaExpressionNode)node;
            return generateFunctionWithBody(lambda);
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
        if (node instanceof AssignmentExpressionNode) {
            AssignmentExpressionNode assignment = (AssignmentExpressionNode) node;
            return js.assign(generateExpression(assignment.getTarget()), generateExpression(assignment.getValue()));
        }
        throw new RuntimeException("Cannot generate JavaScript for " + node);
    }

    private JavaScriptFunctionCallNode generateLiteral(LiteralNode node) {
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
        throw new RuntimeException("Cannot generate JavaScript for " + node);
    }

    public JavaScriptStatementNode generateStatement(StatementNode node) {
        if (node instanceof VariableDeclarationNode) {
            VariableDeclarationNode immutableVariable = (VariableDeclarationNode)node;
            return js.var(namer.javaScriptIdentifierFor(immutableVariable), generateExpression(immutableVariable.getValue()));
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
            BlockNode statements = objectDeclaration.getStatements();
            List<JavaScriptStatementNode> javaScriptBody = generateBlock(statements);

            List<DeclarationNode> publicMembers = new ArrayList<DeclarationNode>();
            for (StatementNode statement : statements) {
                if (statement instanceof PublicDeclarationNode) {
                    publicMembers.add(((PublicDeclarationNode) statement).getDeclaration());
                }
            }
            
            Map<String, JavaScriptExpressionNode> javaScriptProperties = new HashMap<String, JavaScriptExpressionNode>();
            for (DeclarationNode publicMember : publicMembers) {
                javaScriptProperties.put(publicMember.getIdentifier(), js.id(namer.javaScriptIdentifierFor(publicMember)));
            }
            
            javaScriptBody.add(js.ret(js.object(javaScriptProperties)));
            return js.var(namer.javaScriptIdentifierFor(objectDeclaration), js.call(js.func(
                Collections.<String>emptyList(),
                javaScriptBody
            )));
        }
        if (node instanceof IfThenElseStatementNode) {
            IfThenElseStatementNode ifThenElse = (IfThenElseStatementNode) node;
            return js.ifThenElse(
                generateExpression(ifThenElse.getCondition()),
                generateBlock(ifThenElse.getIfTrue()),
                generateBlock(ifThenElse.getIfFalse())
            );
        }
        if (node instanceof WhileStatementNode) {
            WhileStatementNode whileNode = (WhileStatementNode) node;
            String loopBodyIdentifier = namer.freshJavaScriptIdentifier("__tmp_loopBody");
            String resultIdentifier = namer.freshJavaScriptIdentifier("__tmp_loopBodyResult");
            return js.statements(
                js.var(loopBodyIdentifier, js.func(
                    Collections.<String>emptyList(),
                    transform(whileNode.getBody(), toJavaScriptStatement())
                )),
                js.whileLoop(
                    generateExpression(whileNode.getCondition()),
                    js.var(resultIdentifier, js.call(js.id(loopBodyIdentifier))),
                    js.ifThen(js.operator("!==", js.id(resultIdentifier), js.undefined()), js.ret(js.id(resultIdentifier)))
                )
            );
        }
        if (node instanceof FunctionDeclarationNode) {
            FunctionDeclarationNode function = (FunctionDeclarationNode) node;
            return js.var(namer.javaScriptIdentifierFor(function), generateFunctionWithBody(function));
        }
        throw new RuntimeException("Cannot generate JavaScript for " + node);
    }

    public List<JavaScriptStatementNode> generateBlock(BlockNode statements) {
        Iterable<StatementNode> hoistableStatements = filter(statements, isHoistableStatement());
        Iterable<StatementNode> fixedStatements = filter(statements, not(isHoistableStatement()));
        return newArrayList(concat(transform(hoistableStatements, toJavaScriptStatement()), transform(fixedStatements, toJavaScriptStatement())));
    }

    private Predicate<Object> isHoistableStatement() {
        return Predicates.instanceOf(HoistableStatementNode.class);
    }

    private JavaScriptExpressionNode generateFunctionWithBody(FunctionWithBodyNode function) {
        List<JavaScriptStatementNode> javaScriptBody = generateBlock(function.getBody());
        List<String> argumentNames = transform(function.getFormalArguments(), toFormalArgumentName());
        return js.func(argumentNames, javaScriptBody);
    }

    private Function<FormalArgumentNode, String> toFormalArgumentName() {
        return new Function<FormalArgumentNode, String>() {
            @Override
            public String apply(FormalArgumentNode input) {
                return namer.javaScriptIdentifierFor(input);
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
                DeclarationNode declaration = ((PublicDeclarationNode)input).getDeclaration();
                String javaScriptIdentifier = namer.javaScriptIdentifierFor(declaration);
                String fullIdentifier = Joiner.on(".").join(packageDeclaration.getPackageNames()) + "." + declaration.getIdentifier();
                return js.expressionStatement(js.call(js.id("SHED.exportValue"), js.string(fullIdentifier), js.id(javaScriptIdentifier)));
            }
        };
    }
}
