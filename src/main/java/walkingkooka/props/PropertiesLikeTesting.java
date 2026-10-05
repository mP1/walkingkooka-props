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

import walkingkooka.CanBeEmptyTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.text.HasMultiLineTextTesting;
import walkingkooka.text.HasTextTesting;
import walkingkooka.text.printer.TreePrintableTesting;

import java.util.Optional;
import java.util.Set;

public interface PropertiesLikeTesting extends CanBeEmptyTesting,
    HasMultiLineTextTesting,
    HasPropertiesTesting,
    HasTextTesting,
    TreePrintableTesting {

    default <T> void getAndCheck(final PropertiesLike<T> properties,
                                 final PropertiesPath path) {
        this.getAndCheck(
            properties,
            path,
            Optional.empty()
        );
    }

    default <T> void getAndCheck(final PropertiesLike<T> properties,
                                 final PropertiesPath path,
                                 final T expected) {
        this.getAndCheck(
            properties,
            path,
            Optional.of(expected)
        );
    }

    default <T> void getAndCheck(final PropertiesLike<T> properties,
                                 final PropertiesPath path,
                                 final Optional<T> expected) {
        this.checkEquals(
            expected,
            properties.get(path)
        );
    }

    default <T> void getOrTryAncestorsAndCheck(final PropertiesLike<T> properties,
                                               final PropertiesPath path) {
        this.getOrTryAncestorsAndCheck(
            properties,
            path,
            Optional.empty()
        );
    }

    default <T> void getOrTryAncestorsAndCheck(final PropertiesLike<T> properties,
                                               final PropertiesPath path,
                                               final T expected) {
        this.getOrTryAncestorsAndCheck(
            properties,
            path,
            Optional.of(expected)
        );
    }

    default <T> void getOrTryAncestorsAndCheck(final PropertiesLike<T> properties,
                                               final PropertiesPath path,
                                               final Optional<T> expected) {
        this.checkEquals(
            expected,
            properties.getOrTryAncestors(path)
        );
    }

    default void keysAndCheck(final PropertiesLike<?> properties,
                              final PropertiesPath... expected) {
        this.keysAndCheck(
            properties,
            Sets.of(expected)
        );
    }

    default void keysAndCheck(final PropertiesLike<?> properties,
                              final Set<PropertiesPath> expected) {
        this.checkEquals(
            expected,
            properties.keys(),
            properties::toString
        );
    }

    default <T> void valuesAndCheck(final PropertiesLike<T> properties,
                                    final T... expected) {
        this.valuesAndCheck(
            properties,
            Sets.of(expected)
        );
    }

    default <T> void valuesAndCheck(final PropertiesLike<T> properties,
                                    final Set<T> expected) {
        this.checkEquals(
            expected,
            properties.values(),
            properties::toString
        );
    }

    default void sizeAndCheck(final PropertiesLike<?> properties,
                              final int size) {
        this.checkEquals(
            size,
            properties.size(),
            properties::toString
        );
    }
}
