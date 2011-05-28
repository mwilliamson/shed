package org.zwobble.shed.parser.parsing;

import lombok.Data;

@Data
public class Error {
    private final int lineNumber;
    private final int characterNumber;
    private final String description;
}
