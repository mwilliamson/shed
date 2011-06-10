package org.zwobble.shed.parser.parsing;

import java.util.Arrays;
import java.util.List;

import org.zwobble.shed.parser.parsing.nodes.ExpressionNode;
import org.zwobble.shed.parser.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.parser.parsing.nodes.FunctionNode;
import org.zwobble.shed.parser.parsing.nodes.ReturnNode;
import org.zwobble.shed.parser.parsing.nodes.StatementNode;
import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.parser.parsing.nodes.VariableIdentifierNode;

import static org.zwobble.shed.parser.parsing.Blocks.block;
import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.firstOf;
import static org.zwobble.shed.parser.parsing.Rules.guard;
import static org.zwobble.shed.parser.parsing.Rules.optional;
import static org.zwobble.shed.parser.parsing.Rules.sequence;
import static org.zwobble.shed.parser.parsing.Rules.symbol;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
import static org.zwobble.shed.parser.parsing.Rules.whitespace;
import static org.zwobble.shed.parser.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.parser.parsing.Separator.hardSeparator;
import static org.zwobble.shed.parser.parsing.TypeReferences.typeSpecifier;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Expressions {
    @SuppressWarnings("unchecked")
    public static Rule<ExpressionNode> expression() {
        return new Rule<ExpressionNode>() {
            @Override
            public Result<ExpressionNode> parse(TokenIterator tokens) {
                return Rules.firstOf("expression",
                    function(),
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
                public Result<ExpressionNode> apply(String result) {
                    return Result.<ExpressionNode>success(new VariableIdentifierNode(result));
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
                public Result<ExpressionNode> apply(RuleValues result) {
                    return success(result.get(expression));
                }
            }
        );
    }
    
    private static Rule<FunctionNode> function() {
        final Rule<?> comma = sequence(OnError.FINISH, optional(whitespace()), symbol(","), optional(whitespace()));
        final Rule<List<FormalArgumentNode>> formalArguments = zeroOrMoreWithSeparator(formalArgument(), hardSeparator(comma));
        final Rule<List<StatementNode>> functionBody = functionBody();
        return then(
            sequence(OnError.FINISH,
                guard(symbol("(")),
                formalArguments,
                guard(symbol(")")),
                optional(whitespace()),
                guard(symbol("=>")),
                optional(whitespace()),
                functionBody
            ),
            new ParseAction<RuleValues, FunctionNode>() {
                @Override
                public Result<FunctionNode> apply(RuleValues result) {
                    return success(new FunctionNode(
                        result.get(formalArguments),
                        result.get(functionBody)
                    ));
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
                public Result<FormalArgumentNode> apply(RuleValues result) {
                    return success(new FormalArgumentNode(result.get(name), result.get(type)));
                }
            }
        );
    }
    
    @SuppressWarnings("unchecked")
    private static Rule<List<StatementNode>> functionBody() {
        final Rule<ExpressionNode> expression = expression();
        return firstOf("expression",
            block(),
            then(expression, new ParseAction<ExpressionNode, List<StatementNode>>() {
                @Override
                public Result<List<StatementNode>> apply(ExpressionNode result) {
                    return success(Arrays.<StatementNode>asList(new ReturnNode(result)));
                }
            })
        );
    }
}
