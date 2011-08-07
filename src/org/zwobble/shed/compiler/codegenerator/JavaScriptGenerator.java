package org.zwobble.shed.compiler.codegenerator;

import java.util.List;

import org.zwobble.shed.compiler.ShedSymbols;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptVariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

public class JavaScriptGenerator {
    public static final ImportNode CORE_TYPES_IMPORT_NODE = new ImportNode(asList("shed", "core")); 
    
    private static final String CORE_TYPES_OBJECT_NAME = ShedSymbols.INTERNAL_PREFIX + "shed"; 
    private final JavaScriptNodes js = new JavaScriptNodes();
    private final JavaScriptImportGenerator importGenerator;
    private final JavaScriptModuleWrapper wrapper;
    
    public JavaScriptGenerator(JavaScriptImportGenerator importGenerator, JavaScriptModuleWrapper wrapper) {
        this.importGenerator = importGenerator;
        this.wrapper = wrapper;
    }
    
    public JavaScriptNode generate(SyntaxNode node) {
        if (node instanceof BooleanLiteralNode) {
            return js.call(js.id(CORE_TYPES_OBJECT_NAME + ".Boolean"), js.bool(((BooleanLiteralNode)node).getValue()));
        }
        if (node instanceof NumberLiteralNode) {
            return js.call(js.id(CORE_TYPES_OBJECT_NAME + ".Number"), js.number(((NumberLiteralNode)node).getValue()));
        }
        if (node instanceof StringLiteralNode) {
            return js.call(js.id(CORE_TYPES_OBJECT_NAME + ".String"), js.string(((StringLiteralNode)node).getValue()));
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
        if (node instanceof SourceNode) {
            SourceNode source = (SourceNode)node;
            PackageDeclarationNode packageDeclaration = source.getPackageDeclaration();
            JavaScriptNode coreTypesImportExpression = importGenerator.generateExpression(packageDeclaration, CORE_TYPES_IMPORT_NODE);
            JavaScriptVariableDeclarationNode coreTypesImport = js.var(CORE_TYPES_OBJECT_NAME, coreTypesImportExpression);
            Function<ImportNode, JavaScriptNode> toJavaScriptImport = toJavaScriptImport(packageDeclaration);
            Iterable<JavaScriptNode> importStatments = Iterables.transform(source.getImports(), toJavaScriptImport);
            Iterable<JavaScriptNode> sourceStatements = Iterables.transform(source.getStatements(), toJavaScriptStatement());
            JavaScriptStatements statements = js.statements(Iterables.concat(singleton(coreTypesImport), importStatments, sourceStatements));
            
            return wrapper.wrap(statements);
        }
        return null;
    }

    private Function<ImportNode, JavaScriptNode> toJavaScriptImport(final PackageDeclarationNode packageDeclaration) {
        return new Function<ImportNode, JavaScriptNode>() {
            @Override
            public JavaScriptNode apply(ImportNode input) {
                List<String> importNames = input.getNames();
                String name = importNames.get(importNames.size() - 1);
                return js.var(name, importGenerator.generateExpression(packageDeclaration, input));
            }
        };
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
