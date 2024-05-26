package test;

import com.google.gwt.junit.client.GWTTestCase;

import walkingkooka.props.Properties;

public class TestGwtTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "test.Test";
    }

    public void testAssertEquals() {
        assertEquals(
                1,
                1
        );
    }

    public void testProperties() {
        assertEquals(
                Properties.EMPTY.isEmpty(),
                true
        );
    }
}
