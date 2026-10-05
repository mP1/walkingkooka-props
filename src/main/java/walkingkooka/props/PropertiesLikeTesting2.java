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

import org.junit.jupiter.api.Test;
import walkingkooka.reflect.PublicClassTesting;

import static org.junit.jupiter.api.Assertions.assertThrows;

public interface PropertiesLikeTesting2<P extends PropertiesLike<V>, V> extends PropertiesLikeTesting,
    PublicClassTesting<P> {

    @Test
    default void testGetWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createPropertiesLike()
                .get(null)
        );
    }

    @Test
    default void testGetOrTryAncestorsWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createPropertiesLike()
                .getOrTryAncestors(null)
        );
    }

    @Test
    default void testSetWithNullPathFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createPropertiesLike()
                .set(
                    null,
                    null // value
                )
        );
    }

    @Test
    default void testSetWithNullValueFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createPropertiesLike()
                .set(
                    PropertiesPath.parse("key.123"),
                    null
                )
        );
    }

    @Test
    default void testRemoveWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> Properties.EMPTY.remove(null)
        );
    }

    P createPropertiesLike();
}
