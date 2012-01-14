package org.zwobble.shed.compiler.nodetest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.shed.compiler.CompilationResult;
import org.zwobble.shed.compiler.nodejs.ShedToNodeJsCompiler;

import com.google.common.io.CharStreams;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.nodetest.NodeTesting.isSuccessWithOutput;

public class NodeTests {
    @Test
    public void canCalculateFibonacciNumbersAndPrintResult() {
        assertThat(compileAndExecute("fibonacci", "fib.main"), isSuccessWithOutput("55"));
    }
    
    @Test
    public void classesAndObjectsCanImplementAnInterface() {
        assertThat(compileAndExecute("interfaces", "interfacesTest.main"), isSuccessWithOutput("Bob\nBanana\n"));
    }
    
    @Test
    public void classesCanReferenceThemselves() {
        assertThat(compileAndExecute("self-references", "selfReferences.main"), isSuccessWithOutput("nothing to see here"));
    }
    
    @Test
    public void classesCanHaveCircularDependencies() {
        assertThat(compileAndExecute("circular-dependencies", "circularDependencies.main"), isSuccessWithOutput("No"));
    }
    
    @Test
    public void filesAreLoadedInDependencyOrder() {
        assertThat(compileAndExecute("multiple-files", "multipleFiles.main"), isSuccessWithOutput("A"));
    }
    
    @Ignore
    @Test
    public void canDeclareGenericFunctions() {
        assertThat(compileAndExecute("generic-functions", "genericFunctions.main"), isSuccessWithOutput("Blah"));
    }
    
    private NodeExecutionResult compileAndExecute(String directory, String main) {
        try {
            File compiledFile = compile(directory, main);
            String compiledFilePath = compiledFile.getAbsolutePath();
            return execute(compiledFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private File compile(String directory, String main) throws IOException {
        File compiledFile = File.createTempFile(main, ".js");

        CompilationResult result = ShedToNodeJsCompiler.compile(new File("node-test-files", directory), main);
        if (!result.isSuccess()) {
            throw new RuntimeException("Errors while compiling: " + result.errors());
        }
        FileWriter fileWriter = new FileWriter(compiledFile);
        fileWriter.write(result.output());
        fileWriter.flush();
        return compiledFile;
    }

    private NodeExecutionResult execute(String compiledFilePath) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[] {"node", compiledFilePath});
        String output = CharStreams.toString(new InputStreamReader(process.getInputStream()));
        String errorOutput = CharStreams.toString(new InputStreamReader(process.getErrorStream()));
        return new NodeExecutionResult(process.waitFor(), output, errorOutput);
    }
}
