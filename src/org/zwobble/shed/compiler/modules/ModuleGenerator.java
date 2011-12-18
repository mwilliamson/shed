package org.zwobble.shed.compiler.modules;

import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.typechecker.TypeResultWithValue;
import org.zwobble.shed.compiler.typechecker.TypeResults;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.singleton;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.util.ShedIterables.firstOrNone;

public class ModuleGenerator {
    public TypeResultWithValue<Modules> generateModules(Iterable<SourceNode> sourceNodes) {
        TypeResultWithValue<List<Option<Module>>> moduleResults = TypeResultWithValue.combine(transform(sourceNodes, sourceToModule()));
        return moduleResults.map(flatten());
    }

    private TypeResultWithValue<Option<Module>> toModule(SourceNode sourceNode) {
        List<String> packageNames = sourceNode.getPackageDeclaration().getPackageNames();
        Iterable<PublicDeclarationNode> publicDeclarations = filter(sourceNode.getStatements(), PublicDeclarationNode.class);
        Iterable<CompilerError> errors = transform(skip(publicDeclarations, 1), toError());
        Option<PublicDeclarationNode> publicDeclaration = firstOrNone(publicDeclarations);
        Option<Module> module = publicDeclaration.map(publicDeclarationToModule(packageNames));
        return TypeResults.build(module, errors);
    }

    private Module toModule(PublicDeclarationNode publicDeclaration, List<String> packageNames) {
        DeclarationNode declaration = publicDeclaration.getDeclaration();
        String identifier = declaration.getIdentifier();
        FullyQualifiedName name = fullyQualifiedName(ImmutableList.copyOf(concat(packageNames, singleton(identifier))));
        return Module.create(name, declaration);
    }

    private Function<SourceNode, TypeResultWithValue<Option<Module>>> sourceToModule() {
        return new Function<SourceNode, TypeResultWithValue<Option<Module>>>() {
            @Override
            public TypeResultWithValue<Option<Module>> apply(SourceNode input) {
                return toModule(input);
            }
        };
    }

    private Function<PublicDeclarationNode, Module> publicDeclarationToModule(final List<String> packageNames) {
        return new Function<PublicDeclarationNode, Module>() {
            @Override
            public Module apply(PublicDeclarationNode input) {
                return toModule(input, packageNames);
            }
        };
    }
    
    private Function<PublicDeclarationNode, CompilerError> toError() {
        return new Function<PublicDeclarationNode, CompilerError>() {
            @Override
            public CompilerError apply(PublicDeclarationNode input) {
                return new CompilerErrorWithSyntaxNode(input, new MultiplePublicDeclarationsInModuleError());
            }
        };
    }
    
    private Function<List<Option<Module>>, Modules> flatten() {
        return new Function<List<Option<Module>>, Modules>() {
            @Override
            public Modules apply(List<Option<Module>> input) {
                return Modules.build(concat(input));
            }
        };
    }
}
