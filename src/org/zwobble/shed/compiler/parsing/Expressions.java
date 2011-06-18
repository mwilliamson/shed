package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

import static org.zwobble.shed.compiler.parsing.Blocks.block;
import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.optional;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
import static org.zwobble.shed.compiler.parsing.Rules.whitespace;
import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;
import static org.zwobble.shed.compiler.parsing.TypeReferences.typeSpecifier;
import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;


public class Expressions {
    @SuppressWarnings("unchecked")
    public static Rule<ExpressionNode> expression() {
        return new Rule<ExpressionNode>() {
            @Override
            public ParseResult<ExpressionNode> parse(TokenIterator tokens) {
                return Rules.firstOf("expression",
                    longLambdaExpression(),
                    shortLambdaExpression(),
                    variableIdentifier(),
                    Literals.numberLiteral(),
                    Literals.stringLiteral(),
                    Literals.booleanLiteral(),
                    expressionInParens()
                ).parse(tokens);
            }
        };
    }

    private static Rule<ExpressionNode> variableIdentifier() {
        return then(
            tokenOfType(IDENTIFIER),
            new ParseAction<String, ExpressionNode>() {
                @Override
                public ExpressionNode apply(String result) {
                    return new VariableIdentifierNode(result);
                }
            }
        );
    }

    private static Rule<ExpressionNode> expressionInParens() {
        final Rule<ExpressionNode> expression = expression();
        return then(
            sequence(OnError.FINISH,
                guard(symbol("(")),
                optional(whitespace()),
                expression,
                optional(whitespace()),
                symbol(")")
            ),
            new ParseAction<RuleValues, ExpressionNode>() {
                @Override
                public ExpressionNode apply(RuleValues result) {
                    return result.get(expression);
                }
            }
        );
    }
    
    private static Rule<LongLambdaExpressionNode> longLambdaExpression() {
        final Rule<List<FormalArgumentNode>> formalArguments;
        final Rule<TypeReferenceNode> returnType;
        final Rule<List<StatementNode>> functionBody;
        return then(
            sequence(OnError.FINISH,
                formalArguments = guard(formalArgumentList()),
                optional(whitespace()),
                returnType = guard(typeSpecifier()),
                optional(whitespace()),
                guard(symbol("=>")),
                optional(whitespace()),
                functionBody = guard(block())
            ),
            new ParseAction<RuleValues, LongLambdaExpressionNode>() {
                @Override
                public LongLambdaExpressionNode apply(RuleValues result) {
                    return new LongLambdaExpressionNode(
                        result.get(formalArguments),
                        result.get(returnType),
                        result.get(functionBody)
                    );
                }
            }
        );
    }
    
    private static Rule<ShortLambdaExpressionNode> shortLambdaExpression() {
        final Rule<List<FormalArgumentNode>> formalArguments;
        final Rule<Option<TypeReferenceNode>> returnType;
        final Rule<ExpressionNode> functionBody = expression();
        return then(
            sequence(OnError.FINISH,
                formalArguments = guard(formalArgumentList()),
                optional(whitespace()),
                returnType = optional(typeSpecifier()),
                optional(whitespace()),
                guard(symbol("=>")),
                optional(whitespace()),
                functionBody
            ),
            new ParseAction<RuleValues, ShortLambdaExpressionNode>() {
                @Override
                public ShortLambdaExpressionNode apply(RuleValues result) {
                    return new ShortLambdaExpressionNode(
                        result.get(formalArguments),
                        result.get(returnType),
                        result.get(functionBody)
                    );
                }
            }
        );
    }
    
    private static Rule<List<FormalArgumentNode>> formalArgumentList() {
        final Rule<?> comma = sequence(OnError.FINISH, optional(whitespace()), symbol(","), optional(whitespace()));
        final Rule<List<FormalArgumentNode>> formalArguments = zeroOrMoreWithSeparator(formalArgument(), hardSeparator(comma));
        return then(
            sequence(OnError.FINISH,
                guard(symbol("(")),
                formalArguments,
                guard(symbol(")"))
            ),
            new ParseAction<RuleValues, List<FormalArgumentNode>>() {
                @Override
                public List<FormalArgumentNode> apply(RuleValues result) {
                    return result.get(formalArguments);
                }
            }
        );
        
    }

    private static Rule<FormalArgumentNode> formalArgument() {
        final Rule<String> name;
        final Rule<TypeReferenceNode> type = typeSpecifier();
        return then(
            sequence(OnError.FINISH,
                name = guard(tokenOfType(IDENTIFIER)),
                guard(optional(whitespace())),
                type
            ),
            new ParseAction<RuleValues, FormalArgumentNode>() {
                @Override
                public FormalArgumentNode apply(RuleValues result) {
                    return new FormalArgumentNode(result.get(name), result.get(type));
                }
            }
        );
    }
}
