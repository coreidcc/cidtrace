package cc.coreid.tracer.test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestClass00 {
    private static final Logger log = Logger.getLogger(TestClass00.class.getName());

    public static void staticVoidVoid() {
        log.log(Level.FINE, TestClass00.class.getName()+".staticVoidVoid");
    }

    synchronized public static boolean syncBoolBoolean(Boolean b) {
        log.log(Level.FINE, TestClass00.class.getName()+".syncBoolBoolean");
        return !b;
    }

    public void Void_Void() {
        log.log(Level.FINE, TestClass00.class.getName()+".Void_Void");
    }

    public int IntAddIntInteger(int a, Integer b) {
        log.log(Level.FINE, TestClass00.class.getName()+".IntAddIntInteger");
        return a+b;
    }

    public int IntAddIntArray(int[] a) {
        log.log(Level.FINE, TestClass00.class.getName()+".IntAddIntArray");
        return (a[0]+a[1]);
    }

    synchronized public char syncChString(String s) {
        log.log(Level.FINE, TestClass00.class.getName()+".syncChString");
        return s.charAt(0);
    }

    static public int staticThrowIntAddIntInteger(int a, Integer b) {
        log.log(Level.FINE, TestClass00.class.getName()+".staticThrowIntAddIntInteger");
        b = null;
        int r = a+b;  //NOSONAR
        return r;
    }

    public int throwIntAddIntInteger(int a, Integer b) {
        log.log(Level.FINE, TestClass00.class.getName()+".throwIntAddIntInteger");
        b = null;
        int r = a+b;  //NOSONAR
        return r;
    }


}
