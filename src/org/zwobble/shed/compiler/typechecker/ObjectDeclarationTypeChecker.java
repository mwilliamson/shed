package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ObjectDeclarationTypeChecker implements StatementTypeChecker<ObjectDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;

    public ObjectDeclarationTypeChecker(BlockTypeChecker blockTypeChecker) {
        this.blockTypeChecker = blockTypeChecker;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        ObjectDeclarationNode objectDeclaration, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        TypeResult<StatementTypeCheckResult> result = TypeResult.success(StatementTypeCheckResult.noReturn());

        TypeResult<StatementTypeCheckResult> blockResult = 
            blockTypeChecker.typeCheckBlock(objectDeclaration.getStatements(), nodeLocations, context, Option.<Type>none());
        result = result.withErrorsFrom(blockResult);
        
        if (result.isSuccess()) {
            Builder<String, ValueInfo> typeBuilder = ImmutableMap.builder();

            for (StatementNode statement : objectDeclaration.getStatements()) {
                if (statement instanceof PublicDeclarationNode) {
                    DeclarationNode declaration = ((PublicDeclarationNode) statement).getDeclaration();
                    typeBuilder.put(declaration.getIdentifier(), context.get(declaration).getValueInfo());
                }
            }
            
            InterfaceType type = new InterfaceType(context.fullyQualifiedNameOf(objectDeclaration), typeBuilder.build());

            context.add(objectDeclaration, unassignableValue(type));
        }
        
        return result;
    }
    
}
