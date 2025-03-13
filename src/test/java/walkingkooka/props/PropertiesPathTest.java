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
import walkingkooka.collect.set.Sets;
import walkingkooka.naming.PathSeparator;
import walkingkooka.naming.PathTesting;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.test.ParseStringTesting;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final public class PropertiesPathTest implements PathTesting<PropertiesPath, PropertiesName>,
    ClassTesting2<PropertiesPath>,
    ParseStringTesting<PropertiesPath> {

    @Test
    public void testParseEmptyComponent() {
        this.parseStringFails("before..after", IllegalArgumentException.class);
    }

    @Test
    public void testParseFlat() {
        final String value = "xyz";
        final PropertiesPath path = PropertiesPath.parse(value);
        this.valueCheck(path, value);
        this.rootCheck(path);
        this.nameCheck(path, PropertiesName.with(value));
    }

    @Test
    public void testParseHierarchical() {
        final String value = "ab.cd";
        final PropertiesPath path = PropertiesPath.parse(value);
        this.valueCheck(path, value);
        this.rootNotCheck(path);
        this.nameCheck(path, PropertiesName.with("cd"));
        this.parentCheck(path, "ab");
    }

    @Override
    public void testAppendNameToRoot() {
        // nop
    }

    @Test
    public void testGeneral() {
        final PropertiesPath path = PropertiesPath.parse("one.two.three");
        final PropertiesPath parent = path.parent().get();

        this.checkEquals("one.two", parent.value());

        assertFalse(parent.isRoot());
        assertSame(parent, path.parent().get());
        this.checkEquals(PropertiesName.with("two"), parent.name());

        final PropertiesPath grandParent = parent.parent().get();
        this.checkEquals("one", grandParent.value());
        assertTrue(grandParent.isRoot());
        this.checkEquals(PropertiesName.with("one"), grandParent.name());
    }

    @Test
    public void testAppendName() {
        final PropertiesPath path = PropertiesPath.parse("one.two.three")
            .append(
                PropertiesName.with("four")
            );
        this.nameCheck(
            path,
            PropertiesName.with("four")
        );
        this.valueCheck(
            path,
            "one.two.three.four"
        );
        this.parentCheck(
            path,
            "one.two.three"
        );
    }

    @Test
    public void testAppendPaths() {
        final PropertiesPath path = PropertiesPath.parse("one.two.three")
            .append(
                PropertiesPath.parse("four.five")
            );
        this.nameCheck(
            path,
            PropertiesName.with("five")
        );
        this.valueCheck(
            path,
            "one.two.three.four.five"
        );
        this.parentCheck(
            path,
            "one.two.three.four"
        );
    }

    @Test
    public void testEqualsDifferentPath() {
        this.checkNotEquals(PropertiesPath.parse("different.property"));
    }

    @Override
    public PropertiesPath root() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertiesPath createPath() {
        return PropertiesPath.parse("abc");
    }

    @Override
    public PropertiesPath parsePath(final String path) {
        return PropertiesPath.parse(path);
    }

    @Override
    public PropertiesName createName(final int n) {
        return PropertiesName.with("property-" + n);
    }

    @Override
    public PathSeparator separator() {
        return PropertiesPath.SEPARATOR;
    }

    // ComparableTesting................................................................................................

    @Override
    public PropertiesPath createComparable() {
        return PropertiesPath.parse("property-abc");
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<PropertiesPath> type() {
        return PropertiesPath.class;
    }

    @Override
    public final JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // ConstantTesting .................................................................................................

    @Override
    public Set<PropertiesPath> intentionalDuplicateConstants() {
        return Sets.empty();
    }

    // ParseStringTesting ..............................................................................................

    @Override
    public PropertiesPath parseString(final String text) {
        return PropertiesPath.parse(text);
    }

    @Override
    public RuntimeException parseStringFailedExpected(final RuntimeException expected) {
        return expected;
    }

    @Override
    public Class<? extends RuntimeException> parseStringFailedExpected(final Class<? extends RuntimeException> expected) {
        return expected;
    }
}
