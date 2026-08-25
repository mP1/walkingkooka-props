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

import walkingkooka.visit.Visitor;

import java.util.Map.Entry;
import java.util.Objects;

public class PropertiesFileVisitor extends Visitor<Properties> {

    protected PropertiesFileVisitor() {
        super();
    }

    protected void visitComment(final String comment) {

    }

    protected void visitEnd() {

    }

    protected void visitKey(final String key) {

    }

    protected void visitStart() {

    }

    protected void visitValue(final String value) {

    }

    @Override
    public void accept(final Properties properties) {
        Objects.requireNonNull(properties, "properties");

        this.visitStart();

        this.visitComment(properties.comment());

        for(final Entry<PropertiesPath, String> keyAndValue : properties.entries()) {
            this.visitKey(
                keyAndValue.getKey()
                    .value()
            );
            this.visitValue(
                keyAndValue.getValue()
            );
        }

        this.visitEnd();
    }
}
