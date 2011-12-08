package org.zwobble.shed.compiler.typechecker.expressions;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.errors.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.NotScalarTypeError;
import org.zwobble.shed.compiler.types.Member;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.errors.CompilerErrors.error;

import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

public class MemberAccessTypeInferer implements ExpressionTypeInferer<MemberAccessNode> {
    private final TypeInferer typeInferer;
    private final StaticContext context;

    @Inject
    public MemberAccessTypeInferer(TypeInferer typeInferer, StaticContext context) {
        this.typeInferer = typeInferer;
        this.context = context;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(final MemberAccessNode memberAccess) {
        return typeInferer.inferType(memberAccess.getExpression()).ifValueThen(new Function<Type, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(Type leftType) {
                String name = memberAccess.getMemberName();
                if (!(leftType instanceof ScalarType)) {
                    return failure(new CompilerErrorWithSyntaxNode(memberAccess, new NotScalarTypeError(leftType)));
                }
                ScalarTypeInfo leftTypeInfo = context.getInfo((ScalarType)leftType);
                Members members = leftTypeInfo.getMembers();
                Option<Member> member = members.lookup(name);
                
                if (member.hasValue()) {
                    return success(member.get().getValueInfo());
                } else {
                    return failure(error(memberAccess, ("No such member: " + name)));
                }
            }
        });
    }

}
