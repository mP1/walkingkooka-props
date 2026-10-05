/*
 * Copyright 2024 Miroslav Pokorny (github.com/mP1)
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
import walkingkooka.text.HasText;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * A typed {@link Properties} holding values for a given {@link PropertiesPath}.
 */
public interface PropertiesLike<T> extends CanBeEmpty,
    HasText {

    /**
     * Getter that returns the value for the given {@link PropertiesPath}.
     */
    Optional<T> get(final PropertiesPath path);

    default T getOrFail(final PropertiesPath path) {
        return this.get(path)
            .orElseThrow(() -> new MissingPropertyException(path));
    }

    /**
     * Sets or replaces the string value for the given {@link PropertiesPath}, returning a {@link Properties} with the
     * change leaving the original unchanged
     */
    PropertiesLike<T> set(final PropertiesPath path,
                          final T value);

    /**
     * Removes the string value if one exists, for the given {@link PropertiesPath}, returning a {@link Properties} with the
     * change leaving the original unchanged
     */
    Properties remove(final PropertiesPath path);

    /**
     * Read-only view of the entries in this properties object.
     */
    Set<Entry<PropertiesPath, T>> entries();

    /**
     * Read-only view of the keys in this properties object.
     */
    Set<PropertiesPath> keys();

    /**
     * Read-only view of the values in this properties object.
     */
    Collection<T> values();

    /**
     * Returns the number of entries.
     */
    int size();
}
