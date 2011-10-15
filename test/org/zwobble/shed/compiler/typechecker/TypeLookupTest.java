package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import com.google.inject.Injector;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class TypeLookupTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();

    private final GlobalDeclarationNode stringDeclaration = new GlobalDeclarationNode("String");
    private final VariableIdentifierNode stringReference = new VariableIdentifierNode("String");
    
    @Test public void
    canLookupTypesFromContext() {
        TypeResult<Type> result = lookupTypeReference(stringReference, standardContext());
        assertThat(result, is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceAFunctionTypeInCurrentContext() {
        VariableIdentifierNode reference = Nodes.id("length");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("length");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        context.add(declaration, unassignableValue(CoreTypes.NUMBER));
        
        TypeResult<Type> result = lookupTypeReference(reference, context);
        assertThat(errorStrings(result), is(asList("Not a type but an instance of \"Number\"")));
    }
    
    @Test public void
    errorIncludesNodeLocation() {
        VariableIdentifierNode reference = Nodes.id("length");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("length");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        context.add(declaration, unassignableValue(CoreTypes.NUMBER));
        
        nodeLocations.put(reference, range(position(3, 5), position(7, 4)));
        TypeResult<Type> result = lookupTypeReference(reference, context);
        assertThat(
            result.getErrors(),
            is((Object)asList(CompilerError.error(range(position(3, 5), position(7, 4)), "Not a type but an instance of \"Number\"")))
        );
    }
    
    private TypeResult<Type> lookupTypeReference(ExpressionNode typeReference, StaticContext context) {
        Injector injector = TypeCheckerInjector.build(nodeLocations, new FullyQualifiedNamesBuilder().build());
        TypeLookup typeLookup = injector.getInstance(TypeLookup.class);
        return typeLookup.lookupTypeReference(typeReference, context);
    }
    
    private StaticContext standardContext() {
        references.addReference(stringReference, stringDeclaration);
        
        StaticContext context = new StaticContext(references.build());
        context.add(stringDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.STRING)));
        
        return context;
    }
}
