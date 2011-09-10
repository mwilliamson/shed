package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

import static org.zwobble.shed.compiler.parsing.Blocks.block;
import static org.zwobble.shed.compiler.parsing.Rules.firstOf;
import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.optional;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
import static org.zwobble.shed.compiler.parsing.Rules.whitespace;
import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;
import static org.zwobble.shed.compiler.parsing.Separator.softSeparator;
import static org.zwobble.shed.compiler.parsing.TypeReferences.typeSpecifier;
import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;


public class Expressions {
    private static final Rule<?> COMMA = sequence(OnError.FINISH, optional(whitespace()), guard(symbol(",")), optional(whitespace()));
    
    private static interface PartialCallExpression {
        ExpressionNode complete(ExpressionNode expression);
    }
    
    @SuppressWarnings("unchecked")
    public static Rule<ExpressionNode> expression() {
        return new Rule<ExpressionNode>() {
            @Override
            public ParseResult<ExpressionNode> parse(TokenNavigator tokens) {
                final Rule<ExpressionNode> left = guard(firstOf("expression",
                    longLambdaExpression(),
                    shortLambdaExpression(),
                    variableIdentifier(),
                    Literals.numberLiteral(),
                    Literals.stringLiteral(),
                    Literals.booleanLiteral(),
                    Literals.unitLiteral(),
                    expressionInParens()
                )); 
                
                final Rule<List<PartialCallExpression>> calls = zeroOrMoreWithSeparator(
                    firstOf("function call or member access",
                        functionCall(),
                        memberAccess(),
                        typeApplication()
                    ),
                    softSeparator(optional(whitespace()))
                );
                return then(
                    sequence(OnError.FINISH,
                        left,
                        optional(whitespace()),
                        calls
                    ),
                    new ParseAction<RuleValues, ExpressionNode>() {
                        @Override
                        public ExpressionNode apply(RuleValues values) {
                            ExpressionNode result = values.get(left);
                            List<PartialCallExpression> callExpressions = values.get(calls);
                            for (PartialCallExpression callExpression : callExpressions) {
                                result = callExpression.complete(result);
                            }
                            return result;
                        }
                    }
                 ).parse(tokens);
            }
        };
    }
    
    private static Rule<PartialCallExpression> functionCall() {
        final Rule<List<ExpressionNode>> arguments = argumentList();
        
        return then(
            sequence(OnError.FINISH,
                guard(symbol("(")),
                arguments,
                symbol(")")
            ),
            new ParseAction<RuleValues, PartialCallExpression>() {
                @Override
                public PartialCallExpression apply(final RuleValues result) {
                    return new PartialCallExpression() {
                        @Override
                        public ExpressionNode complete(ExpressionNode expression) {
                            return new CallNode(expression, result.get(arguments));
                        }
                    };
                }
            }
        );
    }
    
    private static Rule<PartialCallExpression> memberAccess() {
        final Rule<String> memberName = tokenOfType(IDENTIFIER);
        return then(
            sequence(OnError.FINISH,
                guard(symbol(".")),
                optional(whitespace()),
                memberName
            ),
            new ParseAction<RuleValues, PartialCallExpression>() {
                @Override
                public PartialCallExpression apply(final RuleValues result) {
                    return new PartialCallExpression() {
                        @Override
                        public ExpressionNode complete(ExpressionNode expression) {
                            return new MemberAccessNode(expression, result.get(memberName));
                        }
                    };
                }
            }
        );
    }
    
    private static Rule<PartialCallExpression> typeApplication() {
        final Rule<List<ExpressionNode>> arguments = argumentList();
        
        return then(
            sequence(OnError.FINISH,
                guard(symbol("[")),
                arguments,
                symbol("]")
            ),
            new ParseAction<RuleValues, PartialCallExpression>() {
                @Override
                public PartialCallExpression apply(final RuleValues result) {
                    return new PartialCallExpression() {
                        @Override
                        public ExpressionNode complete(ExpressionNode expression) {
                            return new TypeApplicationNode(expression, result.get(arguments));
                        }
                    };
                }
            }
        );
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
        final Rule<ExpressionNode> returnType;
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
        final Rule<Option<ExpressionNode>> returnType;
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
        final Rule<?> comma = sequence(OnError.FINISH, optional(whitespace()), guard(symbol(",")), optional(whitespace()));
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
        final Rule<ExpressionNode> type = typeSpecifier();
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
    
    private static Rule<List<ExpressionNode>> argumentList() {
        return zeroOrMoreWithSeparator(expression(), softSeparator(COMMA));
    }
}
