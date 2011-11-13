package org.zwobble.shed.compiler.typechecker;

import java.util.Map;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;

public class TypeCheckerTestFixture {
    private static VariableIdentifierNode STRING_TYPE_REFERENCE = Nodes.id("String");
    private static VariableIdentifierNode UNIT_TYPE_REFERENCE = Nodes.id("Unit");
    private static VariableIdentifierNode BOOLEAN_TYPE_REFERENCE = Nodes.id("Boolean");
    private static VariableIdentifierNode DOUBLE_TYPE_REFERENCE = Nodes.id("Double");
    
    public static TypeCheckerTestFixture build() {
        return new TypeCheckerTestFixture();
    }
    
    private final FullyQualifiedNamesBuilder fullNames = new FullyQualifiedNamesBuilder();
    private final StaticContext context;
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    private final Declaration stringTypeDeclaration;
    private final Declaration unitTypeDeclaration;
    private final Declaration booleanTypeDeclaration;
    private final Declaration doubleTypeDeclaration;
    
    private TypeCheckerTestFixture() {
        context = DefaultContext.defaultContext();
        
        Map<String, GlobalDeclaration> builtIns = context.getBuiltIns();
        stringTypeDeclaration = builtIns.get("String");
        unitTypeDeclaration = builtIns.get("Unit");
        booleanTypeDeclaration = builtIns.get("Boolean");
        doubleTypeDeclaration = builtIns.get("Double");
        
        references.addReference(STRING_TYPE_REFERENCE, stringTypeDeclaration);
        references.addReference(UNIT_TYPE_REFERENCE, unitTypeDeclaration);
        references.addReference(BOOLEAN_TYPE_REFERENCE, booleanTypeDeclaration);
        references.addReference(DOUBLE_TYPE_REFERENCE, doubleTypeDeclaration);
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
        return stringTypeDeclaration;
    }
    
    public VariableIdentifierNode unitTypeReference() {
        return UNIT_TYPE_REFERENCE;
    }
    
    public Declaration unitTypeDeclaration() {
        return unitTypeDeclaration;
    }
    
    public VariableIdentifierNode booleanTypeReference() {
        return BOOLEAN_TYPE_REFERENCE;
    }
    
    public Declaration booleanTypeDeclaration() {
        return booleanTypeDeclaration;
    }
    
    public VariableIdentifierNode doubleTypeReference() {
        return DOUBLE_TYPE_REFERENCE;
    }
    
    public Declaration doubleTypeDeclaration() {
        return doubleTypeDeclaration;
    }
}
