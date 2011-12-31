package org.zwobble.shed.compiler.sourceordering;

import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.modules.Module;
import org.zwobble.shed.compiler.modules.SourceModule;
import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.parsing.nodes.EntireSourceNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;

public class SourceOrdererTest {
    private final PackageDeclarationNode packageDeclaration = Nodes.packageDeclaration("shed", "example");
    private final List<String> noImports = Collections.emptyList();
    private final Option<String> noName = Option.none();
    private final List<Module> modules = Lists.newArrayList();
    
    @Test public void
    singleSourceWithNoImportsIsNotReordered() {
        SourceNode source = source(noImports, noName);
        assertThat(reorder(Nodes.sources(source)), isOrder(source));
    }
    
    @Test public void
    twoSourcesAreReorderedToSatisfyDependencies() {
        SourceNode dependent = source(asList("Song"), noName);
        SourceNode dependency = source(noImports, some("Song"));
        assertThat(reorder(Nodes.sources(dependent, dependency)), isOrder(dependency, dependent));
    }
    
    @Test public void
    errorIsRaisedIfSourcesHaveCircularDependency() {
        SourceNode dependent = source(asList("Song"), some("Artist"));
        SourceNode dependency = source(asList("Artist"), some("Song"));
        assertThat(
            reorder(Nodes.sources(dependent, dependency)),
            isFailureWithErrors(
                CircularModuleDependency.create(asList(
                    fullyQualifiedName("shed", "example", "Artist"),
                    fullyQualifiedName("shed", "example", "Song")
                ))
            )
        );
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Matcher<TypeResult<? extends Iterable<SourceNode>>> isOrder(SourceNode... sources) {
        return (Matcher)isSuccessWithValue(contains(sources));
    }

    private TypeResult<EntireSourceNode> reorder(EntireSourceNode sourceNodes) {
        return new SourceOrderer(Modules.build(modules)).reorder(sourceNodes);
    }
    
    private SourceNode source(List<String> imports, Option<String> name) {
        if (name.hasValue()) {
            VariableDeclarationNode declaration = Nodes.immutableVar(name.get(), Nodes.unit());
            StatementNode publicDeclaration = Nodes.publik(declaration);
            SourceNode source = Nodes.source(packageDeclaration, Lists.transform(imports, toImportNode()), asList(publicDeclaration));
            modules.add(SourceModule.create(source));
            return source;
        } else {
            return Nodes.source(packageDeclaration, Lists.transform(imports, toImportNode()), Collections.<StatementNode>emptyList());
        }
    }

    private Function<String, ImportNode> toImportNode() {
        return new Function<String, ImportNode>() {
            @Override
            public ImportNode apply(String input) {
                return Nodes.importNode("shed", "example", input);
            }
        };
    }
}
