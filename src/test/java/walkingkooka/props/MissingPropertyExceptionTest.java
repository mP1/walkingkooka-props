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

import org.junit.jupiter.api.Test;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.ThrowableTesting2;

public final class MissingPropertyExceptionTest implements ThrowableTesting2<MissingPropertyException> {

    @Override
    public void testIfClassIsFinalIfAllConstructorsArePrivate() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testProperty() {
        final PropertiesPath propertiesPath = PropertiesPath.parse("hello.world");
        final MissingPropertyException missingPropertyException = new MissingPropertyException(propertiesPath);

        this.checkEquals(
            propertiesPath,
            missingPropertyException.property()
        );
    }

    @Test
    public void testGetMessage() {
        final PropertiesPath propertiesPath = PropertiesPath.parse("hello.world");
        final MissingPropertyException missingPropertyException = new MissingPropertyException(propertiesPath);

        this.checkMessage(
            missingPropertyException,
            "Missing property \"hello.world\""
        );
    }

    // class............................................................................................................

    @Override
    public Class<MissingPropertyException> type() {
        return MissingPropertyException.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}

