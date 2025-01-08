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


import walkingkooka.Cast;
import walkingkooka.naming.Name;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.CharSequences;

/**
 * A simple {@link Name} that accepts a {@link String} composed of any character.
 */
final public class PropertiesName implements Name,
    Comparable<PropertiesName> {

    /**
     * Root singleton
     */
    final static PropertiesName ROOT = new PropertiesName("");

    /**
     * Factory that creates a {@link PropertiesName}, only the root path has this name.
     */
    static PropertiesName with(final String name) {
        CharSequences.failIfNullOrEmpty(name, "name");
        if (-1 != name.indexOf(PropertiesPath.SEPARATOR.character())) {
            throw new IllegalArgumentException("Name " + CharSequences.quote(name) +
                " cannot contain " + CharSequences.quoteIfChars(PropertiesPath.SEPARATOR.character()));
        }

        return new PropertiesName(name);
    }

    private PropertiesName(final String name) {
        this.name = name;
    }

    @Override
    public String value() {
        return this.name;
    }

    private final String name;

    // Object...........................................................................................................

    public int hashCode() {
        return CASE_SENSITIVITY.hash(this.name);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            other instanceof PropertiesName &&
                this.equals0(Cast.to(other));
    }

    private boolean equals0(final PropertiesName other) {
        return this.compareTo(other) == 0;
    }

    @Override
    public String toString() {
        return this.name;
    }

    // Comparable ......................................................................................................

    @Override
    public int compareTo(final PropertiesName other) {
        return CASE_SENSITIVITY.comparator().compare(this.name, other.name);
    }

    // HasCaseSensitivity................................................................................................

    @Override
    public CaseSensitivity caseSensitivity() {
        return CASE_SENSITIVITY;
    }

    private final static CaseSensitivity CASE_SENSITIVITY = CaseSensitivity.SENSITIVE;
}
