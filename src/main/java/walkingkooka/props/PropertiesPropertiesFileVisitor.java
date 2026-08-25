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

final class PropertiesPropertiesFileVisitor extends PropertiesFileVisitor {

    static PropertiesPropertiesFileVisitor empty() {
        return new PropertiesPropertiesFileVisitor();
    }

    PropertiesPropertiesFileVisitor() {
        super();

        this.properties = Properties.EMPTY;
    }

    @Override
    protected void visitComment(final String comment) {
        this.properties = this.properties.setComment(comment);
    }

    @Override
    protected void visitEnd() {

    }

    @Override
    protected void visitKey(final String key) {
        System.out.println("visitKey: " + key);
        this.key = PropertiesPath.parse(key);
    }

    private PropertiesPath key;

    @Override
    protected void visitValue(final String value) {
        System.out.println("visitValue: " + value);
        this.properties = this.properties.set(
            this.key,
            value
        );

        this.key = null;
    }

    Properties properties;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.properties.toString();
    }
}
