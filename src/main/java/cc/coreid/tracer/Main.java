package cc.coreid.tracer;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;

import java.util.logging.Logger;

// FIXME: just a stupid main to do initial testing -> get rid of it and implement proper test-cases
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] argv) {
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.appendSystemPath();
            pool.appendClassPath("target/Trace-1.0-SNAPSHOT.jar");
            pool.appendClassPath(new ClassClassPath(java.util.logging.Logger.class));
            pool.appendClassPath(new ClassClassPath(java.util.logging.Level.class));
            CtClass cl = pool.get("cc.coreid.tracer.Point");
            CtClass ex = ClassPool.getDefault().get("java.lang.Exception");
            pool.get("java.util.logging.Logger");
            Instrumentation.doInstrumentation(cl, ex);
            cl.writeFile("target/classes");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
