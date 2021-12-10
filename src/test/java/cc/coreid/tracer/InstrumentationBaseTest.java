package cc.coreid.tracer;

import cc.coreid.tracer.test.TestClass00;
import cc.coreid.tracer.test.TestLogMemoryHandler;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test the basic enter leave tracing
 */
class InstrumentationBaseTest {
    static HashMap<String, Object> classMap;
    static TestLogMemoryHandler logHandler;
    static private ClassPool pool;

    public static String TRC_ENTER_TMPL = ">>> %s(%s) {";
    public static String TRC_LEAVE_TMPL = "<<< } %s : %s";

    static String TESTCLASS00 = "cc.coreid.tracer.test.TestClass00";

    @BeforeAll
    public static void init() throws IOException, NotFoundException {
        classMap = new HashMap<>();
        logHandler = new TestLogMemoryHandler();
        InputStream stream = new FileInputStream("src/test/resources/tjul.properties");
        LogManager.getLogManager().readConfiguration(stream);

        // prepare javassit
        pool = ClassPool.getDefault();
        pool.appendSystemPath();
        pool.appendClassPath("target/Trace-1.0-SNAPSHOT.jar");
        pool.appendClassPath(new ClassClassPath(java.util.logging.Logger.class));
        pool.appendClassPath(new ClassClassPath(java.util.logging.Level.class));
    }

    Object createTestClass00(String className) throws Exception {
        if(!classMap.containsKey(className)) {
            CtClass ct = pool.get(className);
            CtClass ex = ClassPool.getDefault().get("java.lang.Exception");
            Instrumentation.doInstrumentation(ct, ex);

            Class<?> c = ct.toClass(TestLogMemoryHandler.class);
            Object o = c.getDeclaredConstructor().newInstance();
            classMap.put(className, o);

            // attach our test logger to the class
            Logger.getLogger(className).addHandler(logHandler);
        }
        logHandler.flush();
        return classMap.get(className);
    }

    @Test
    void test_StaticThroughClass() throws Exception {
        createTestClass00(TESTCLASS00);

        TestClass00.staticVoidVoid();

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+".staticVoidVoid";
        assertEquals(TESTCLASS00+".staticVoidVoid", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, ""), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "void"), logHandler.getMessage(2));
    }

    @Test
    void test_StaticThroughInstance() throws Exception {
        TestClass00 tc = (TestClass00)createTestClass00(TESTCLASS00);

        tc.staticVoidVoid(); //NOSONAR

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+".staticVoidVoid";
        assertEquals(TESTCLASS00+".staticVoidVoid", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, ""), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "void"), logHandler.getMessage(2));
    }

    @Test
    void test_syncBoolBoolean() throws Exception {
        createTestClass00(TESTCLASS00);

        boolean b = TestClass00.syncBoolBoolean(Boolean.TRUE);
        assertFalse(b);

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+".syncBoolBoolean";
        assertEquals(TESTCLASS00+".syncBoolBoolean", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, "b='true'"), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "'false'"), logHandler.getMessage(2));
    }

    @Test
    void test_Void_Void() throws Exception {
        TestClass00 tc = (TestClass00)createTestClass00(TESTCLASS00);

        tc.Void_Void();

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+String.format("@%h",tc.hashCode())+".Void_Void";
        assertEquals(TESTCLASS00+".Void_Void", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, ""), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "void"), logHandler.getMessage(2));
    }

    @Test
    void test_Int_IntInteger() throws Exception {
        TestClass00 tc = (TestClass00)createTestClass00(TESTCLASS00);

        int r = tc.IntAddIntInteger(1, 2);
        assertEquals(3, r);

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+String.format("@%h",tc.hashCode())+".IntAddIntInteger";
        assertEquals(TESTCLASS00+".IntAddIntInteger", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, "a='1', b='2'"), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "'3'"), logHandler.getMessage(2));
    }

    @Test
    void test_IntAddIntArray() throws Exception {
        TestClass00 tc = (TestClass00)createTestClass00(TESTCLASS00);

        int r = tc.IntAddIntArray(new int[]{1,2});
        assertEquals(3, r);

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+String.format("@%h",tc.hashCode())+".IntAddIntArray";
        assertEquals(TESTCLASS00+".IntAddIntArray", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, "a='[1, 2]'"), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "'3'"), logHandler.getMessage(2));
    }

    @Test
    void test_syncChString() throws Exception {
        TestClass00 tc = (TestClass00)createTestClass00(TESTCLASS00);

        char r = tc.syncChString("hello");
        assertEquals('h', r);

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+String.format("@%h",tc.hashCode())+".syncChString";
        assertEquals(TESTCLASS00+".syncChString", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, "s='hello'"), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "'h'"), logHandler.getMessage(2));
    }

    @Test
    void test_staticThrowIntAddIntInteger() throws Exception {
        createTestClass00(TESTCLASS00);

        Integer r = null;
        try {
            r = TestClass00.staticThrowIntAddIntInteger(1, 2);
        } catch (NullPointerException e) {} //NOSONAR
        assertEquals(null, r);

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+".staticThrowIntAddIntInteger";
        assertEquals(TESTCLASS00+".staticThrowIntAddIntInteger", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, "a='1', b='2'"), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "java.lang.NullPointerException"), logHandler.getMessage(2));
    }

    @Test
    void test_throwIntAddIntInteger() throws Exception {
        TestClass00 tc = (TestClass00)createTestClass00(TESTCLASS00);

        Integer r = null;
        try {
            r = tc.throwIntAddIntInteger(1, 2);
        } catch (NullPointerException e) {} //NOSONAR
        assertEquals(null, r);

        assertEquals(3, logHandler.getMessageCount());
        String name = TESTCLASS00+String.format("@%h",tc.hashCode())+".throwIntAddIntInteger";
        assertEquals(TESTCLASS00+".throwIntAddIntInteger", logHandler.getMessage(1));
        assertEquals(String.format(TRC_ENTER_TMPL, name, "a='1', b='2'"), logHandler.getMessage(0));
        assertEquals(String.format(TRC_LEAVE_TMPL, name, "java.lang.NullPointerException"), logHandler.getMessage(2));
    }


}