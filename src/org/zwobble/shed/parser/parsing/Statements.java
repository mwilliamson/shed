package org.zwobble.shed.parser.parsing;

import java.util.Arrays;

import org.zwobble.shed.parser.Option;
import org.zwobble.shed.parser.parsing.nodes.ExpressionNode;
import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.ReturnNode;
import org.zwobble.shed.parser.parsing.nodes.StatementNode;
import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.parser.tokeniser.Keyword;

import static org.zwobble.shed.parser.parsing.Expressions.expression;
import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.firstOf;
import static org.zwobble.shed.parser.parsing.Rules.guard;
import static org.zwobble.shed.parser.parsing.Rules.keyword;
import static org.zwobble.shed.parser.parsing.Rules.optional;
import static org.zwobble.shed.parser.parsing.Rules.sequence;
import static org.zwobble.shed.parser.parsing.Rules.symbol;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
import static org.zwobble.shed.parser.parsing.Rules.whitespace;
import static org.zwobble.shed.parser.parsing.TypeReferences.typeSpecifier;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Statements {
    @SuppressWarnings("unchecked")
    public static Rule<StatementNode> statement() {
        return firstOf("statement",
            immutableVariable(),
            mutableVariable(),
            returnStatement()
        );
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
                public Result<ReturnNode> apply(RuleValues result) {
                    return success(new ReturnNode(result.get(expression)));
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
            public Result<RuleValues> parse(TokenIterator tokens) {
                Result<RuleValues> result = sequence(OnError.FINISH, statementRules).parse(tokens);
                if (!result.isFatal()) {
                    return result;
                }
                tokens.seekToEndOfStatement();
                return new Result<RuleValues>(null, result.getErrors(), Result.Type.ERROR_RECOVERED);
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
                public Result<T> apply(RuleValues result) {
                    return success(constructor.apply(result.get(identifier), result.get(type), result.get(expression)));
                }
            }
        );
    }

    private interface VariableNodeConstructor<T> {
        T apply(String identifier, Option<TypeReferenceNode> typeReference, ExpressionNode expression);
    }
}
