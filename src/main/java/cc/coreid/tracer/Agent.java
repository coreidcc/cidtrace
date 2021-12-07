package cc.coreid.tracer;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Agent {
    private static final Logger log = Logger.getLogger(Agent.class.getName());

    public static void premain(String args, java.lang.instrument.Instrumentation instrumentation) {

        Properties cfg = Configuration.parseCfgStr(args);
        instrumentation.addTransformer(new Transformer(cfg));
    }

    public static class Transformer implements ClassFileTransformer {
        public Transformer(Properties cfg) {
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
            log.log(Level.CONFIG, "{0} instrumenting class {1}", new Object[]{Transformer.class.getName(), className});
            CtClass cl = null;
            ClassPool pool = ClassPool.getDefault();
            try {
                if (className.startsWith("cc/coreid/tracer/Point")) {  // FIXME: remove just for initial testing
                    cl = pool.makeClass(new ByteArrayInputStream(bytes));
                    Instrumentation.doInstrumentation(cl);
                    bytes = cl.toBytecode();
                }
            } catch (Exception ex) {
                String msg = String.format("%s: failed instrumenting '%s'", Transformer.class.getName(), className);
                log.log(Level.WARNING, msg, ex);
            } finally {
                if (cl != null) {
                    cl.detach();
                }
            }
            return bytes;
        }
    }
}

