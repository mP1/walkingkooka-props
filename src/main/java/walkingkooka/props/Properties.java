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