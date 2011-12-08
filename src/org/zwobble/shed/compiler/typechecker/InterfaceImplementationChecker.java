package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.errors.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.errors.HasErrors;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.typechecker.errors.MissingMemberError;
import org.zwobble.shed.compiler.typechecker.errors.WrongMemberTypeError;
import org.zwobble.shed.compiler.types.Member;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;

public class InterfaceImplementationChecker {
    private final SubTyping subTyping;
    private final StaticContext context;

    @Inject
    public InterfaceImplementationChecker(SubTyping subTyping, StaticContext context) {
        this.subTyping = subTyping;
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
    
    private TypeResult<?> checkInterfaceMember(SyntaxNode declaration, ScalarTypeInfo typeInfo, Type superType, Member member) {
        String memberName = member.getName();
        Members classMembers = typeInfo.getMembers();
        Option<Member> actualMemberOption = classMembers.lookup(memberName);
        if (!actualMemberOption.hasValue()) {
            return TypeResults.failure(new CompilerErrorWithSyntaxNode(declaration, new MissingMemberError(superType, memberName)));
        }
        Type expectedMemberType = member.getType();
        Type actualMemberType = actualMemberOption.get().getType();
        if (!subTyping.isSubType(actualMemberType, expectedMemberType)) {
            CompilerErrorDescription description = new WrongMemberTypeError(superType, memberName, expectedMemberType, actualMemberType);
            return TypeResults.failure(new CompilerErrorWithSyntaxNode(declaration, description));
        }
        
        return TypeResults.success();
    }
}
