/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.props;

import walkingkooka.CanBeEmpty;
import walkingkooka.InvalidCharacterException;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.text.CharSequences;
import walkingkooka.text.CharacterConstant;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An immutable key/value store of {@link String values}.
 */
public final class Properties implements CanBeEmpty,
        TreePrintable {

    /**
     * An empty {@link Properties}.
     */
    public final static Properties EMPTY = new Properties(Maps.empty());

    /**
     * Package private ctor
     */
    Properties(final Map<PropertiesPath, String> pathToValue) {
        this.pathToValue = pathToValue;
    }

    /**
     * Fetches the string value for the given {@link PropertiesPath}.
     */
    public Optional<String> get(final PropertiesPath path) {
        return Optional.ofNullable(
                this.pathToValue.get(
                        check(path)
                )
        );
    }

    /**
     * Sets or replaces the string value for the given {@link PropertiesPath}, returning a {@link Properties} with the
     * change leaving the original unchanged
     */
    public Properties set(final PropertiesPath path,
                          final String value) {
        check(path);
        Objects.requireNonNull(value, "value");

        final Map<PropertiesPath, String> pathToValue = this.pathToValue;
        final Properties setOrReplaced;

        final Object old = pathToValue.get(path);
        if (value.equals(old)) {
            setOrReplaced = this;
        } else {
            final Map<PropertiesPath, String> copy = Maps.ordered();
            copy.putAll(pathToValue);
            copy.put(
                    path,
                    value
            );

            setOrReplaced = new Properties(copy);
        }

        return setOrReplaced;
    }

    /**
     * Removes the string value if one exists, for the given {@link PropertiesPath}, returning a {@link Properties} with the
     * change leaving the original unchanged
     */
    public Properties remove(final PropertiesPath path) {
        check(path);

        final Map<PropertiesPath, String> pathToValue = this.pathToValue;
        final Properties removed;

        if (pathToValue.containsKey(path)) {
            if (1 == pathToValue.size()) {
                removed = EMPTY;
            } else {
                final Map<PropertiesPath, String> copy = Maps.ordered();
                copy.putAll(pathToValue);
                copy.remove(path);

                removed = new Properties(copy);
            }
        } else {
            removed = this;
        }

        return removed;
    }

    private static PropertiesPath check(final PropertiesPath path) {
        return Objects.requireNonNull(path, "path");
    }

    /**
     * Read-only view of the entries in this properties object.
     */
    public Set<Entry<PropertiesPath, String>> entries() {
        return Sets.readOnly(
                this.pathToValue.entrySet()
        );
    }

    /**
     * Read-only view of the keys in this properties object.
     */
    public Set<PropertiesPath> keys() {
        return Sets.readOnly(this.pathToValue.keySet());
    }

    /**
     * Read-only view of the values in this properties object.
     */
    public Collection<String> values() {
        return Collections.unmodifiableCollection(
                this.pathToValue.values()
        );
    }

    /**
     * Returns the number of entries.
     */
    public int size() {
        return this.pathToValue.size();
    }

    /**
     * Returns true if this properties is empty.
     */
    @Override
    public boolean isEmpty() {
        return this.pathToValue.isEmpty();
    }

    // @VisibleForTesting
    final Map<PropertiesPath, String> pathToValue;

    // parse............................................................................................................

    /**
     * Parses the text assumed to be a multi line text file containing a *.properties following similar rules of
     * {@link java.util.Properties#load(Reader)}.
     * <br>
     * Note the major difference between the two is what is considered to be a valid key, {@see ProperiesPath}.
     * The rules about line continuation and escape characters should be identical.
     */
    public static Properties parse(final String text) {
        Objects.requireNonNull(text, "text");

        final int length = text.length();

        final int MODE_CHAR = 1;
        final int MODE_CHAR_BACKSPACE_ESCAPING = 2;
        final int MODE_CHAR_BACKSPACE_ESCAPING_CR = 3;
        final int MODE_CHAR_BACKSPACE_ESCAPING_NL = 4;
        final int MODE_CHAR_UNICODE_0 = 5;
        final int MODE_CHAR_UNICODE_1 = 6;
        final int MODE_CHAR_UNICODE_2 = 7;
        final int MODE_CHAR_UNICODE_3 = 8;

        int unicodeChar = 0;
        int charMode = MODE_CHAR;

        Properties properties = EMPTY;

        PropertiesPath key = null;
        String value = null;

        StringBuilder token = null;

        final int MODE_TOKEN = 1;
        final int MODE_TOKEN_COMMENT = 2;
        final int MODE_TOKEN_KEY = 3;
        final int MODE_TOKEN_VALUE = 4;

        int tokenMode = MODE_TOKEN;

        for (int i = 0; i < length; i++) {
            char nextChar = 0;

            {
                final char c = text.charAt(i);

                switch (charMode) {
                    case MODE_CHAR:
                        switch (c) {
                            case '\n':
                            case '\r':
                                // not in escape mode must be end of key/value
                                if (null != key) {
                                    if (null == value) {
                                        throw new InvalidCharacterException(
                                                text,
                                                i
                                        );
                                    }
                                    properties = properties.set(
                                            key,
                                            value + token.toString()
                                                    .trim()
                                    );

                                    key = null;
                                    value = null;
                                    tokenMode = MODE_TOKEN;
                                }
                                nextChar = c;
                                break;
                            case '\\':
                                charMode = MODE_CHAR_BACKSPACE_ESCAPING;
                                break;
                            default:
                                nextChar = c;
                                break;
                        }
                        break;
                    case MODE_CHAR_BACKSPACE_ESCAPING:
                        switch (c) {
                            case 'b':
                                nextChar = '\b';
                                charMode = MODE_CHAR;
                                break;
                            case 'f':
                                nextChar = '\f';
                                charMode = MODE_CHAR;
                                break;
                            case 'n':
                                nextChar = '\n';
                                charMode = MODE_CHAR;
                                break;
                            case 'r':
                                nextChar = '\r';
                                charMode = MODE_CHAR;
                                break;
                            case 't':
                                nextChar = '\t';
                                charMode = MODE_CHAR;
                                break;
                            case 'u':
                                charMode = MODE_CHAR_UNICODE_0;
                                break;
                            case '\\':
                                charMode = MODE_CHAR;
                                break;
                            case '\r':
                                value = value + token.toString()
                                        .trim();
                                token = new StringBuilder();
                                charMode = MODE_CHAR_BACKSPACE_ESCAPING_CR;
                                break;
                            case '\n':
                                value = value + token.toString()
                                        .trim();
                                token = new StringBuilder();
                                charMode = MODE_CHAR_BACKSPACE_ESCAPING_NL;
                                break;
                            default:
                                nextChar = c;
                                charMode = MODE_CHAR;
                                break;
                        }
                        break;
                    case MODE_CHAR_BACKSPACE_ESCAPING_CR:
                        switch (c) {
                            case '\n': // BACKSLASH CR NL
                                charMode = MODE_CHAR;
                                break;
                            default:
                                nextChar = c;
                                charMode = MODE_CHAR;
                                break;
                        }
                        break;
                    case MODE_CHAR_BACKSPACE_ESCAPING_NL:
                        switch (c) {
                            case '\r': // BACKSLASH CR NL
                                nextChar = '\r';
                                charMode = MODE_CHAR;
                                break;
                            default:
                                nextChar = c;
                                charMode = MODE_CHAR;
                                break;
                        }
                        break;
                    case MODE_CHAR_UNICODE_0:
                        unicodeChar = nextUnicodeDigit(
                                c,
                                i,
                                text,
                                unicodeChar
                        );
                        charMode = MODE_CHAR_UNICODE_1;
                        break;
                    case MODE_CHAR_UNICODE_1:
                        unicodeChar = nextUnicodeDigit(
                                c,
                                i,
                                text,
                                unicodeChar
                        );
                        charMode = MODE_CHAR_UNICODE_2;
                        break;
                    case MODE_CHAR_UNICODE_2:
                        unicodeChar = nextUnicodeDigit(
                                c,
                                i,
                                text,
                                unicodeChar
                        );
                        charMode = MODE_CHAR_UNICODE_3;
                        break;
                    case MODE_CHAR_UNICODE_3:
                        nextChar = (char) nextUnicodeDigit(
                                c,
                                i,
                                text,
                                unicodeChar
                        );
                        charMode = MODE_CHAR;
                        unicodeChar = 0;
                        break;
                    default:
                        throw new IllegalStateException("Invalid char mode " + charMode);
                }
            }

            switch (charMode) {
                case MODE_CHAR:
                    switch (tokenMode) {
                        case MODE_TOKEN:
                            if (isWhitespace(nextChar)) {
                                break;
                            }
                            if (isComment(nextChar)) {
                                tokenMode = MODE_TOKEN_COMMENT;
                                break;
                            }
                            // starting key!
                            token = new StringBuilder()
                                    .append(nextChar);
                            tokenMode = MODE_TOKEN_KEY;
                            break;
                        case MODE_TOKEN_COMMENT:
                            switch (nextChar) {
                                case '\n':
                                case '\r':
                                    tokenMode = MODE_TOKEN;
                                    break;
                                default:
                                    // ignore other comment chars
                                    break;
                            }
                            break;
                        case MODE_TOKEN_KEY:
                            switch (nextChar) {
                                case '=':
                                case ':':
                                    key = key(token.toString().trim());
                                    token = new StringBuilder();
                                    value = "";
                                    tokenMode = MODE_TOKEN_VALUE;
                                    break;
                                case '\n':
                                case '\r':
                                    // missing assignment and value
                                    throw new InvalidCharacterException(
                                            text,
                                            i
                                    );
                                default:
                                    token.append(nextChar);
                                    break;
                            }
                            break;
                        case MODE_TOKEN_VALUE:
                            switch (nextChar) {
                                case '\n':
                                case '\r':
                                    value = value + token.toString()
                                            .trim();
                                    token = new StringBuilder();
                                default:
                                    token.append(nextChar);
                                    break;
                            }
                            break;
                        default:
                            throw new IllegalStateException("Invalid token mode " + tokenMode);
                    }
                    break;
                default:
                    // other char modes character is incomplete continue reading chars.
            }
        }

        switch (tokenMode) {
            case MODE_TOKEN:
            case MODE_TOKEN_COMMENT:
                break;
            case MODE_TOKEN_KEY:
                throw new IllegalArgumentException("Missing assignment following key");
            case MODE_TOKEN_VALUE:
                properties = properties.set(
                        key,
                        value + token.toString()
                                .trim()
                );
                break;
            default:
                throw new IllegalStateException("Invalid tokenMode " + tokenMode);
        }

        return properties;
    }

    private static int nextUnicodeDigit(final char c,
                                        final int pos,
                                        final String text,
                                        final int unicode) {
        return unicode * 16 + digit(
                c,
                pos,
                text
        );
    }

    private static int digit(final char c,
                             final int pos,
                             final String text) {
        final int value;

        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                value = c - '0';
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                value = 10 + c - 'A';
                break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                value = 10 + c - 'a';
                break;
            default:
                throw new InvalidCharacterException(
                        text,
                        pos
                );
        }

        return value;
    }

    /**
     * Helper used to define what is considered whitespace within a properties text file.
     */
    private static boolean isWhitespace(final char c) {
        final boolean whitespace;

        switch (c) {
            case '\0':
            case '\b':
            case '\f':
            case '\n':
            case '\r':
            case '\t':
            case ' ':
                whitespace = true;
                break;
            default:
                whitespace = false;
                break;
        }

        return whitespace;
    }

    private static boolean isComment(final char c) {
        return '!' == c || '#' == c;
    }

    private static PropertiesPath key(final String text) {
        return PropertiesPath.parse(text);
    }

    // TreePrintable....................................................................................................

    private final static CharacterConstant SEPARATOR = CharacterConstant.with('=');

    @Override
    public void printTree(final IndentingPrinter printer) {
        Objects.requireNonNull(printer, "printer");

        for(final Entry<PropertiesPath, String> entries : this.entries()) {
            printer.lineStart();

            printer.print(
                    entries.getKey()
                            .value()
            );

            printer.print(SEPARATOR);
            printer.println(
                    CharSequences.escape(
                        entries.getValue()
                    )
            );
        }

        printer.lineStart();
    }

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.pathToValue.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof Properties && this.equals0((Properties) other);
    }

    private boolean equals0(final Properties properties) {
        return this.pathToValue.equals(properties.pathToValue);
    }

    @Override
    public String toString() {
        return this.pathToValue.toString();
    }

    // JsonNodeContext...................................................................................................

    /**
     * Factory that creates a {@link Properties} from a {@link JsonNode}.
     */
    static Properties unmarshall(final JsonNode node,
                                 final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        Properties properties = EMPTY;

        for (final JsonNode child : node.objectOrFail().children()) {
            properties = properties.set(
                    PropertiesPath.parse(
                            child.name()
                                    .value()
                    ),
                    child.stringOrFail()
            );
        }

        return properties;
    }

    /**
     * <pre>
     * {
     *   "key-1a": "value-1a",
     *   "key-2b": "value-2b"
     * }
     * </pre>
     */
    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .setChildren(
                        this.entries()
                                .stream()
                                .map(e -> JsonNode.string(
                                                e.getValue()
                                        ).setName(
                                                JsonPropertyName.with(
                                                        e.getKey()
                                                                .value()
                                                )
                                        )
                                )
                                .collect(Collectors.toList())
                );
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(Properties.class),
                Properties::unmarshall,
                Properties::marshall,
                Properties.class
        );
    }
}
