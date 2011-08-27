package org.zwobble.shed.compiler.parsing;

import java.util.Arrays;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.tokeniser.Keyword;

import static org.zwobble.shed.compiler.parsing.Expressions.expression;
import static org.zwobble.shed.compiler.parsing.ParseResult.subResults;
import static org.zwobble.shed.compiler.parsing.Rules.firstOf;
import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.keyword;
import static org.zwobble.shed.compiler.parsing.Rules.optional;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
import static org.zwobble.shed.compiler.parsing.Rules.whitespace;
import static org.zwobble.shed.compiler.parsing.TypeReferences.typeSpecifier;
import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;


public class Statements {
    @SuppressWarnings("unchecked")
    public static Rule<StatementNode> statement() {
        return new Rule<StatementNode>() {
            @Override
            public ParseResult<StatementNode> parse(TokenIterator tokens) {
                return firstOf("statement",
                    immutableVariable(),
                    mutableVariable(),
                    returnStatement(),
                    objectDeclaration(),
                    expressionStatement()
                ).parse(tokens);
            }
        };
    }
    
    public static Rule<ImmutableVariableNode> immutableVariable() {
        return variable(Keyword.VAL, new VariableNodeConstructor<ImmutableVariableNode>() {
            @Override
            public ImmutableVariableNode apply(String identifier, Option<TypeReferenceNode> typeReference, ExpressionNode expression) {
                return new ImmutableVariableNode(identifier, typeReference, expression);
            }
        });
    }

    public static Rule<MutableVariableNode> mutableVariable() {
        return variable(Keyword.VAR, new VariableNodeConstructor<MutableVariableNode>() {
            @Override
            public MutableVariableNode apply(String identifier, Option<TypeReferenceNode> typeReference, ExpressionNode expression) {
                return new MutableVariableNode(identifier, typeReference, expression);
            }
        });
    }
    
    public static Rule<ReturnNode> returnStatement() {
        final Rule<ExpressionNode> expression = expression();
        return then( 
            aStatement(
                guard(keyword(Keyword.RETURN)),
                optional(whitespace()),
                expression
            ),
            new ParseAction<RuleValues, ReturnNode>() {
                @Override
                public ReturnNode apply(RuleValues result) {
                    return new ReturnNode(result.get(expression));
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
            new ParseAction<RuleValues, ExpressionStatementNode>() {
                @Override
                public ExpressionStatementNode apply(RuleValues result) {
                    return new ExpressionStatementNode(result.get(expression));
                }
            }
        );
    }
    
    public static Rule<ObjectDeclarationNode> objectDeclaration() {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<List<StatementNode>> body = Blocks.block();
        return then(
            sequence(OnError.FINISH,
                guard(keyword(Keyword.OBJECT)),
                optional(whitespace()),
                identifier,
                optional(whitespace()),
                body
            ),
            new ParseAction<RuleValues, ObjectDeclarationNode>() {
                @Override
                public ObjectDeclarationNode apply(RuleValues result) {
                    String objectName = result.get(identifier);
                    List<StatementNode> statements = result.get(body);
                    return new ObjectDeclarationNode(objectName, statements);
                }
            }
        );
    }
    
    public static Rule<RuleValues> aStatement(Rule<?>... rules) {
        final Rule<?>[] statementRules = Arrays.copyOf(rules, rules.length + 2);
        statementRules[rules.length] = optional(whitespace());
        statementRules[rules.length + 1] = symbol(";");
        return new Rule<RuleValues>() {
            @Override
            public ParseResult<RuleValues> parse(TokenIterator tokens) {
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
        final Rule<Option<TypeReferenceNode>> type = optional(typeSpecifier());
        return then(
            aStatement(
                guard(keyword(keyword)), whitespace(),
                identifier, optional(whitespace()),
                type, optional(whitespace()),
                symbol("="), optional(whitespace()),
                expression
            ),
            new ParseAction<RuleValues, T>() {
                @Override
                public T apply(RuleValues result) {
                    return constructor.apply(result.get(identifier), result.get(type), result.get(expression));
                }
            }
        );
    }

    private interface VariableNodeConstructor<T> {
        T apply(String identifier, Option<TypeReferenceNode> typeReference, ExpressionNode expression);
    }
}
