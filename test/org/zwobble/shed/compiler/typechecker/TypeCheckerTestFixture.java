package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;

import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;

public class TypeCheckerTestFixture {
    private static VariableIdentifierNode STRING_TYPE_REFERENCE = Nodes.id("String");
    private static Declaration STRING_TYPE_DECLARATION = globalDeclaration("String");
    
    private static VariableIdentifierNode UNIT_TYPE_REFERENCE = Nodes.id("Unit");
    private static Declaration UNIT_TYPE_DECLARATION = globalDeclaration("Unit");
    
    private static VariableIdentifierNode BOOLEAN_TYPE_REFERENCE = Nodes.id("Boolean");
    private static Declaration BOOLEAN_TYPE_DECLARATION = globalDeclaration("Boolean");
    
    private static VariableIdentifierNode DOUBLE_TYPE_REFERENCE = Nodes.id("Double");
    private static Declaration DOUBLE_TYPE_DECLARATION = globalDeclaration("Double");
    
    public static TypeCheckerTestFixture build() {
        return new TypeCheckerTestFixture();
    }
    
    private final FullyQualifiedNamesBuilder fullNames = new FullyQualifiedNamesBuilder();
    private final StaticContext context;
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    private TypeCheckerTestFixture() {
        references.addReference(STRING_TYPE_REFERENCE, STRING_TYPE_DECLARATION);
        references.addReference(UNIT_TYPE_REFERENCE, UNIT_TYPE_DECLARATION);
        references.addReference(BOOLEAN_TYPE_REFERENCE, BOOLEAN_TYPE_DECLARATION);
        references.addReference(DOUBLE_TYPE_REFERENCE, DOUBLE_TYPE_DECLARATION);
        context = new StaticContext();
        context.addClass(STRING_TYPE_DECLARATION, CoreTypes.STRING, ScalarTypeInfo.EMPTY);
        context.addClass(UNIT_TYPE_DECLARATION, CoreTypes.UNIT, ScalarTypeInfo.EMPTY);
        context.addClass(BOOLEAN_TYPE_DECLARATION, CoreTypes.BOOLEAN, ScalarTypeInfo.EMPTY);
        context.addClass(DOUBLE_TYPE_DECLARATION, CoreTypes.DOUBLE, ScalarTypeInfo.EMPTY);
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
    
    public void addReference(VariableIdentifierNode reference, Declaration declaration) {
        references.addReference(reference, declaration);
    }
    
    public VariableIdentifierNode stringTypeReference() {
        return STRING_TYPE_REFERENCE;
    }
    
    public Declaration stringTypeDeclaration() {
        return STRING_TYPE_DECLARATION;
    }
    
    public VariableIdentifierNode unitTypeReference() {
        return UNIT_TYPE_REFERENCE;
    }
    
    public Declaration unitTypeDeclaration() {
        return UNIT_TYPE_DECLARATION;
    }
    
    public VariableIdentifierNode booleanTypeReference() {
        return BOOLEAN_TYPE_REFERENCE;
    }
    
    public Declaration booleanTypeDeclaration() {
        return BOOLEAN_TYPE_DECLARATION;
    }
    
    public VariableIdentifierNode doubleTypeReference() {
        return DOUBLE_TYPE_REFERENCE;
    }
    
    public Declaration doubleTypeDeclaration() {
        return DOUBLE_TYPE_DECLARATION;
    }
}
