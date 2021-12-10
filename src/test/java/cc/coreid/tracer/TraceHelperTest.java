package cc.coreid.tracer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraceHelperTest {

    @Test
    void testFormat() {
        assertEquals("[true, false]", TraceHelper.format(new boolean[] {true,false}));
        assertEquals("[0, 1]",TraceHelper.format(new byte[] {0,1}));
        assertEquals("[a, b]",TraceHelper.format(new char[] {'a','b'}));
        assertEquals("[0.0, 1.0]",TraceHelper.format(new double[] {0,1}));
        assertEquals("[0.0, 1.0]",TraceHelper.format(new float[] {0,1}));
        assertEquals("[0, 1]",TraceHelper.format(new int[] {0,1}));
        assertEquals("[0, 1]",TraceHelper.format(new long[] {0,1}));
        assertEquals("[0, 1]",TraceHelper.format(new short[] {0,1}));
    }
}
