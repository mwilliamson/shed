package org.zwobble.shed.compiler.tokeniser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.ShedSymbols;
import org.zwobble.shed.compiler.parsing.SourcePosition;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.PeekingIterator;

import static org.zwobble.shed.compiler.tokeniser.Token.token;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterators.peekingIterator;
import static com.google.common.collect.Lists.charactersOf;
import static java.util.Arrays.asList;

public class Tokeniser {
    private static final List<String> symbols = asList(
        "=>", "`", "¬", "!", "£", "$", "%", "^", "&", "*", "(", ")", "-",
        "=", "+", "[", "]", "{", "}", ";", ":", "'", "@", "#", "~", "<",
        ">", ",", ".", "/", "?", "\\", "|"
    );
    private static final char stringDelimiter = '"';
    private static final char newLine = '\n';
    private static final Map<Character, Character> escapeCharacters = ImmutableMap.<Character, Character>builder()
        .put('"', '"')
        .put('b', '\b')
        .put('t', '\t')
        .put('n', '\n')
        .put('f', '\f')
        .put('r', '\r')
        .put('\'', '\'')
        .put('\\', '\\')
        .build();
    
    public List<TokenPosition> tokenise(String inputString) {
        List<TokenPosition> tokens = new ArrayList<TokenPosition>();
        TokenType previousTokenType = null;
        InputStringIterator characters = new InputStringIterator(inputString);
        while (characters.hasNext()) {
            SourcePosition tokenStart = new SourcePosition(characters.currentLineNumber(), characters.currentCharacterNumber());
            Token token = nextToken(characters, previousTokenType);
            tokens.add(new TokenPosition(tokenStart, token));
            previousTokenType = token.getType();
        }
        tokens.add(new TokenPosition(new SourcePosition(characters.currentLineNumber(), characters.currentCharacterNumber()), Token.end()));
        return tokens;
    }
    
    private Token nextToken(PeekingIterator<Character> characters, TokenType previousTokenType) {
        Character firstCharacter = characters.peek();
        
        if (isWhitespace().apply(firstCharacter)) {
            return token(TokenType.WHITESPACE, takeWhile(characters, isWhitespace()));
        } else if (symbols.contains(firstCharacter.toString())) {
            characters.next();
            if (characters.hasNext() && symbols.contains(firstCharacter.toString() + characters.peek().toString())) {
                return token(TokenType.SYMBOL, firstCharacter.toString() + characters.next().toString());
            }
            return token(TokenType.SYMBOL, firstCharacter.toString());
        } else if (isDigit().apply(firstCharacter)) {
            return token(TokenType.NUMBER, takeWhile(characters, isDigit()));
        } else if (previousTokenType == TokenType.NUMBER) {
            return token(TokenType.ERROR, takeWhile(characters, isIdentifierCharacter()));
        } else if (firstCharacter == stringDelimiter) {
            TokenType type = TokenType.STRING;
            StringBuilder original = new StringBuilder();
            original.append(characters.next());
            StringBuilder value = new StringBuilder();
            while (characters.hasNext() && characters.peek() != newLine && characters.peek() != stringDelimiter) {
                char next = characters.next();
                original.append(next);
                if (next == '\\') {
                    char escapeCharacter = characters.next();
                    original.append(escapeCharacter);
                    if (escapeCharacters.containsKey(escapeCharacter)) {
                        value.append(escapeCharacters.get(escapeCharacter));
                    } else {
                        type = TokenType.STRING_WITH_INVALID_ESCAPE_CODES;
                    }
                } else {
                    value.append(next);
                }
            }
            if (!characters.hasNext() || characters.peek() == newLine) {
                return new Token(TokenType.UNTERMINATED_STRING, value.toString(), original.toString());
            }
            original.append(characters.next());
            return new Token(type, value.toString(), original.toString());
        } else {
            String value = takeWhile(characters, isIdentifierCharacter());
            if (isKeyword(value)) {
                return token(TokenType.KEYWORD, value);
            } else if (value.startsWith(ShedSymbols.INTERNAL_PREFIX)) {
                return token(TokenType.ERROR, value);
            } else {
                return token(TokenType.IDENTIFIER, value);
            }
        }
    }

    private String takeWhile(PeekingIterator<Character> characters, Predicate<Character> predicate) {
        StringBuilder result = new StringBuilder();
        while (characters.hasNext() && predicate.apply(characters.peek())) {
            result.append(characters.next());
        }
        return result.toString();
    }

    private Predicate<Character> isDigit() {
        return new Predicate<Character>() {
            @Override
            public boolean apply(Character input) {
                return Character.isDigit(input);
            }
        };
    }

    private Predicate<Character> isWhitespace() {
        return new Predicate<Character>() {
            @Override
            public boolean apply(Character input) {
                return Character.isWhitespace(input);
            }
        };
    }
    
    private Predicate<Character> isIdentifierCharacter() {
        return not(or(isWhitespace(), isSymbolCharacter()));
    }

    private Predicate<Character> isSymbolCharacter() {
        return new Predicate<Character>() {
            @Override
            public boolean apply(Character input) {
                return symbols.contains(input.toString());
            }
        };
    }

    private boolean isKeyword(String value) {
        for (Keyword keyword : Keyword.values()) {
            if (value.equals(keyword.keywordName())) {
                return true;
            }
        }
        return false;
    }
    
    private class InputStringIterator implements PeekingIterator<Character> {
        private final PeekingIterator<Character> characters;
        private int lineNumber = 1;
        private int characterNumber = 1;

        public InputStringIterator(String inputString) {
            characters = peekingIterator(charactersOf(inputString).iterator());
        }
        
        @Override
        public boolean hasNext() {
            return characters.hasNext();
        }

        @Override
        public Character peek() {
            return characters.peek();
        }

        @Override
        public Character next() {
            Character character = characters.next();
            if (character == '\n') {
                lineNumber += 1;
                characterNumber = 1;
            } else {
                characterNumber += 1;
            }
            return character;
        }

        @Override
        public void remove() {
            characters.remove();
        }
        
        public int currentLineNumber() {
            return lineNumber;
        }
        
        public int currentCharacterNumber() {
            return characterNumber;
        }
    }
}
