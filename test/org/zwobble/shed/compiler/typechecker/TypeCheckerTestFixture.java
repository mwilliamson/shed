package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.types.CoreTypes;

public class TypeCheckerTestFixture {
    public static VariableIdentifierNode STRING_TYPE_REFERENCE = Nodes.id("String");
    public static Declaration STRING_TYPE_DECLARATION = new GlobalDeclarationNode("String");
    
    public static VariableIdentifierNode UNIT_TYPE_REFERENCE = Nodes.id("Unit");
    public static Declaration UNIT_TYPE_DECLARATION = new GlobalDeclarationNode("Unit");
    
    public static TypeCheckerTestFixture build() {
        return new TypeCheckerTestFixture();
    }
    
    private final FullyQualifiedNamesBuilder fullNames = new FullyQualifiedNamesBuilder();
    private final StaticContext context;
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    private TypeCheckerTestFixture() {
        references.addReference(STRING_TYPE_REFERENCE, STRING_TYPE_DECLARATION);
        references.addReference(UNIT_TYPE_REFERENCE, UNIT_TYPE_DECLARATION);
        context = new StaticContext();
        context.add(STRING_TYPE_DECLARATION, ValueInfo.unassignableValue(CoreTypes.classOf(CoreTypes.STRING)));
        context.add(UNIT_TYPE_DECLARATION, ValueInfo.unassignableValue(CoreTypes.classOf(CoreTypes.UNIT)));
    }

    public <T> T get(Class<T> clazz) {
        return TypeCheckerInjector.build(fullNames.build(), context, references.build()).getInstance(clazz);
    }
    
    public StaticContext context() {
        return context;
    }
    
    public void addFullyQualifiedName(TypeDeclarationNode node, FullyQualifiedName name) {
        fullNames.addFullyQualifiedName(node, name);
    }
}
