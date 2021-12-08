package cc.coreid.tracer;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Instrumentation {
    static final String LOGGER_NAME = "_cc_log_";
    private static final Logger log = Logger.getLogger(Instrumentation.class.getName());
    private final CtClass cl;
    private final CtClass clEx;

    public Instrumentation(CtClass cl, CtClass ex) {
        this.cl = cl;
        this.clEx = ex;
    }

    public static void doInstrumentation(CtClass cl, CtClass ex) {
        assert (cl != null);
        if (!cl.isInterface()) {
            Instrumentation li = new Instrumentation(cl, ex);
            li.doInstrumentation();
        }
    }

    public void doInstrumentation() {
        log.log(Level.INFO, "Instrumentation.doInstrumentClass: {0}", cl.getName());
        try {
            if (isClassInstrumentationNeeded()) {
                createLoggerField();
                for (CtBehavior m : cl.getDeclaredBehaviors()) {
                    if (isMethodInstrumentationNeeded(m)) {
                        doMethodInstrumentation(m);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Instrumentation.doInstrumentClass failed: '" + cl.getName() + "'", ex); //NOSONAR
        }
    }

    /**
     * Check if we need to instrument the given class
     * <p>
     * TODO: currently we do instrument all classes -> provide some sort of configuration e.g.
     *       a) white- and black-lists of package names -> external configuration, allows arbitrary code to be instrumented
     *       b) marker annotation to trigger instrumentation -> no instrumentation if we don't maintain the source
     *       c) or a combination of those
     * <p>
     * If we find a field with our logger name we assume that the class is already instrumented.
     * Consequences: if there's a name clash we might miss instrumentation.
     */
    boolean isClassInstrumentationNeeded() {
        boolean isInstrumentationNeeded = true;
        // the current implementation of CtClass does throw a NotFoundException if a given field
        // doesn't exist. at the same time it doesn't provide a way to query the existence of a
        // given field. => Bad, bad API design
        try {
            isInstrumentationNeeded = (cl.getField(LOGGER_NAME) == null);
            if (!isInstrumentationNeeded) {
                log.log(Level.WARNING,
                        "Class {0} contains a field {1} skip instrumentation",
                        new Object[]{cl.getName(), LOGGER_NAME});
            }
        }
        // swallow it as Exception is used for if/then/else
        catch (NotFoundException ignored) { //NOSONAR
        }
        return isInstrumentationNeeded;
    }

    void createLoggerField() throws CannotCompileException {
        CtField field = CtField.make("private static java.util.logging.Logger " + LOGGER_NAME + ";", cl);
        cl.addField(field, "java.util.logging.Logger.getLogger(\"" + cl.getName() + "\")");
    }

    /**
     * For now we just instrument every public method
     * <p>
     * TODO: we need to become a lot more intelligent
     * a) avoid tracing of critical data (e.g. passwords) -> exclude common names (configurable with sane defaults)
     * b) allow method to be excluded explicitly (e.g. via annotations)
     * c) allow masking of given method parameters
     * d) identify getters and setters and trace them differently
     */
    boolean isMethodInstrumentationNeeded(CtBehavior m) throws ClassNotFoundException {
        boolean doTrace = Modifier.isPublic(m.getModifiers());
        if (m.hasAnnotation(Trace.class)) {
            Trace a = (Trace) m.getAnnotation(Trace.class);
            doTrace = !a.exclude();
        }
        return doTrace;
    }

    void doMethodInstrumentation(CtBehavior m) throws NotFoundException, CannotCompileException {
        log.log(Level.INFO, "Instrumentation.doInstrumentMethod: {0}", m.getName());
        String mSig = methodSignature(m);
        mSig = String.format(
                "if(%s.isLoggable(java.util.logging.Level.FINEST))" +
                        "%s.log(java.util.logging.Level.FINEST,\"%s\");",
                LOGGER_NAME, LOGGER_NAME, mSig);
        log.log(Level.FINE, mSig);
        m.insertBefore(mSig);

        String mRet = methodReturn(m);
        mRet = String.format(
                "if(%s.isLoggable(java.util.logging.Level.FINEST))" +
                        "%s.log(java.util.logging.Level.FINEST,\"%s\");",
                LOGGER_NAME, LOGGER_NAME, mRet);
        log.log(Level.FINE, mRet);
        m.insertAfter(mRet);

        String mExc = methodException(m);
        mExc = String.format(
                "{ if(%s.isLoggable(java.util.logging.Level.FINEST))" +
                        "%s.log(java.util.logging.Level.FINEST,\"%s\");"+
                "  throw $e; }",
                LOGGER_NAME, LOGGER_NAME, mExc);
        log.log(Level.FINE, mExc);
        m.addCatch(mExc, clEx);
    }

    String methodSignature(CtBehavior m) throws NotFoundException {

        // extract meta-data from byte code
        MethodInfo mi = m.getMethodInfo();
        LocalVariableAttribute paramNames = getParameterValues(mi);
        CtClass[] paramTypes = m.getParameterTypes();
        Annotation[][] paramAnnotations = getParameterAnnotations(mi);

        // create log-statement
        StringBuilder sb = new StringBuilder(">>> ");

        // trace obj-instance for non-static methods
        if (mi.isMethod() && !Modifier.isStatic(m.getModifiers())) {
            sb.append("\"+$0+\"").append(m.getName()).append("(");
        } else {
            sb.append(cl.getName()).append(".").append(m.getName()).append("(");
        }

        String sep = "";
        for (int i = 0; i < paramTypes.length; i++) {
            sb.append(sep);
            sep = ", ";
            sb.append(argumentVariableName(m, i, paramNames));
            sb.append(parameterValue(paramTypes, i, paramAnnotations));
        }
        sb.append(") {");
        return sb.toString();
    }

    LocalVariableAttribute getParameterValues(MethodInfo mi) {
        CodeAttribute ca = mi.getCodeAttribute();
        return (LocalVariableAttribute) ca.getAttribute("LocalVariableTable");
    }

    /**
     * Get all annotations of all parameters (can return null)
     *
     * @return returns null if there's no annotations for any of the parameters
     */
    Annotation[][] getParameterAnnotations(MethodInfo mi) {
        ParameterAnnotationsAttribute paa = (ParameterAnnotationsAttribute)
                mi.getAttribute(ParameterAnnotationsAttribute.visibleTag);
        // can be 'null' if there's no annotations for any parameter
        return ((paa != null) ? paa.getAnnotations() : null);
    }

    String argumentVariableName(CtBehavior m, int i, LocalVariableAttribute localVars) {
        int modifiers = m.getModifiers();
        // first argument of non-static methods is 'this' pointer -> skip over
        if (!Modifier.isStatic(modifiers)) i++;
        return localVars.variableName(i);
    }

    String parameterValue(CtClass[] pTypes, int i, Annotation[][] parameterAnnotations) {
        final String fmt = "cc.coreid.tracer.TraceHelper.format";
        StringBuilder sb = new StringBuilder("=<***>");
        if (!isParameterMasked(i, parameterAnnotations)) {
            sb = new StringBuilder("='\"+");
            if (pTypes[i].isPrimitive()) {
                sb.append("$").append(i + 1);
            } else {
                sb.append(fmt).append("($").append(i + 1).append(")");
            }
            sb.append("+\"'");
        }
        return sb.toString();
    }

    boolean isParameterMasked(int i, Annotation[][] annotations) {
        boolean res = false;
        if (annotations != null) {
            for (Annotation a : annotations[i]) {
                if (a.getTypeName().equals(Trace.class.getName())) {
                    res = ((BooleanMemberValue) a.getMemberValue("exclude")).getValue();
                }
            }
        }
        return res;
    }


    String methodReturn(CtBehavior m) throws NotFoundException {
        StringBuilder sb = new StringBuilder("<<< } ");
        if (m instanceof CtConstructor) {
            sb.append(":\"+").append("$0").append("+\"");
        } else if (!isVoid(m)) {
            sb.append(cl.getName()).append(".").append(m.getName());
            sb.append(" : \"+").append("$1").append("+\"");
        } else {
            sb.append(cl.getName()).append(".").append(m.getName());
            sb.append(": <void>");
        }
        return sb.toString();
    }

    String methodException(CtBehavior m) {
        return "<<< } "+cl.getName()+"."+m.getName()+" : \"+$e+\"";
    }

    boolean isVoid(CtBehavior m) throws NotFoundException {
        boolean isVoid = true;
        if (m.getMethodInfo().isMethod()) {
            CtClass returnType = ((CtMethod) m).getReturnType();
            isVoid = "void".equals(returnType.getName());
        }
        return isVoid;
    }
}
