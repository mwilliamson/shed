package org.zwobble.shed.compiler.nodetest;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.nodetest.NodeTesting.isSuccessWithOutput;

public class NodeTests {
    @Test
    public void canCalculateFibonacciNumbersAndPrintResult() {
        assertThat(execute("fibonacci", "main"), isSuccessWithOutput("55"));
    }
    
    private NodeExecutionResult execute(String directory, String main) {
        return Runtime.getRuntime().exec("node");;
    }
}
