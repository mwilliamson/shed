package org.zwobble.shed.compiler.nodetest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;
import org.zwobble.shed.compiler.nodejs.ShedToNodeJsCompiler;

import com.google.common.io.CharStreams;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.nodetest.NodeTesting.isSuccessWithOutput;

public class NodeTests {
    @Test
    public void canCalculateFibonacciNumbersAndPrintResult() {
        assertThat(compileAndExecute("fibonacci", "main"), isSuccessWithOutput("55"));
    }
    
    private NodeExecutionResult compileAndExecute(String directory, String main) {
        try {
            File compiledFile = compile(directory, main);
            String compiledFilePath = compiledFile.getAbsolutePath();
            return execute(compiledFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File compile(String directory, String main) throws IOException {
        File compiledFile = File.createTempFile(main, ".js");
        FileOutputStream outputStream = new FileOutputStream(compiledFile);

        Writer writer = new OutputStreamWriter(outputStream);
        writer.write(CharStreams.toString(new FileReader(new File("src/org/zwobble/shed/runtime/shed.node.js"))));
        ShedToNodeJsCompiler.compile(new File("src/org/zwobble/shed/runtime/stdlib"), main, writer);
        ShedToNodeJsCompiler.compile(new File("node-test-files", directory), main, writer);
        return compiledFile;
    }

    private NodeExecutionResult execute(String compiledFilePath) throws IOException {
        Process process = Runtime.getRuntime().exec(new String[] {"node", compiledFilePath});
        String output = CharStreams.toString(new InputStreamReader(process.getInputStream()));
        String errorOutput = CharStreams.toString(new InputStreamReader(process.getErrorStream()));
        return new NodeExecutionResult(process.exitValue(), output, errorOutput);
    }
}
