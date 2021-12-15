package cc.coreid.tracer;

import java.util.logging.Level;
import java.util.logging.Logger;

// FIXME: just a simple POJO class to prototype first steps in instrumentation -> get rid of it and do proper unit-testing
public class Point {
    private static final Logger log = Logger.getLogger(Point.class.getName());

    private int x;
    private int y;

    // public and static
    public static void main(String[] argv) {
        log.log(Level.CONFIG, "Point.main.....");
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.out.println("rootPath='"+rootPath+"'");


        Point x = new Point();
        x.publicMove(1, 2);
        x.privateMove(1, 2);
        x.arrayParameters(new int[]{3, 4});
        x.maskArgument(5, 6, "password");
        x.publicExclude(7, 8);
        x.multipleReturn(0);
        x.multipleReturn(1);
        x.multipleReturn(2);
        x.traceExeptions(0);
        x.traceExeptions(1);

    }

    synchronized public void publicMove(int x, int y) {
        this.x = x;
        this.y = y;
        privateMove(x, y);
    }

    private void privateMove(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void arrayParameters(int[] val) {
        this.x = val[0];
        this.y = val[1];
    }

    public void maskArgument(int x, int y, @Trace(exclude = true) String password) {
        this.x = x;
        this.y = y;
    }

    @Trace(exclude = true)
    public void publicExclude(int x, int y) {
        this.x = x;
        this.y = y;
        privateInclude(x, y);
    }

    @Trace
    private void privateInclude(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int multipleReturn(int x) {
        switch(x) {
            case 1: return 1;
            case 2: return 2;
            default: return 0;
        }
    }

    public int traceExeptions(int x) {
        if(x == 1) {
            return 0;
        }
        else {
            throw new RuntimeException("guguseli");
        }
    }
}
