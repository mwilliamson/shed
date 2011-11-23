package org.zwobble.shed.compiler.parsing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionSignatureDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceBodyNode;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.tokeniser.Keyword;

import static org.zwobble.shed.compiler.parsing.Blocks.bracedStatementSequence;
import static org.zwobble.shed.compiler.parsing.Expressions.expression;
import static org.zwobble.shed.compiler.parsing.Expressions.typeExpression;
import static org.zwobble.shed.compiler.parsing.Functions.functionDeclaration;
import static org.zwobble.shed.compiler.parsing.Functions.functionSignatureDeclaration;
import static org.zwobble.shed.compiler.parsing.ParseResult.subResults;
import static org.zwobble.shed.compiler.parsing.Rules.firstOf;
import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.keyword;
import static org.zwobble.shed.compiler.parsing.Rules.oneOrMoreWithSeparator;
import static org.zwobble.shed.compiler.parsing.Rules.optional;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;
import static org.zwobble.shed.compiler.parsing.TypeReferences.typeSpecifier;
import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;


public class Statements {
    @SuppressWarnings("unchecked")
    public static Rule<StatementNode> statement() {
        return new Rule<StatementNode>() {
            @Override
            public ParseResult<StatementNode> parse(TokenNavigator tokens) {
                return firstOf("statement",
                    publicDeclaration(),
                    declaration(),
                    returnStatement(),
                    ifThenElseStatement(),
                    whileStatement(),
                    expressionStatement()
                ).parse(tokens);
            }
        };
    }
    
    public static Rule<PublicDeclarationNode> publicDeclaration() {
        final Rule<DeclarationNode> declaration = declaration();
        return then(
            sequence(OnError.FINISH,
                guard(keyword(Keyword.PUBLIC)),
                declaration
            ),
            new SimpleParseAction<RuleValues, PublicDeclarationNode>() {
                @Override
                public PublicDeclarationNode apply(RuleValues result) {
                    return new PublicDeclarationNode(result.get(declaration));
                }
            }
        );
    }
    
    @SuppressWarnings("unchecked")
    public static Rule<DeclarationNode> declaration() {
        return firstOf("declaration",
            immutableVariable(),
            mutableVariable(),
            objectDeclaration(),
            classDeclaration(),
            interfaceDeclaration(),
            functionDeclaration()
        );
    }

    public static Rule<VariableDeclarationNode> immutableVariable() {
        return variable(Keyword.VAL, new VariableNodeConstructor<VariableDeclarationNode>() {
            @Override
            public VariableDeclarationNode apply(String identifier, Option<ExpressionNode> typeReference, ExpressionNode expression) {
                return VariableDeclarationNode.immutable(identifier, typeReference, expression);
            }
        });
    }

    public static Rule<VariableDeclarationNode> mutableVariable() {
        return variable(Keyword.VAR, new VariableNodeConstructor<VariableDeclarationNode>() {
            @Override
            public VariableDeclarationNode apply(String identifier, Option<ExpressionNode> typeReference, ExpressionNode expression) {
                return VariableDeclarationNode.mutable(identifier, typeReference, expression);
            }
        });
    }
    
    public static Rule<ReturnNode> returnStatement() {
        final Rule<ExpressionNode> expression = expression();
        return then( 
            aStatement(
                guard(keyword(Keyword.RETURN)),
                expression
            ),
            new SimpleParseAction<RuleValues, ReturnNode>() {
                @Override
                public ReturnNode apply(RuleValues result) {
                    return new ReturnNode(result.get(expression));
                }
            }
        );
    }
    
    public static Rule<IfThenElseStatementNode> ifThenElseStatement() {
        final Rule<ExpressionNode> condition = expression();
        final Rule<BlockNode> ifTrue = Blocks.block();
        final Rule<Option<BlockNode>> elseClause = Rules.optional(elseClause());
        return then( 
            sequence(OnError.FINISH,
                guard(keyword(Keyword.IF)),
                condition,
                ifTrue,
                elseClause
            ),
            new SimpleParseAction<RuleValues, IfThenElseStatementNode>() {
                @Override
                public IfThenElseStatementNode apply(RuleValues result) {
                    Option<BlockNode> elseBody = result.get(elseClause);
                    BlockNode ifFalse = elseBody.orElse(Nodes.block()); 
                    return new IfThenElseStatementNode(result.get(condition), result.get(ifTrue), ifFalse);
                }
            }
        );
    }
    
    private static Rule<BlockNode> elseClause() {
        final Rule<BlockNode> ifFalse = Blocks.block();
        return then(
            sequence(OnError.FINISH,
                guard(keyword(Keyword.ELSE)),
                ifFalse
            ),
            new SimpleParseAction<RuleValues, BlockNode>() {
                @Override
                public BlockNode apply(RuleValues result) {
                    return result.get(ifFalse);
                }
            }
        );
    }
    
