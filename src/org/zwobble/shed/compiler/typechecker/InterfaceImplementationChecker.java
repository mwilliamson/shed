package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.typechecker.errors.MissingMemberError;
import org.zwobble.shed.compiler.typechecker.errors.WrongMemberTypeError;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.Member;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;

public class InterfaceImplementationChecker {
    private final SubTyping subTyping;
    private final TypeLookup typeLookup;
    private final StaticContext context;

    @Inject
    public InterfaceImplementationChecker(SubTyping subTyping, TypeLookup typeLookup, StaticContext context) {
        this.subTyping = subTyping;
        this.typeLookup = typeLookup;
        this.context = context;
    }

    public HasErrors checkInterfaces(SyntaxNode declaration, ScalarType type) {
        TypeResultBuilder<?> resultBuilder = typeResultBuilder();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        for (ScalarType superType : typeInfo.getInterfaces()) {
            ScalarTypeInfo superTypeInfo = context.getInfo(superType);
            
            for (Member member : superTypeInfo.getMembers()) {
                resultBuilder.addErrors(checkInterfaceMember(declaration, typeInfo, superType, member));
            }
        }
        return resultBuilder.build();
    }

    public Interfaces dereferenceInterfaces(List<ExpressionNode> interfaces) {
        return interfaces(transform(interfaces, lookupType()));
    }
    
    private TypeResult<?> checkInterfaceMember(SyntaxNode declaration, ScalarTypeInfo typeInfo, Type superType, Member member) {
        String memberName = member.getName();
        Members classMembers = typeInfo.getMembers();
        Option<Member> actualMemberOption = classMembers.lookup(memberName);
        if (!actualMemberOption.hasValue()) {
            return TypeResult.failure(new CompilerErrorWithSyntaxNode(declaration, new MissingMemberError(superType, memberName)));
        }
        Type expectedMemberType = member.getType();
        Type actualMemberType = actualMemberOption.get().getType();
        if (!subTyping.isSubType(actualMemberType, expectedMemberType)) {
            CompilerErrorDescription description = new WrongMemberTypeError(superType, memberName, expectedMemberType, actualMemberType);
            return TypeResult.failure(new CompilerErrorWithSyntaxNode(declaration, description));
        }
        
        return TypeResult.success();
    }

    private Function<ExpressionNode, ScalarType> lookupType() {
        return new Function<ExpressionNode, ScalarType>() {
            @Override
            public ScalarType apply(ExpressionNode input) {
                TypeResult<Type> lookupResult = typeLookup.lookupTypeReference(input);
                if (!lookupResult.isSuccess()) {
                    // TODO:
                    throw new RuntimeException("Failed type lookup " + lookupResult.getErrors());
                }
                // TODO: handle non-scalar types
                return (ScalarType)lookupResult.get();
            }
        };
    }
}
