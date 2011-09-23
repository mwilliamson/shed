package org.zwobble.shed.compiler.referenceresolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes;
import org.zwobble.shed.compiler.referenceresolution.Scope.NotDeclaredYet;
import org.zwobble.shed.compiler.referenceresolution.Scope.NotInScope;
import org.zwobble.shed.compiler.referenceresolution.Scope.Result;
import org.zwobble.shed.compiler.referenceresolution.Scope.Success;

import static java.util.Arrays.asList;

public class ReferenceResolver {
    public ReferenceResolverResult resolveReferences(SyntaxNode node, NodeLocations nodeLocations, Map<String, GlobalDeclarationNode> globalDeclarations) {
        ReferencesBuilder references = new ReferencesBuilder();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        resolveReferences(node, nodeLocations, references, new SubScope(new TopScope(globalDeclarations), findDeclarations(asList(node))), errors);
        return ReferenceResolverResult.build(references.build(), errors);
    }

    private void resolveReferences(SyntaxNode node, NodeLocations nodeLocations, ReferencesBuilder references, SubScope scope, List<CompilerError> errors) {
        addReferences(node, nodeLocations, references, scope, errors);
        addDeclarations(node, nodeLocations, scope, errors);
    }

    private SubScope scopeFor(ScopedNodes nodes, SubScope currentScope) {
        if (nodes.getScope() == ScopedNodes.Scope.SAME) {
            return currentScope;
        } else if (nodes.getScope() == ScopedNodes.Scope.EXTENDED_SCOPE) {
            return currentScope.extend(findDeclarations(nodes));
        } else {
            return new SubScope(currentScope, findDeclarations(nodes)); 
        }
    }

    private void addReferences(SyntaxNode node, NodeLocations nodeLocations,
        ReferencesBuilder references, SubScope currentScope, List<CompilerError> errors) {
        if (node instanceof VariableIdentifierNode) {
            VariableIdentifierNode variableIdentifier = (VariableIdentifierNode) node;
            Result lookupResult = currentScope.lookup(variableIdentifier.getIdentifier());
            if (lookupResult instanceof NotInScope) {
                errors.add(new CompilerError(nodeLocations.locate(node), new VariableNotInScopeError(variableIdentifier.getIdentifier())));
            } else if (lookupResult instanceof NotDeclaredYet) {
                errors.add(new CompilerError(nodeLocations.locate(node), new VariableNotDeclaredYetError(variableIdentifier.getIdentifier())));
            } else {
                references.addReference(variableIdentifier, ((Success)lookupResult).getNode());                
            }
            
        }

        for (ScopedNodes nodes : node.describeStructure().getChildren()) {
            SubScope scope = scopeFor(nodes, currentScope);
            for (SyntaxNode childNode : nodes) {
                resolveReferences(childNode, nodeLocations, references, scope, errors);
            }
        }
    }

    private void addDeclarations(SyntaxNode node, NodeLocations nodeLocations, SubScope scope, List<CompilerError> errors) {
        if (node instanceof DeclarationNode) {
            DeclarationNode declaration = (DeclarationNode)node;
            if (scope.isDeclaredInCurrentScope(declaration.getIdentifier())) {
                errors.add(new CompilerError(nodeLocations.locate(node), new DuplicateIdentifierError(declaration.getIdentifier())));
            } else {
                scope.add(declaration.getIdentifier(), declaration);
            }
        }
    }

    private Set<String> findDeclarations(Iterable<SyntaxNode> nodes) {
        Set<String> declarations = new HashSet<String>();
        for (SyntaxNode node : nodes) {
            if (node instanceof DeclarationNode) {
                declarations.add(((DeclarationNode) node).getIdentifier());
            }
            for (ScopedNodes subNodes : node.describeStructure().getChildren()) {
                if (subNodes.getScope() == ScopedNodes.Scope.SAME) {
                    declarations.addAll(findDeclarations(subNodes));
                }
            }
        }
        return declarations;
    }
}
