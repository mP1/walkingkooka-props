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
import walkingkooka.CanBeEmptyTesting;
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.test.ParseStringTesting;
import walkingkooka.text.CharSequences;
import walkingkooka.text.HasTextTesting;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class PropertiesTest implements ClassTesting<Properties>,
    HashCodeEqualsDefinedTesting2<Properties>,
    HasTextTesting,
    ToStringTesting<Properties>,
    CanBeEmptyTesting,
    JsonNodeMarshallingTesting<Properties>,
    TreePrintableTesting,
    ParseStringTesting<Properties> {

    // get..............................................................................................................

    @Test
    public void testGetNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> Properties.EMPTY.get(null)
        );
    }

    @Test
    public void testGet() {
        final PropertiesPath key = PropertiesPath.parse("key.1");
        final String value = "value1";

        this.getAndCheck(
            new Properties(
                Maps.of(
                    key,
                    value
                )
            ),
            key,
            Optional.of(value)
        );
    }

    @Test
    public void testGetUnknown() {
        this.getAndCheck(
            new Properties(
                Maps.of(
                    PropertiesPath.parse("key.1"),
                    "value1"
                )
            ),
            PropertiesPath.parse("unknown.key.404"),
            Optional.empty()
        );
    }

    private void getAndCheck(final Properties properties,
                             final PropertiesPath key,
                             final Optional<String> value) {
        this.checkEquals(
            value,
            properties.get(key),
            () -> properties + " " + key
        );
    }

    // set..............................................................................................................

    @Test
    public void testSetNullPathFails() {
        assertThrows(
            NullPointerException.class,
            () -> Properties.EMPTY.set(
                null,
                "*value*"
            )
        );
    }

    @Test
    public void testSetNullValueFails() {
        assertThrows(
            NullPointerException.class,
            () -> Properties.EMPTY.set(
                PropertiesPath.parse("key.123"),
                null
            )
        );
    }

    @Test
    public void testSetSame() {
        final PropertiesPath key = PropertiesPath.parse("key.123");
        final String value = "*value*123";

        final Properties properties = new Properties(
            Maps.of(
                key,
                value
            )
        );

        assertSame(
            properties,
            properties.set(
                key,
                value
            )
        );
    }

    @Test
    public void testSetDifferentWhenEmpty() {
        final PropertiesPath key = PropertiesPath.parse("key.123");
        final String value = "*value*123";

        this.setAndCheck(
            Properties.EMPTY,
            key,
            value,
            Maps.of(
                key,
                value
            )
        );
    }

    @Test
    public void testSetDifferentWhenNonEmpty() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final String value1 = "*value*111";

        final PropertiesPath key2 = PropertiesPath.parse("key.222");
        final String value2 = "*value*222";

        this.setAndCheck(
            new Properties(
                Maps.of(
                    key1,
                    value1
                )
            ),
            key2,
            value2,
            Maps.of(
                key1,
                value1,
                key2,
                value2
            )
        );
    }

    private void setAndCheck(final Properties properties,
                             final PropertiesPath key,
                             final String value,
                             final Map<PropertiesPath, String> expected) {
        final Properties set = properties.set(
            key,
            value
        );
        assertNotSame(
            properties,
            set
        );

        this.checkEquals(
            expected,
            set.pathToValue,
            () -> properties + " " + key + " " + value
        );
    }

    // remove...........................................................................................................

    @Test
    public void testRemoveNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> Properties.EMPTY.remove(null)
        );
    }

    @Test
    public void testRemoveWhenEmpty() {
        final PropertiesPath key = PropertiesPath.parse("key.123");

        assertSame(
            Properties.EMPTY.remove(key),
            Properties.EMPTY
        );
    }

    @Test
    public void testRemoveUnknown() {
        final PropertiesPath key = PropertiesPath.parse("key.111");
        final String value = "*value*111";
        final Properties properties = new Properties(
            Maps.of(
                key,
                value
            )
        );

        assertSame(
            properties,
            properties.remove(
                PropertiesPath.parse("unknown.404")
            )
        );
    }

    @Test
    public void testRemoveWhenNonEmpty() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final String value1 = "*value*111";

        final PropertiesPath key2 = PropertiesPath.parse("key.222");
        final String value2 = "*value*222";

        this.removeAndCheck(
            new Properties(
                Maps.of(
                    key1,
                    value1,
                    key2,
                    value2
                )
            ),
            key2,
            Maps.of(
                key1,
                value1
            )
        );
    }

    @Test
    public void testRemoveBecomesEmpty() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final String value1 = "*value*111";


        assertSame(
            new Properties(
                Maps.of(
                    key1,
                    value1
                )
            ).remove(key1),
            Properties.EMPTY
        );
    }

    private void removeAndCheck(final Properties properties,
                                final PropertiesPath key,
                                final Map<PropertiesPath, String> expected) {
        final Properties removed = properties.remove(
            key
        );
        assertNotSame(
            properties,
            removed
        );

        this.checkEquals(
            expected,
            removed.pathToValue,
            () -> properties + " " + key
        );
    }

    // all..............................................................................................................

    @Test
    public void testSetSetSet() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final String value1 = "*value*111";

        final PropertiesPath key2 = PropertiesPath.parse("key.222");
        final String value2 = "*value*222";

        final PropertiesPath key3 = PropertiesPath.parse("key.333");
        final String value3 = "*value*333";

        this.checkEquals(
            new Properties(
                Maps.of(
                    key1,
                    value1,
                    key2,
                    value2,
                    key3,
                    value3
                )
            ),
            Properties.EMPTY.set(
                key1,
                value1
            ).set(
                key2,
                value2
            ).set(
                key3,
                value3
            )
        );
    }

    @Test
    public void testSetAndReplace() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final String value1 = "*value*111";

        final PropertiesPath key2 = PropertiesPath.parse("key.222");
        final String value2 = "*value*222";

        final PropertiesPath key3 = PropertiesPath.parse("key.333");
        final String value3 = "*value*333";

        this.checkEquals(
            new Properties(
                Maps.of(
                    key1,
                    value1,
                    key2,
                    value2,
                    key3,
                    value3
                )
            ),
            Properties.EMPTY.set(
                key1,
                value1
            ).set(
                key2,
                "replaced"
            ).set(
                key2,
                value2
            ).set(
                key3,
                value3
            )
        );
    }

    @Test
    public void testSetAndRemove() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final String value1 = "*value*111";

        final PropertiesPath key2 = PropertiesPath.parse("key.222");
        final String value2 = "*value*222";

        final PropertiesPath key3 = PropertiesPath.parse("key.333");
        final String value3 = "*value*333";

        this.checkEquals(
            new Properties(
                Maps.of(
                    key1,
                    value1,
                    key2,
                    value2,
                    key3,
                    value3
                )
            ),
            Properties.EMPTY.set(
                    key1,
                    value1
                ).set(
                    key2,
                    "removed"
                ).remove(key2)
                .set(
                    key2,
                    value2
                ).set(
                    key3,
                    value3
                )
        );
    }

    // entries..........................................................................................................

    @Test
    public void testEntriesWhenEmpty() {
        this.entriesAndCheck(
            Properties.EMPTY
        );
    }

    @Test
    public void testEntriesWhenNotEmpty() {
        final PropertiesPath key = PropertiesPath.parse("key.111");
        final String value = "*value1*";

        this.entriesAndCheck(
            Properties.EMPTY.set(
                key,
                value
            ),
            Maps.entry(
                key,
                value
            )
        );
    }

    @Test
    public void testEntriesWhenNotEmpty2() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final PropertiesPath key2 = PropertiesPath.parse("key.222");

        final String value1 = "*value1*";
        final String value2 = "*value2*";

        this.entriesAndCheck(
            Properties.EMPTY.set(
                key1,
                value1
            ).set(
                key2,
                value2
            ),
            Maps.entry(
                key1,
                value1
            ),
            Maps.entry(
                key2,
                value2
            )
        );
    }

    private void entriesAndCheck(final Properties properties,
                                 final Entry<PropertiesPath, String>... expected) {
        this.entriesAndCheck(
            properties,
            Sets.of(expected)
        );
    }

    private void entriesAndCheck(final Properties properties,
                                 final Set<Entry<PropertiesPath, String>> expected) {
        final Map<PropertiesPath, String> expectedMap = Maps.sorted();
        for (final Entry<PropertiesPath, String> entry : expected) {
            expectedMap.put(
                entry.getKey(),
                entry.getValue()
            );
        }

        final Map<PropertiesPath, String> actualMap = Maps.sorted();
        for (final Entry<PropertiesPath, String> entry : properties.entries()) {
            actualMap.put(
                entry.getKey(),
                entry.getValue()
            );
        }

        // cant compare Set<Entry> because Entry.hashCode is not defined

        this.checkEquals(
            expectedMap,
            actualMap,
            () -> properties.toString()
        );
    }

    @Test
    public void testEntriesReadOnly() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> Properties.EMPTY.set(
                    PropertiesPath.parse("key.111"),
                    "value111"
                ).entries()
                .clear()
        );
    }

    // keys.............................................................................................................

    @Test
    public void testKeysWhenEmpty() {
        this.keysAndCheck(
            Properties.EMPTY
        );
    }

    @Test
    public void testKeysWhenNotEmpty() {
        final PropertiesPath key = PropertiesPath.parse("key.111");

        this.keysAndCheck(
            Properties.EMPTY.set(
                key,
                "*value11*"
            ),
            key
        );
    }

    @Test
    public void testKeysWhenNotEmpty2() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final PropertiesPath key2 = PropertiesPath.parse("key.222");

        this.keysAndCheck(
            Properties.EMPTY.set(
                key1,
                "*value11*"
            ).set(
                key2,
                "*value22*"
            ),
            key1,
            key2
        );
    }

    private void keysAndCheck(final Properties properties,
                              final PropertiesPath... expected) {
        this.keysAndCheck(
            properties,
            Sets.of(expected)
        );
    }

    private void keysAndCheck(final Properties properties,
                              final Set<PropertiesPath> expected) {
        this.checkEquals(
            expected,
            properties.keys(),
            () -> properties.toString()
        );
    }

    @Test
    public void testKeysReadOnly() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> Properties.EMPTY.set(
                    PropertiesPath.parse("key.111"),
                    "value111"
                ).keys()
                .clear()
        );
    }

    // values.............................................................................................................

    @Test
    public void testValuesWhenEmpty() {
        this.valuesAndCheck(
            Properties.EMPTY
        );
    }

    @Test
    public void testValuesWhenNotEmpty() {
        final String value = "*value11*";

        this.valuesAndCheck(
            Properties.EMPTY.set(
                PropertiesPath.parse("key.111"),
                value
            ),
            value
        );
    }

    @Test
    public void testValuesWhenNotEmpty2() {
        final String value1 = "*value11*";
        final String value2 = "*value22*";

        this.valuesAndCheck(
            Properties.EMPTY.set(
                PropertiesPath.parse("key.111"),
                value1
            ).set(
                PropertiesPath.parse("key.222"),
                value2
            ),
            value1,
            value2
        );
    }

    private void valuesAndCheck(final Properties properties,
                                final String... expected) {
        this.valuesAndCheck(
            properties,
            Sets.of(expected)
        );
    }

    private void valuesAndCheck(final Properties properties,
                                final Collection<String> expected) {
        this.checkEquals(
            new ArrayList<>(expected),
            new ArrayList<>(
                properties.values()
            ),
            () -> properties.toString()
        );
    }

    @Test
    public void testValuesReadOnly() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> Properties.EMPTY.set(
                    PropertiesPath.parse("key.111"),
                    "value111"
                ).values()
                .clear()
        );
    }

    // canBeEmpty.......................................................................................................

    @Test
    public void testIsEmptyWhenEmpty() {
        this.isEmptyAndCheck(
            Properties.EMPTY,
            true
        );
    }

    @Test
    public void testIsEmptyWhenNotEmpty() {
        this.isEmptyAndCheck(
            Properties.EMPTY.set(
                PropertiesPath.parse("key.111"),
                "*value*111"
            ),
            false
        );
    }

    // size.............................................................................................................

    @Test
    public void testSizeWhenEmpty() {
        this.sizeAndCheck(
            Properties.EMPTY,
            0
        );
    }

    @Test
    public void testSizeWhenNotEmpty() {
        this.sizeAndCheck(
            Properties.EMPTY.set(
                PropertiesPath.parse("key.111"),
                "*value11*"
            ),
            1
        );
    }

    private void sizeAndCheck(final Properties properties,
                              final int expected) {
        this.checkEquals(
            expected,
            properties.size(),
            () -> properties.toString()
        );
    }

    // parse............................................................................................................

    @Override
    public void testParseStringEmptyFails() {
        throw new UnsupportedOperationException(); // empty is ok
    }

    // trailing whitespace not removed
    @Test
    public void testParseJavaUtilPropertiesKeyAndValueSurroundedByWhitespace() throws Exception {
        final java.util.Properties p = new java.util.Properties();
        p.load(new StringReader(" a1 = b2 "));
        this.checkEquals(
            "b2 ",
            p.get("a1")
        );
    }

    @Test
    public void testParseJavaUtilPropertiesMultilineValue() throws Exception {
        final java.util.Properties p = new java.util.Properties();
        p.load(new StringReader(" a1 = b2 \\\n   c3 \\\r   d4 "));
        this.checkEquals(
            "b2 c3 d4 ",
            p.get("a1")
        );
    }

    @Test
    public void testParseJavaUtilPropertiesMidLineComment() throws Exception {
        final java.util.Properties p = new java.util.Properties();
        p.load(new StringReader("a=before!comment"));
        this.checkEquals(
            "before!comment",
            p.get("a")
        );
    }

    @Test
    public void testParseEmpty() {
        this.parseStringAndCheck(
            "",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseEmptyLineCr() {
        this.parseStringAndCheck(
            " \r",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseEmptyLineNl() {
        this.parseStringAndCheck(
            " \n",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseEmptyLineCrNl() {
        this.parseStringAndCheck(
            " \r\n",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseEmptyLineWhitespace() {
        this.parseStringAndCheck(
            "\b\f\t\r\n",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseEsclComments() {
        this.parseStringAndCheck(
            "! 123",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseHashComments() {
        this.parseStringAndCheck(
            "# 123",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseHashCommentsEmptyLine() {
        this.parseStringAndCheck(
            "# 123\n\r\n",
            Properties.EMPTY
        );
    }

    @Test
    public void testParseKeyMissingAssignmentFails() {
        this.parseStringFails(
            "key1",
            new IllegalArgumentException("Missing assignment following key")
        );
    }

    @Test
    public void testParseKeyEmptyValue() {
        this.parseStringAndCheck(
            "key1=",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                ""
            )
        );
    }

    @Test
    public void testParseKeyNonEmptyValue() {
        this.parseStringAndCheck(
            "key1=123",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "123"
            )
        );
    }

    @Test
    public void testParseKeyValueNulChar() {
        this.parseStringAndCheck(
            "key1=\\u0000",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "\u0000"
            )
        );
    }

    @Test
    public void testParseKeyNonEmptyValueIncludesUnicode() {
        this.parseStringAndCheck(
            "key1=123\\u0041\\u0042",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "123AB"
            )
        );
    }

    @Test
    public void testParseWhitespaceKeyWhitespaceAssignmentWhitespaceValueWhitespace() {
        this.parseStringAndCheck(
            " key1 = 123   ",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "123   "
            )
        );
    }

    @Test
    public void testParseWhitespaceKeyWhitespaceAssignmentWhitespaceValueWhitespace2() {
        this.parseStringAndCheck(
            "\f\tkey1\f\t=\f\t123\f\t",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "123\f\t"
            )
        );
    }

    @Test
    public void testParseKeyMultilineValueCr() {
        this.parseStringAndCheck(
            "key1=123\\\r4",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "1234"
            )
        );
    }

    @Test
    public void testParseKeyMultilineValueNl() {
        this.parseStringAndCheck(
            "key1=123\\\n4",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "1234"
            )
        );
    }

    @Test
    public void testParseKeyMultilineValueCrNl() {
        this.parseStringAndCheck(
            "key1=123\\\r\n4",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "1234"
            )
        );
    }

    @Test
    public void testParseKeyMultilineValueCrNl2() {
        this.parseStringAndCheck(
            "key1=123\\\r4\\\n5\\\r\n6",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "123456"
            )
        );
    }

    @Test
    public void testParseKeyValueKeyValue() {
        this.parseStringAndCheck(
            "key1=111\rkey2=222",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "111"
            ).set(
                PropertiesPath.parse("key2"),
                "222"
            )
        );
    }

    @Test
    public void testParseKeyMultilineValueCrKeyValue() {
        this.parseStringAndCheck(
            "key1=111\\\rAAA\rkey2=222",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "111AAA"
            ).set(
                PropertiesPath.parse("key2"),
                "222"
            )
        );
    }

    @Test
    public void testParseKeyMultilineValueNlKeyValue() {
        this.parseStringAndCheck(
            "key1=111\\\nAAA\rkey2=222",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "111AAA"
            ).set(
                PropertiesPath.parse("key2"),
                "222"
            )
        );
    }

    @Test
    public void testParseKeyMultilineValueCrNlKeyValue() {
        this.parseStringAndCheck(
            "key1=111\\\r\nAAA\rkey2=222",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "111AAA"
            ).set(
                PropertiesPath.parse("key2"),
                "222"
            )
        );
    }

    @Test
    public void testParseKeyValueCommentKeyValueComment() {
        this.parseStringAndCheck(
            "key1=111\n! comment1\nkey2=222\n# comment 2",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "111"
            ).set(
                PropertiesPath.parse("key2"),
                "222"
            )
        );
    }

    @Test
    public void testParseKeyValueEmptyLineKeyValueComment() {
        this.parseStringAndCheck(
            "key1=111\n \t\f\b\nkey2=222\n# comment 2",
            Properties.EMPTY.set(
                PropertiesPath.parse("key1"),
                "111"
            ).set(
                PropertiesPath.parse("key2"),
                "222"
            )
        );
    }

    @Override
    public Properties parseString(final String text) {
        return Properties.parse(text);
    }

    @Override
    public Class<? extends RuntimeException> parseStringFailedExpected(final Class<? extends RuntimeException> thrown) {
        return thrown;
    }

    @Override
    public RuntimeException parseStringFailedExpected(final RuntimeException thrown) {
        return thrown;
    }

    // TreePrintable....................................................................................................

    @Test
    public void testPrintTreeWhenEmpty() {
        this.treePrintAndCheck(
            Properties.EMPTY,
            ""
        );
    }

    @Test
    public void testPrintTreeWhenNotEmpty() {
        this.treePrintAndCheck(
            Properties.EMPTY
                .set(
                    PropertiesPath.parse("key.111"),
                    "*value*111"
                ).set(
                    PropertiesPath.parse("key.222"),
                    "*value*222"
                ),
            "key.111=*value*111\n" +
                "key.222=*value*222\n"
        );
    }

    @Test
    public void testPrintTreeWhenNotEmptyButEmptyValues() {
        this.treePrintAndCheck(
            Properties.EMPTY
                .set(
                    PropertiesPath.parse("key.111"),
                    ""
                ).set(
                    PropertiesPath.parse("key.222"),
                    "*value*222"
                ),
            "key.111=\n" +
                "key.222=*value*222\n"
        );
    }

    @Test
    public void testPrintTreeValuesNeedEscaping() {
        this.treePrintAndCheck(
            Properties.EMPTY
                .set(
                    PropertiesPath.parse("key.111"),
                    "*value*111"
                ).set(
                    PropertiesPath.parse("key.222"),
                    "\b\f\t\r\n*value*222\b\f\t\r\n"
                ),
            "key.111=*value*111\n" +
                "key.222=\\b\\f\\t\\r\\n*value*222\\b\\f\\t\\r\\n\n"
        );
    }

    @Test
    public void testPrintTreeMultiLineValues() {
        this.treePrintAndCheck(
            Properties.EMPTY
                .set(
                    PropertiesPath.parse("key.111"),
                    "*value*111"
                ).set(
                    PropertiesPath.parse("key.222"),
                    "222\rBBB\n222\r\nBBB"
                ).set(
                    PropertiesPath.parse("key.333"),
                    "333"
                ),
            "key.111=*value*111\n" +
                "key.222=222\\rBBB\\n222\\r\\nBBB\n" +
                "key.333=333\n"
        );
    }

    @Test
    public void testPrintTreeNulChar() throws Exception {

        final java.util.Properties p = new java.util.Properties();
        p.setProperty("key.111", "\u0000");

        final StringWriter stringWriter = new StringWriter();
        p.store(
            stringWriter,
            null
        );

        final StringBuilder text = new StringBuilder();
        try (final BufferedReader bufferedReader = new BufferedReader(new StringReader(stringWriter.toString()))) {
            for (; ; ) {
                final String line = bufferedReader.readLine();
                if (null == line) {
                    break;
                }
                if (line.startsWith("!") | line.startsWith("#")) {
                    continue;
                }
                text.append(line)
                    .append('\n');
            }
        }


        this.checkEquals(
            "key.111=\u0000\n",
            text.toString()
        );

        this.treePrintAndCheck(
            Properties.EMPTY
                .set(
                    PropertiesPath.parse("key.111"),
                    "\u0000"
                ).set(
                    PropertiesPath.parse("key.222"),
                    "222"
                ),
            "key.111=\\u0000\n" +
                "key.222=222\n"
        );
    }

    @Test
    public void testPrintTreeAndParse15() {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            b.append(
                CharSequences.escape(
                    String.valueOf((char) i)
                )
            );
        }
        final String value = b.toString();

        final Properties properties = Properties.EMPTY
            .set(
                PropertiesPath.parse("key.111"),
                value
            );

        final String text = properties.treeToString(
            Indentation.SPACES2,
            LineEnding.NL
        );

        this.checkEquals(
            properties,
            Properties.parse(text),
            () -> "round tripping\n" + text
        );
    }

    @Test
    public void testPrintTreeAndParse255() {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            b.append(
                CharSequences.escape(
                    String.valueOf((char) i)
                )
            );
        }
        final String value = b.toString();

        final Properties properties = Properties.EMPTY
            .set(
                PropertiesPath.parse("key.111"),
                value
            );

        final String text = properties.treeToString(
            Indentation.SPACES2,
            LineEnding.NL
        );

        this.checkEquals(
            properties,
            Properties.parse(text),
            () -> "round tripping\n" + text
        );
    }

    // java.util.Properties.............................................................................................

    @Test
    public void testJavaUtilPropertiesSaveLoadRoundtrip() throws Exception {
        final java.util.Properties saved = new java.util.Properties();

        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            b.append((char) i);
        }
        final String value = b.toString();

        saved.setProperty("key123", value);

        final StringWriter stringWriter = new StringWriter();
        saved.store(stringWriter, null);
        stringWriter.flush();
        stringWriter.close();

        final String written = stringWriter.toString();

        final java.util.Properties loaded = new java.util.Properties();
        loaded.load(new StringReader(written));

        this.checkEquals(
            saved,
            loaded
        );
    }

    @Test
    public void testJavaUtilPropertiesSaveLoadRoundtrip2() throws Exception {
        final java.util.Properties saved = new java.util.Properties();
        final String value = "   Hello   ";
        saved.setProperty("key123", value);

        final StringWriter stringWriter = new StringWriter();
        saved.store(stringWriter, null);
        stringWriter.flush();
        stringWriter.close();

        final String written = stringWriter.toString();
        System.out.println(written);

        final java.util.Properties loaded = new java.util.Properties();
        loaded.load(new StringReader(written));

        this.checkEquals(
            saved,
            loaded
        );
    }

    // equals...........................................................................................................

    @Test
    public void testEqualsDifferent() {
        this.checkNotEquals(
            Properties.EMPTY
                .set(
                    PropertiesPath.parse("key.111"),
                    "*value*111"
                ).set(
                    PropertiesPath.parse("key.222"),
                    "*value*222"
                )
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final PropertiesPath key1 = PropertiesPath.parse("key.111");
        final String value1 = "*value*111";

        final PropertiesPath key2 = PropertiesPath.parse("key.222");
        final String value2 = "*value*222";

        final Map<PropertiesPath, String> map = Maps.of(
            key1,
            value1,
            key2,
            value2
        );

        this.toStringAndCheck(
            new Properties(
                map
            ),
            map.toString()
        );
    }

    // JSON.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            Properties.EMPTY.set(
                PropertiesPath.parse("key.111"),
                "value111"
            ).set(
                PropertiesPath.parse("key.222"),
                "value222"
            ),
            JsonNode.object()
                .set(
                    JsonPropertyName.with("key.111"),
                    JsonNode.string("value111")
                ).set(
                    JsonPropertyName.with("key.222"),
                    JsonNode.string("value222")
                )
        );
    }

    @Override
    public Properties unmarshall(final JsonNode json,
                                 final JsonNodeUnmarshallContext context) {
        return Properties.unmarshall(
            json,
            context
        );
    }

    @Override
    public Properties createJsonNodeMarshallingValue() {
        return Properties.EMPTY.set(
            PropertiesPath.parse("key.111"),
            "value111"
        );
    }

    // HasText..........................................................................................................

    @Test
    public void testText() {
        this.textAndCheck(
            Properties.EMPTY.set(
                PropertiesPath.parse("hello"),
                "world"
            ).set(
                PropertiesPath.parse("2nd"),
                "222"
            ),
            "{hello=world, 2nd=222}"
        );
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<Properties> type() {
        return Properties.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // HashCodeEqualsDefinedTesting2 ...................................................................................

    @Override
    public Properties createObject() {
        return Properties.EMPTY;
    }
}
