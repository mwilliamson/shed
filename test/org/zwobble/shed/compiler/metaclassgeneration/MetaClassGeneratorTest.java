package org.zwobble.shed.compiler.metaclassgeneration;

import org.junit.Test;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarType;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.TypeMatchers.classTypeWithName;

public class MetaClassGeneratorTest {
    @Test public void
    metaClassesAreGeneratedForEachType() {
        ClassType type = new ClassType(fullyQualifiedName("shed", "Song"));
        MetaClasses metaClasses = generate(types(type));
        assertThat(metaClasses.metaClassOf(type), is(classTypeWithName(fullyQualifiedName("shed", "Song", "$Meta"))));
    }
    
    @Test public void
    typeCanBeRetrivedByMetaClass() {
        ScalarType type = new ClassType(fullyQualifiedName("shed", "Song"));
        MetaClasses metaClasses = generate(types(type));
        ClassType metaClass = metaClasses.metaClassOf(type);
        assertThat(metaClasses.getTypeFromMetaClass(metaClass), is(type));
    }
    
    private Iterable<ScalarType> types(ScalarType... types) {
        return asList(types);
    }
    
    private MetaClasses generate(Iterable<ScalarType> types) {
        return new MetaClassGenerator().generate(types);
    }
}
