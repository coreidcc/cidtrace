package cc.coreid.tracer;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TraceHelper {
    final private static Logger log = Logger.getLogger(TraceHelper.class.getName());

    /**
     * Formats arrays with the help of the java.utils.Arrays helper methods.
     *
     * @param val the value to be formatted
     * @return String representation of the value
     */
    public static String format(Object val) {
        try {
            String res = String.valueOf(val);
            if ((val != null) && val.getClass().isArray()) {
                // we need to test for all the different types primitive type arrays because
                // a) we can't cast array of primitives to Object[] (-> type incompatibility)
                // b) java does static dispatching of overloaded methods (compile time)
                switch (val.getClass().getName()) {
                    case "[Z":
                        return Arrays.toString((boolean[]) val);
                    case "[B":
                        return Arrays.toString((byte[]) val);
                    case "[C":
                        return Arrays.toString((char[]) val);
                    case "[D":
                        return Arrays.toString((double[]) val);
                    case "[F":
                        return Arrays.toString((float[]) val);
                    case "[I":
                        return Arrays.toString((int[]) val);
                    case "[J":
                        return Arrays.toString((long[]) val);
                    case "[S":
                        return Arrays.toString((short[]) val);
                    default:
                        return Arrays.deepToString((Object[]) val);
                }
            }
            return res;
        } catch (Exception ex) {
            log.log(Level.WARNING, "TraceHelper.format - failed", ex);
            return val.getClass().getName() + "@" + Integer.toHexString(val.hashCode());
        }
    }
}
