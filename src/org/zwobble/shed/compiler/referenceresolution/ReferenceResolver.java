package org.zwobble.shed.compiler.referenceresolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes;
import org.zwobble.shed.compiler.referenceresolution.Scope.NotInScope;
import org.zwobble.shed.compiler.referenceresolution.Scope.Result;
import org.zwobble.shed.compiler.referenceresolution.Scope.Success;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.CompilerErrors.error;

public class ReferenceResolver {
    public ReferenceResolverResult resolveReferences(SyntaxNode node, Map<String, GlobalDeclarationNode> globalDeclarations) {
        ReferencesBuilder references = new ReferencesBuilder();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        SubScope scope = scopeFor(ScopedNodes.subScope(asList(node)), new SubScope(new TopScope(globalDeclarations)), errors);
        resolveReferences(node, references, scope, errors);
        return ReferenceResolverResult.build(references.build(), errors);
    }

    private void resolveReferences(SyntaxNode node, ReferencesBuilder references, SubScope scope, List<CompilerError> errors) {
        if (node instanceof VariableIdentifierNode) {
            VariableIdentifierNode variableIdentifier = (VariableIdentifierNode) node;
            Result lookupResult = scope.lookup(variableIdentifier.getIdentifier());
            if (lookupResult instanceof NotInScope) {
                errors.add(error(node, new VariableNotInScopeError(variableIdentifier.getIdentifier())));
            } else {
                references.addReference(variableIdentifier, ((Success)lookupResult).getNode());                
            }
        }
        
        for (ScopedNodes nodes : node.describeStructure().getChildren()) {
            SubScope scope1 = scopeFor(nodes, scope, errors);
            for (SyntaxNode childNode : nodes) {
                resolveReferences(childNode, references, scope1, errors);
            }
        }
    }

    private SubScope scopeFor(ScopedNodes nodes, SubScope currentScope, List<CompilerError> errors) {
        if (nodes.getScope() == ScopedNodes.Scope.SAME) {
            return currentScope;
        } else if (nodes.getScope() == ScopedNodes.Scope.EXTENDED_SCOPE) {
            SubScope extendedScope = currentScope.extend();
            findDeclarationsInSameScope(nodes, extendedScope, errors);
            return extendedScope;
        } else {
            SubScope subScope = new SubScope(currentScope);
            findDeclarationsInSameScope(nodes, subScope, errors);
            return subScope; 
        }
    }

    private void findDeclarationsInSameScope(ScopedNodes nodes, SubScope scope, List<CompilerError> errors) {
        for (SyntaxNode node : nodes) {
            if (node instanceof DeclarationNode) {
                DeclarationNode declaration = (DeclarationNode)node;
                if (scope.isDeclaredInCurrentScope(declaration.getIdentifier())) {
                    errors.add(error(declaration, new DuplicateIdentifierError(declaration.getIdentifier())));
                } else {
                    scope.add(declaration.getIdentifier(), declaration);
                }
            }
            for (ScopedNodes subNodes : node.describeStructure().getChildren()) {
                if (subNodes.getScope() == ScopedNodes.Scope.SAME) {
                    findDeclarationsInSameScope(subNodes, scope, errors);
                }
            }
        }
    }
}
