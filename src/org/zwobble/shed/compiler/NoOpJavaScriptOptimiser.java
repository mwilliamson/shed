package org.zwobble.shed.compiler;

public class NoOpJavaScriptOptimiser implements JavaScriptOptimiser {
    @Override
    public String optimise(String javaScript) {
        return javaScript;
    }
}
