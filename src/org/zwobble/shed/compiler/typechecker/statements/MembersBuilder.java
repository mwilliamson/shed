package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.Members;

import com.google.common.collect.ImmutableMap;

import static org.zwobble.shed.compiler.types.Members.members;

public class MembersBuilder {
    private final StaticContext context;

    @Inject
    public MembersBuilder(StaticContext context) {
        this.context = context;
    }
    
    public Members buildMembers(Iterable<? extends DeclarationNode> declarations) {
        ImmutableMap.Builder<String, ValueInfo> members = ImmutableMap.builder();
        
        for (DeclarationNode memberDeclaration : declarations) {
            Option<ValueInfo> memberType = findMemberType(memberDeclaration);
            if (memberType.hasValue()) {
                members.put(memberDeclaration.getIdentifier(), memberType.get());
            }
        }
        return members(members.build());
    }

    private Option<ValueInfo> findMemberType(DeclarationNode memberDeclaration) {
        return context.getValueInfoFor(memberDeclaration);
    }
}