    public static Rule<WhileStatementNode> whileStatement() {
        final Rule<ExpressionNode> condition = expression();
        final Rule<BlockNode> body = Blocks.block();
        return then( 
            sequence(OnError.FINISH,
                guard(keyword(Keyword.WHILE)),
                condition,
                body
            ),
            new SimpleParseAction<RuleValues, WhileStatementNode>() {
                @Override
                public WhileStatementNode apply(RuleValues result) {
                    return new WhileStatementNode(result.get(condition), result.get(body));
                }
            }
        );
    }
    
    public static Rule<ExpressionStatementNode> expressionStatement() {
        final Rule<ExpressionNode> expression = guard(expression());
        return then( 
            aStatement(
                expression
            ),
            new SimpleParseAction<RuleValues, ExpressionStatementNode>() {
                @Override
                public ExpressionStatementNode apply(RuleValues result) {
                    return new ExpressionStatementNode(result.get(expression));
                }
            }
        );
    }
    
    public static Rule<ObjectDeclarationNode> objectDeclaration() {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<BlockNode> body = Blocks.block();
        return then(
            sequence(OnError.FINISH,
                guard(keyword(Keyword.OBJECT)),
                identifier,
                body
            ),
            new SimpleParseAction<RuleValues, ObjectDeclarationNode>() {
                @Override
                public ObjectDeclarationNode apply(RuleValues result) {
                    String objectName = result.get(identifier);
                    BlockNode statements = result.get(body);
                    return new ObjectDeclarationNode(objectName, statements);
                }
            }
        );
    }
    
    public static Rule<ClassDeclarationNode> classDeclaration() {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<List<FormalArgumentNode>> formalArguments = Arguments.formalArgumentList();
        final Rule<Option<List<ExpressionNode>>> superTypes = optional(superTypeSpecifier());
        final Rule<BlockNode> body = Blocks.block();
        return then(
            sequence(OnError.FINISH,
                guard(keyword(Keyword.CLASS)),
                identifier,
                formalArguments,
                superTypes,
                body
            ),
            new SimpleParseAction<RuleValues, ClassDeclarationNode>() {
                @Override
                public ClassDeclarationNode apply(RuleValues result) {
                    List<ExpressionNode> superTypeExpressions = result.get(superTypes).orElse(Collections.<ExpressionNode>emptyList());
                    return new ClassDeclarationNode(result.get(identifier), result.get(formalArguments), superTypeExpressions, result.get(body));
                }
            }
        );
    }
    
    private static Rule<List<ExpressionNode>> superTypeSpecifier() {
        Rule<List<ExpressionNode>> typeExpressions = oneOrMoreWithSeparator(typeExpression(), hardSeparator(symbol(",")));
        return then(
            sequence(OnError.FINISH,
                guard(symbol("<:")),
                typeExpressions
            ),
            ParseActions.extract(typeExpressions)
        );
    }
    
    public static Rule<InterfaceDeclarationNode> interfaceDeclaration() {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<InterfaceBodyNode> body = interfaceBody();
        return then(
            sequence(OnError.FINISH,
                guard(keyword(Keyword.INTERFACE)),
                identifier,
                body
            ),
            new SimpleParseAction<RuleValues, InterfaceDeclarationNode>() {
                @Override
                public InterfaceDeclarationNode apply(RuleValues result) {
                    return new InterfaceDeclarationNode(result.get(identifier), result.get(body));
                }
            }
        );
    }
    
    public static Rule<InterfaceBodyNode> interfaceBody() {
        return then(
            bracedStatementSequence(functionSignatureDeclaration()),
            new SimpleParseAction<List<FunctionSignatureDeclarationNode>, InterfaceBodyNode>() {
                @Override
                public InterfaceBodyNode apply(List<FunctionSignatureDeclarationNode> result) {
                    return new InterfaceBodyNode(result);
                }
            }
        );
    }
    
    public static Rule<RuleValues> aStatement(Rule<?>... rules) {
        final Rule<?>[] statementRules = Arrays.copyOf(rules, rules.length + 1);
        statementRules[rules.length] = symbol(";");
        return new Rule<RuleValues>() {
            @Override
            public ParseResult<RuleValues> parse(TokenNavigator tokens) {
                ParseResult<RuleValues> result = sequence(OnError.FINISH, statementRules).parse(tokens);
                if (!result.isFatal()) {
                    return result;
                }
                tokens.seekToEndOfStatement();
                return ParseResult.errorRecovered(result.getErrors(), subResults(result));
            }
        };
    }

    private static <T> Rule<T> variable(Keyword keyword, final VariableNodeConstructor<T> constructor) {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<? extends ExpressionNode> expression = expression(); 
        final Rule<Option<ExpressionNode>> type = optional(typeSpecifier());
        return then(
            aStatement(
                guard(keyword(keyword)),
                identifier,
                type,
                symbol("="),
                expression
            ),
            new SimpleParseAction<RuleValues, T>() {
                @Override
                public T apply(RuleValues result) {
                    return constructor.apply(result.get(identifier), result.get(type), result.get(expression));
                }
            }
        );
    }

    private interface VariableNodeConstructor<T> {
        T apply(String identifier, Option<ExpressionNode> typeReference, ExpressionNode expression);
    }
}
