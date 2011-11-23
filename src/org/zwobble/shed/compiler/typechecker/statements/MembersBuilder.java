package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;

import com.google.common.collect.ImmutableMap;

import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;

public class MembersBuilder {
    private final StaticContext context;

    @Inject
    public MembersBuilder(StaticContext context) {
        this.context = context;
    }
    
    public Map<String, ValueInfo> buildMembers(Iterable<? extends DeclarationNode> declarations) {
        ImmutableMap.Builder<String, ValueInfo> members = ImmutableMap.builder();
        
        for (DeclarationNode memberDeclaration : declarations) {
            Option<ValueInfo> memberType = findMemberType(memberDeclaration);
            if (memberType.hasValue()) {
                members.put(memberDeclaration.getIdentifier(), memberType.get());
            }
        }
        return members.build();
    }

    private Option<ValueInfo> findMemberType(DeclarationNode memberDeclaration) {
        VariableLookupResult result = context.get(memberDeclaration);
        if (result.getStatus() == Status.SUCCESS) {
            return some(result.getValueInfo());
        } else {
            return none();
        }
    }
}
